# MindForge Development Handoff

## Purpose

Continue development of MindForge on another computer. The immediate goal is to connect the existing Session/CSRF and Note APIs to the Next.js client and complete the first browser-visible vertical slice.

For product scope and acceptance requirements, read:

- `docs/developer-knowledge-hub-prd.md`
- `docs/design.md`
- `docs/development-plan.md` (current execution order and checklists)

Do not redesign the product from this handoff; those documents remain the source of truth.

## Repository state at handoff

- Repository: `https://github.com/Arthur-Artoria/MindForge.git`
- Branch: `main`
- Plan snapshot: branch `main`, commit `d7c670a` on 2026-08-14. Run `git log -1 --oneline` after pulling instead of assuming this commit is still current.
- Backend test command passed on 2026-08-14. The generated test reports recorded 22 tests with no failures or errors, including 4 Auth Session/CSRF integration tests and 14 Note Controller integration tests:

  ```powershell
  cd apps/backend
  .\gradlew.bat test
  ```

  Result: `BUILD SUCCESSFUL`.

After pulling a committed handoff update, the working tree should be clean before new development begins.

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
  - Implements JSON login, `/me`, and CSRF token retrieval.
  - Calls `AuthenticationManager`, executes the configured `SessionAuthenticationStrategy`, creates a `SecurityContext`, puts it in `SecurityContextHolder`, then saves it through `SecurityContextRepository`.
- `SecurityConfig.java`
  - Builds the `SecurityFilterChain`, `AuthenticationManager`, `PasswordEncoder`, `UserDetailsService`, and `SecurityContextRepository`.
  - Configures logout through Spring Security's `LogoutFilter` rather than a controller method.
  - Uses `ChangeSessionIdAuthenticationStrategy` for session-fixation protection and `CsrfAuthenticationStrategy` to invalidate the pre-login token.
  - Uses `HttpSessionCsrfTokenRepository`; login and logout no longer bypass CSRF.

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

### Note module

- `POST /api/notes`, `GET /api/notes`, `GET /api/notes/{id}`, `PUT /api/notes/{id}`, and `DELETE /api/notes/{id}` are implemented.
- Note ownership always comes from `AuthenticatedUser`; request DTOs do not accept `userId`.
- Repository queries constrain both `userId` and `deletedAt is null`. Cross-user access and access to deleted Notes return 404.
- DELETE uses soft deletion. Flyway V6 added `deleted_at`, and V7 changed it to `timestamptz`; the previously used V2 migration was not modified.
- Note Controller integration tests cover unauthenticated and missing-CSRF writes, validation, ownership, list filtering, update, delete, and post-delete invisibility.

## Verified behavior and diagnostics

- PostgreSQL and Spring Boot can connect, Flyway migrations run, Hibernate validates the schema, and the backend context test passes.
- `GET http://localhost:8080/hello` returned HTTP 200.
- `POST /api/auth/login` without a valid CSRF token is rejected before MVC with the stable JSON CSRF error response.
- The SPA-style protocol verified by backend integration tests is:

  ```text
  GET /api/auth/csrf
    -> POST /api/auth/login with the same Session and CSRF header
    -> GET /api/auth/csrf again after login
    -> authenticated API calls
    -> POST /api/auth/logout with the current CSRF token
  ```

- Authentication tests verify login -> `/me` -> logout -> old Session receives 401, session ID rotation on login, the real CSRF token acquisition/refresh lifecycle, and stable JSON 401/403 responses.
- Note tests verify authenticated creation with a real CSRF token, ownership isolation, CRUD behavior, soft deletion, and list filtering.

## Known gaps and next work

The authoritative implementation order, mutable status, and acceptance checklists are maintained only in `docs/development-plan.md`. Keep this handoff focused on stable context so the two documents do not drift.

The current execution target is Phase C in that plan:

1. Configure a Next.js same-origin rewrite/proxy for `/api/*`, with the backend origin supplied by environment configuration.
2. Build the frontend API client and the real login -> CSRF refresh -> `/me` -> logout flow.
3. Build the Notes list, create, edit, and delete pages against the existing backend API.
4. Verify the flow in a browser, then run frontend lint/build and the backend test suite.

The frontend is still the default Create Next App page. Before changing it, read `apps/web/AGENTS.md` and the relevant documentation bundled under `node_modules/next/dist/docs/`, because this repository uses Next.js 16.

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

Pull the latest branch, start PostgreSQL, run the existing tests, then read `docs/development-plan.md`. Resume from Phase C, beginning with the documented Next.js 16 rewrite/proxy decision and the frontend Session/CSRF API client.
