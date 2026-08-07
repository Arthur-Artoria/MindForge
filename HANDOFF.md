# MindForge Development Handoff

## Purpose

Continue development of the MindForge authentication flow on another computer. The immediate goal is to turn the current runnable Spring Security skeleton into a verified Session-based REST login flow, then connect it to the Next.js client.

For product scope and acceptance requirements, read:

- `docs/developer-knowledge-hub-prd.md`
- `docs/design.md`

Do not redesign the product from this handoff; those documents remain the source of truth.

## Repository state at handoff

- Repository: `https://github.com/Arthur-Artoria/MindForge.git`
- Branch: `main`
- Commit: `fed7342` (`feat: add Spring Security and validation dependencies to backend`)
- Working tree was clean before this `HANDOFF.md` was created.
- Backend test command passed on 2026-08-07:

  ```powershell
  cd apps/backend
  .\gradlew.bat test
  ```

  Result: `BUILD SUCCESSFUL`.

`HANDOFF.md` itself is the only expected new workspace change after generation.

## Runtime and setup

- Java 21
- Spring Boot 4.1.0
- Spring MVC, Spring Data JPA, Spring Security, Validation, Flyway
- PostgreSQL 18 via `docker-compose.yml`
- Gradle wrapper is committed under `apps/backend`

On the new computer:

```powershell
git clone https://github.com/Arthur-Artoria/MindForge.git
cd MindForge
docker compose up -d
cd apps/backend
.\gradlew.bat test
.\gradlew.bat bootRun
```

The datasource settings are in `apps/backend/src/main/resources/application.properties` and the matching local development database configuration is in `docker-compose.yml`. Do not copy credentials into logs or new documentation; use local environment/profile overrides if the configuration becomes non-local.

Useful checks:

```powershell
docker compose ps
curl.exe -i http://localhost:8080/hello
```

## Authentication decision

Use a Session + HttpOnly Cookie flow for the current first-party Next.js client and single Spring Boot backend:

```text
POST /api/auth/login
  -> AuthenticationManager.authenticate(...)
  -> persist SecurityContext in HttpSession
  -> return current user and JSESSIONID cookie

GET /api/auth/me
  -> restore SecurityContext from JSESSIONID
  -> return current user

POST /api/auth/logout
  -> invalidate the authenticated session
```

Do not introduce a custom JWT filter at this stage. Re-evaluate JWT only if mobile clients, third-party clients, stateless deployment, or cross-service token propagation become real requirements.

## Current implementation

### User module

- `apps/backend/src/main/java/cn/artoria/mind_forge/user/User.java`
  - JPA entity mapped to the existing `users` table.
  - The table name was previously `user`, which caused Hibernate validation to fail with `missing table [user]`; it is now correctly mapped to `users`.
- `apps/backend/src/main/java/cn/artoria/mind_forge/user/UserRepository.java`
  - Provides lookup and uniqueness checks for email and username.

Flyway owns the schema. See `apps/backend/src/main/resources/db/migration/`, especially `V1__create_users_table.sql`. Keep `spring.jpa.hibernate.ddl-auto=validate`; do not let Hibernate mutate the schema.

### Auth module

- `AuthenticatedUser.java`
  - Spring Security principal containing user id, email, display name, authorities, and the password hash during authentication.
  - Implements `CredentialsContainer` so the password hash can be erased after authentication.
- `DatabaseUserDetailsService.java`
  - Adapter from `UserRepository` to Spring Security's `UserDetailsService` interface.
  - Normalizes login email and loads a domain `User`, then returns `AuthenticatedUser`.
- `LoginRequest.java`
  - Validated email/password JSON input.
- `CurrentUserResponse.java`
  - Safe response projection; does not expose the password hash.
- `AuthController.java`
  - Implements JSON login and `/me`.
  - Calls `AuthenticationManager`, creates a `SecurityContext`, puts it in `SecurityContextHolder`, then saves it through `SecurityContextRepository`.
- `SecurityConfig.java`
  - Builds the `SecurityFilterChain`, `AuthenticationManager`, `PasswordEncoder`, `UserDetailsService`, and `SecurityContextRepository`.
  - Configures logout through Spring Security's `LogoutFilter` rather than a controller method.

### Important Bean registration detail

`DatabaseUserDetailsService` currently has no `@Service` annotation. It is deliberately registered explicitly in `SecurityConfig`:

```java
@Bean
UserDetailsService userDetailsService(UserRepository userRepository) {
  return new DatabaseUserDetailsService(userRepository);
}
```

This is correct. Do not also add `@Service` unless the explicit `@Bean` method is removed; use exactly one registration mechanism.

The runtime dependency chain is:

```text
UserRepository
  -> DatabaseUserDetailsService (UserDetailsService)
  -> DaoAuthenticationProvider + PasswordEncoder
  -> ProviderManager (AuthenticationManager)
  -> AuthController
```

`SecurityConfig` creates and wires this mechanism at application startup. `AuthController` uses the resulting interfaces at request time; the two classes do not call each other directly.

## Verified behavior and diagnostics

- PostgreSQL and Spring Boot can connect, Flyway migrations run, Hibernate validates the schema, and the backend context test passes.
- `GET http://localhost:8080/hello` returned HTTP 200.
- Before the current CSRF exception was added, `POST /api/auth/login` without a CSRF token returned:

  ```http
  HTTP/1.1 403
  Content-Length: 0
  Set-Cookie: JSESSIONID=...
  ```

  This was expected Spring Security behavior: `CsrfFilter` rejected the request before `AuthController`, so the controller log did not run. Apifox displayed an empty Body because the response body was zero bytes; the 403 was visible in response metadata.
- `SecurityConfig` currently contains:

  ```java
  .csrf(csrf -> csrf
      .ignoringRequestMatchers("/api/auth/login"))
  ```

  This is only a temporary development step to reach and test the controller. It is not the intended final browser security design.

## Not yet verified

Do not claim the login feature is complete until the following full acceptance loop has been tested against a known database user:

1. Correct email/password returns HTTP 200 and a session cookie.
2. Wrong credentials return a generic JSON HTTP 401 without revealing whether the email exists.
3. `GET /api/auth/me` with the cookie returns the authenticated user.
4. `POST /api/auth/logout` invalidates the session.
5. `/api/auth/me` with the old cookie then returns HTTP 401.

There is currently only a context-load test. No authentication integration tests have been added.

## Known gaps and next work

Work in this order:

1. **Add authentication integration tests.** Add Spring Security test support if needed. Cover the five-step acceptance loop above before expanding the feature.
2. **Finish CSRF for the SPA.** Replace the login ignore rule with a permitted CSRF-token endpoint (for example `GET /api/auth/csrf`). The frontend must preserve the session cookie and send the returned CSRF token header on unsafe requests. Refresh the token after login and logout as required by Spring Security.
3. **Restore the complete session authentication lifecycle.** The manual controller currently saves the `SecurityContext`, but it does not invoke a `SessionAuthenticationStrategy`; session fixation protection must be handled. Consider whether a filter-based JSON authentication flow would let Spring Security own more of this lifecycle.
4. **Return structured REST errors.** Configure an `AuthenticationEntryPoint` for JSON 401 responses and an `AccessDeniedHandler` for JSON 403 responses. Map authentication failures consistently.
5. **Configure frontend integration together.** Design CORS, `credentials: "include"`, cookie `HttpOnly`/`Secure`/`SameSite`, allowed origins, and CSRF as one deployment-aware decision. Do not configure these independently.
6. **Review password encoding before changing it.** The project currently uses `BCryptPasswordEncoder`. Switching to `PasswordEncoderFactories.createDelegatingPasswordEncoder()` changes stored output to an `{id}...` format; existing unprefixed BCrypt hashes require an explicit migration or compatibility plan.
7. **Normalize email consistently.** Login lowercases email, but registration/bootstrap and the database uniqueness rule must use the same normalization. PostgreSQL's current unique constraint on raw email is case-sensitive.
8. **Cleanup after behavior is locked down.** Remove the unused `DaoAuthenticationConfigurer` import, replace/remove `System.out.println`, and apply formatting.

## Failure boundaries to preserve

- `permitAll()` only bypasses the authorization requirement; it does not bypass the rest of the Security Filter Chain, including CSRF.
- `AuthenticationManager.authenticate()` validates credentials but does not by itself persist login across requests.
- `SecurityContextHolder` represents the current request/thread; `SecurityContextRepository` persists/restores context across requests.
- Business modules must derive `userId` from the authenticated backend principal, never from a client-supplied user id.
- Do not compare raw passwords manually in `DatabaseUserDetailsService`; `DaoAuthenticationProvider` and `PasswordEncoder` own password verification.

## Suggested skills for the next agent

- `tdd` — implement the login/session/logout acceptance loop test-first.
- `diagnosing-bugs` — use for any startup, session, cookie, CSRF, CORS, or unexpected status-code failure.
- `codebase-design` — preserve the seams between the user module, Spring Security adapter, HTTP controller, and persistence strategy.
- `teach` — use if continuing the conceptual walkthrough while implementing.
- `implement` — use after the desired CSRF/session/error-response behavior is specified and implementation is requested.

## Recommended first action on the new computer

Start PostgreSQL, run the existing tests, inspect `SecurityConfig.java` and `AuthController.java`, then add one integration test that proves a correct login returns a session cookie and that the same cookie authenticates `/api/auth/me`. This provides the tight feedback loop needed before changing CSRF or session behavior.
