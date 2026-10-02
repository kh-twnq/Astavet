# AstaVet repository reference

Read the root `AGENTS.md` first. This page provides details needed only for affected areas. Check current project files before relying on declared versions or historical gaps.

## Stack and layout

- Store MVP: product listings/details, localStorage cart, COD checkout, product/variant/inventory administration, and order administration. The backend verifies prices, reserves stock, and prevents duplicate orders. `PaymentMethod` currently contains only `COD`.
- Backend declarations: Java 21, Spring Boot 4.1.1, Spring MVC/Security/Data JPA, Jakarta Validation, Flyway, PostgreSQL, springdoc 3.1.0, Gradle wrapper 9.7.1. Check `backend/build.gradle` and `backend/gradle/wrapper/gradle-wrapper.properties` before using these versions.
- Frontend declarations: Next.js 16.3.7 App Router, React 19.3.0, TypeScript 5.9.3 strict, Tailwind CSS 4.3.3, ESLint 9.39.1, Vitest 4.1.11. Check `frontend/package.json`, lockfile, and installed documentation for version-sensitive APIs. README and package metadata require Node.js >=20.17; README requires PostgreSQL >=15 while Compose uses `postgres:17-alpine`. Declared versions do not establish runtime compatibility.
- `backend/src/main/java/com/astavet/`: `controller/` handles HTTP APIs; `service/` holds business logic and transactions; `repository/` holds JPA access. `entity/` holds models/status rules; `dto/request/` and `dto/response/` hold API contracts; `mapper/` converts entities to responses; `config/` and `exception/` hold configuration and API errors.
- `backend/src/main/resources/application.yml` holds datasource, session, CORS and shipping configuration. `backend/src/main/resources/db/migration/` holds Flyway migrations. Backend tests are under `backend/src/test/java/com/astavet/`.
- `frontend/src/app/` holds storefront, cart, checkout, order-success and admin routes; `components/` holds shared UI; `features/cart/` holds cart state/model/tests; `features/admin/` holds the product form; `lib/` holds API client/types/formatting. Static assets are in `frontend/public/`.
- Root `docker-compose.yml` runs PostgreSQL only. `.env.example` and `frontend/.env.local.example` are templates; the backend does not automatically read `.env`. README describes environment setup and deployment requirements.

## Commands

These are documented commands or commands inferred from project configuration, not results of a current run. Use the stated working directory.

| Purpose | Working directory | Command / condition |
| --- | --- | --- |
| Database | root | `docker compose up -d postgres` |
| Backend development | `backend/` | `./gradlew bootRun` (default port 8080; Swagger `/swagger-ui.html`) |
| Backend build | `backend/` | `./gradlew build` (Java plugin task, inferred from build configuration) |
| Backend unit tests | `backend/` | `./gradlew test` |
| PostgreSQL integration tests | `backend/` | With a proven disposable database, load `ASTAVET_TEST_DATABASE_URL`, `ASTAVET_TEST_DATABASE_USERNAME`, `ASTAVET_TEST_DATABASE_PASSWORD`; run `./gradlew test --tests '*IntegrationTest'`. See README for the example. |
| Frontend dependency setup | `frontend/` | `npm install`; keep `package-lock.json` synchronized when dependencies intentionally change. |
| Frontend development | `frontend/` | `npm run dev` (default port 3000) |
| Frontend validation | `frontend/` | `npm test`, `npm run lint`, `npm run build` |
| Frontend production server | `frontend/` | `npm run start` after build |
| Frontend dependency audit | `frontend/` | `npm audit` |

There is no configured backend lint task and no separate documented frontend type-check script. `tsconfig.json` enables `strict` and `noEmit`.

## Implementation conventions

- Maintain controller -> service -> repository business dependencies; controllers validate request DTOs and return responses, while services use constructor injection and transactions. `AuthController` directly handles Spring Security/session; `CheckoutController` reads `StoreProperties`. These are recorded project exceptions. This layering is an AstaVet convention, not a Spring mandate.
- `backend/src/test/java/com/astavet/architecture/ControllerRepositoryBoundaryTest.java` checks that controller Java sources do not import or use fully qualified types from `com.astavet.repository`. It runs with `./gradlew test`, uses the JDK parser, and requires no extra dependency. It checks this direct package boundary, not every possible runtime dependency.
- Java classes/records use PascalCase, members camelCase, packages lowercase. Request/response DTOs use records and Jakarta Validation with explicit mappers. APIs use `/api/v1`, and admin APIs use `/api/v1/admin`. Synchronize frontend types and client with DTO changes.
- Frontend filenames use kebab-case except App Router `page.tsx`/`layout.tsx`; alias `@/*` maps to `src/*`. Use `"use client"` for state/browser APIs and keep user-facing messages in Vietnamese. `frontend/AGENTS.md.backup` is inactive but records the principle of consulting installed Next documentation before changing Next APIs.
- Flyway owns schema changes. Hibernate uses `ddl-auto: validate` and `open-in-view: false`. Add `V<n>__<description>.sql`; do not edit applied migrations. Money uses integer amounts (`bigint`/Java long), IDs UUID, timestamps UTC according to schema/configuration.
- Preserve server-side total recalculation, transactions and PostgreSQL locks for idempotency and inventory. Do not trust browser prices. README and entity/service tests describe order status, one-time inventory restoration, and COD collection/refund conditions.
- Admin uses session cookies, CSRF `XSRF-TOKEN`/`X-XSRF-TOKEN`, credentials and a specific CORS origin. Use `frontend/src/lib/api.ts` to preserve the client contract. Public checkout is currently exempt from CSRF in `SecurityConfig`.

## Validation and gaps

- Run `./gradlew test` for backend business changes. For schema, locking, inventory, or persistence changes, additionally run PostgreSQL integration tests only after proving the target is a separate disposable test database. Both integration fixtures call `deleteAll()` and skip without `ASTAVET_TEST_DATABASE_URL`; H2 cannot replace PostgreSQL advisory locks. Report skipped tests as skipped.
- Run `npm test`, `npm run lint`, and `npm run build` for frontend changes. Existing tests focus on the cart model. For API contracts, inspect both sides and manually verify affected flows when no automated flow coverage exists.
- For documentation-only work, verify paths and commands against current source, links, and the final diff. Setup and application tests are unnecessary.
- No active nested `AGENTS.md`, CI workflow, E2E suite, or controller/security test was present when this reference was written. Recheck before relying on these gaps.
- The first startup bootstraps admin from `ADMIN_EMAIL`/`ADMIN_PASSWORD`; later environment changes do not update the stored password. Keep `.env` and `.env.local` out of commits. In-memory rate limiting is unsuitable for multiple backend instances. README covers HTTPS/secure cookies, reverse proxy, secrets, backups and monitoring for production; automated deployment procedures are not supplied by the repo.
