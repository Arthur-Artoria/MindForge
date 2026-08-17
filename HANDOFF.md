# MindForge Development Handoff

## Purpose

Continue development of MindForge on another computer. The immediate engineering goal is still to connect the existing Session/CSRF and Note APIs to the Next.js client and complete the first browser-visible vertical slice. A separate throwaway Dashboard prototype now exists to choose a visual direction before production UI work expands.

For product scope and acceptance requirements, read:

- `docs/developer-knowledge-hub-prd.md`
- `docs/development-plan.md` (current execution order and checklists)
- `apps/web/app/prototype/dashboard/` (temporary visual exploration, not a production specification)

`docs/design.md` was intentionally deleted on 2026-08-15 because the user considers it obsolete. Do not restore it or treat it as a source of truth. Product scope remains in the PRD. A durable replacement design specification has not been selected yet.

## Repository state at handoff

- Repository: `https://github.com/Arthur-Artoria/MindForge.git`
- Branch: `main`
- Plan snapshot: branch `main`, commit `cc9a514` on 2026-08-15. Run `git log -1 --oneline` after pulling instead of assuming this commit is still current.
- Backend test command passed on 2026-08-14. The generated test reports recorded 22 tests with no failures or errors, including 4 Auth Session/CSRF integration tests and 14 Note Controller integration tests:

  ```powershell
  cd apps/backend
  .\gradlew.bat test
  ```

  Result: `BUILD SUCCESSFUL`.

At this handoff update, the following work is intentionally uncommitted:

```text
M  HANDOFF.md
D  docs/design.md
?? apps/web/app/prototype/
```

The `design.md` deletion is intentional user work. The prototype directory and this handoff update must be committed and pushed before switching computers if they need to appear on the other device. Do not discard these paths during cleanup.

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

### Frontend and visual prototype

- The production root route is still the default Create Next App page. Phase C production frontend work has not started.
- A throwaway route was added under `apps/web/app/prototype/dashboard/` and is available at `/prototype/dashboard?variant=A`.
- The route intentionally contains three structurally different Dashboard directions using the same realistic MindForge sample content:
  - `A` — 工程田野日志: a chronological, editorial workspace built around evidence and weekly progress.
  - `B` — 知识星图: a project/knowledge relationship map with a contextual inspector.
  - `C` — 命令账本: a dense, keyboard-first operating ledger with quick capture.
- The bottom prototype switcher changes the `variant` URL parameter and supports left/right arrow keys. It is hidden in production builds.
- Prototype interactions are local-only: A toggles its focus action, B changes the node inspector, and C captures text in memory. Nothing calls the backend or persists data.
- Do not promote the prototype directly into production. First choose a direction, record why it won, then rewrite the selected ideas against real Phase C data and delete the losing variants/switcher.

Run it from the repository root:

```powershell
pnpm --filter web dev --hostname 127.0.0.1 --port 3000
```

Then open `http://127.0.0.1:3000/prototype/dashboard?variant=A` and switch among A/B/C.

The user-level `frontend-design` Skill was installed on the original computer at `C:\Users\Artoria\.codex\skills\frontend-design`. Skills outside the repository do not transfer with Git. Install it separately on the new computer if needed:

```powershell
npx skills add https://github.com/anthropics/skills --skill frontend-design
```

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
- On 2026-08-15, the new Dashboard prototype passed `pnpm --filter web lint` and `pnpm --filter web build` with Next.js 16.2.9.
- All three variants were rendered in the browser. URL switching, B's inspector selection, and C's in-memory quick capture worked without browser console errors.

## Known gaps and next work

The authoritative implementation order, mutable status, and acceptance checklists are maintained only in `docs/development-plan.md`. Keep this handoff focused on stable context so the two documents do not drift.

The current engineering execution target remains Phase C in that plan. It now begins by establishing the Spring OpenAPI contract, generated TypeScript types, the frontend request/CSRF/TanStack Query layers, and cross-project quality gates before implementing the login and Note pages. Keep the detailed order and acceptance checklist only in `docs/development-plan.md` instead of duplicating it here.

The production frontend is still the default Create Next App page; the prototype route does not complete any Phase C item. Before changing production routes, read `apps/web/AGENTS.md` and the relevant documentation bundled under `node_modules/next/dist/docs/`, because this repository uses Next.js 16.

## Failure boundaries to preserve

- `permitAll()` only bypasses the authorization requirement; it does not bypass the rest of the Security Filter Chain, including CSRF.
- `AuthenticationManager.authenticate()` validates credentials but does not by itself persist login across requests.
- `SecurityContextHolder` represents the current request/thread; `SecurityContextRepository` persists/restores context across requests.
- Business modules must derive `userId` from the authenticated backend principal, never from a client-supplied user id.
- Do not compare raw passwords manually in `DatabaseUserDetailsService`; `DaoAuthenticationProvider` and `PasswordEncoder` own password verification.

## Suggested skills for the next agent

- `frontend-design` — establish the durable visual direction after the user evaluates A/B/C; do not fall back to generic Dashboard styling.
- `prototype` — preserve the throwaway/variant discipline until a winner is chosen, then capture the decision and remove losing variants.
- `browser:control-in-app-browser` — render and inspect the chosen direction at real desktop/mobile sizes before accepting it.
- `tdd` — implement the login/session/logout acceptance loop test-first.
- `diagnosing-bugs` — use for any startup, session, cookie, CSRF, CORS, or unexpected status-code failure.
- `codebase-design` — preserve the seams between the user module, Spring Security adapter, HTTP controller, and persistence strategy.
- `teach` — use if continuing the conceptual walkthrough while implementing.
- `implement` — use after the desired CSRF/session/error-response behavior is specified and implementation is requested.

## Recommended first action on the new computer

Before leaving the original computer, commit and push the intentional `design.md` deletion, the prototype directory, and this handoff update. On the new computer, pull the latest branch and verify `git status`.

Then choose one of these paths explicitly:

1. Visual decision: run `/prototype/dashboard`, compare A/B/C, record the winner and useful borrowed elements, then replace the throwaway prototype with a durable design decision.
2. Engineering continuation: start PostgreSQL, run the existing backend tests, read `docs/development-plan.md`, and resume from its first unchecked Phase C infrastructure item.
