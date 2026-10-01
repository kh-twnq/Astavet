# AstaVet — repository instructions

## Scope and stack

- AstaVet store MVP: product listings/details, localStorage cart, COD checkout, and administration of products/variants/inventory and orders. The backend verifies prices, reserves inventory, and prevents duplicate orders; `PaymentMethod` currently contains only `COD`.
- Backend: Java 21 toolchain, Spring Boot 4.1.1, Spring MVC/Security/Data JPA, Jakarta Validation, Flyway, PostgreSQL, springdoc 3.1.0; Gradle wrapper 9.7.1 (`backend/build.gradle`, `gradle-wrapper.properties`).
- Frontend: Next.js 16.3.7 App Router, React 19.3.0, TypeScript 5.9.3 strict, Tailwind CSS 4.3.3, ESLint 9.39.1, Vitest 4.1.11. The direct dependencies in `frontend/package-lock.json` v3 match the manifest.
- README and `frontend/package.json` declare Node.js >=20.17; README requires PostgreSQL >=15, while Compose uses `postgres:17-alpine`. These are declared versions; runtime compatibility and all transitive dependencies' Node requirements have not been verified.

## Layout

- `backend/src/main/java/com/astavet/`: `controller/` for HTTP APIs, `service/` for business logic/transactions, `repository/` for JPA; further grouped by auth/product/order. `entity/` holds models and status rules; `dto/request/` and `dto/response/` define API contracts; `mapper/` converts entities to responses; `config/` handles security/bootstrap/configuration; `exception/` centralizes API errors.
- `backend/src/main/resources/application.yml`: datasource, session, CORS origin, shipping fee; `db/migration/`: V1 initial schema, V2 payment history.
- `backend/src/test/java/com/astavet/`: entity/service tests and two PostgreSQL integration tests (product management, concurrent order cancellation).
- `frontend/src/app/`: storefront, cart, checkout, order-success, and admin pages; `components/`: shared UI; `features/cart/`: cart state/model/tests; `features/admin/`: product form; `lib/`: API client, types, formatting.
- `frontend/public/`: static assets. Root `docker-compose.yml` runs only PostgreSQL; `.env.example` and `frontend/.env.local.example` are configuration templates. See [README.md](README.md) for running and deployment procedures.

## Commands and working directories

The commands below are documented in README/package scripts, except the backend build command inferred from the Java plugin. No setup, build, lint, or tests were run when creating this document; this table is not a record of test results.

| Purpose | Directory | Command / conditions |
| --- | --- | --- |
| Environment setup | root | Copy `.env.example` to `.env`, change passwords, and load variables into the shell as described in README; the backend does not automatically read `.env`. |
| Database | root | `docker compose up -d postgres` |
| Frontend setup | root | Copy `frontend/.env.local.example` to `frontend/.env.local`. |
| Install frontend dependencies | `frontend/` | `npm install` (README command; keep package-lock synchronized when intentionally changing dependencies). |
| Backend dev | `backend/` | `./gradlew bootRun` — default port 8080, Swagger `/swagger-ui.html`. |
| Backend build | `backend/` | `./gradlew build` — Java plugin task, not documented in README; not executed for verification. |
| Backend test | `backend/` | `./gradlew test` |
| PostgreSQL integration test | `backend/` | Load `ASTAVET_TEST_DATABASE_URL`, `ASTAVET_TEST_DATABASE_USERNAME`, and `ASTAVET_TEST_DATABASE_PASSWORD`, then run `./gradlew test --tests '*IntegrationTest'`; see README for the complete example. |
| Frontend dev | `frontend/` | `npm run dev` — default port 3000. |
| Frontend build / serve build | `frontend/` | `npm run build` / `npm run start` |
| Frontend lint | `frontend/` | `npm run lint` |
| Frontend test | `frontend/` | `npm test` |
| Dependency audit | `frontend/` | `npm audit` (README). |
| Separate type-check | — | No separate script/command is documented; `tsconfig.json` enables `strict` and `noEmit`. |
| Backend lint | — | No separate lint task/plugin is configured. |

## Implementation conventions

- Keep separate `controller/`, `service/`, and `repository/` packages and require the business dependency direction **controller -> service -> repository**. Controllers receive/validate DTOs and return responses; they must not access JPA repositories directly. Services use constructor injection and manage transactions.
- Product/order currently follow this dependency direction. Recorded exceptions: `AuthController` handles Spring Security/session directly through `AuthenticationManager` and `SecurityContextRepository`; `CheckoutController` reads `StoreProperties` directly. Do not refactor these exceptions as part of documentation work.
- Keep Java PascalCase for classes/records, camelCase for members, and lowercase packages; request/response DTOs use records and Jakarta Validation, with explicit mappers. APIs live under `/api/v1`, admin under `/api/v1/admin`; keep frontend types/client synchronized with DTOs.
- Follow existing frontend kebab-case filenames, `page.tsx`/`layout.tsx` routes, and the `@/*` -> `src/*` alias; use `"use client"` for state/browser APIs. Keep messages/UI in Vietnamese and technical names consistent with code. ESLint uses Next core-web-vitals and TypeScript rules.
- Flyway manages the schema, with Hibernate `ddl-auto: validate` and `open-in-view: false`; add `V<n>__<description>.sql` migrations for schema changes and do not edit applied migrations. Store money as integers (`bigint`/Java long), IDs as UUIDs, and timestamps in UTC according to schema/configuration.
- Preserve server-side price/total recalculation, transactions, and PostgreSQL locks for idempotency/inventory; do not trust browser price data. Status transitions, restoring inventory exactly once, and COD collection/refund conditions are described in README and entity/service tests.
- Admin uses session cookies, CSRF `XSRF-TOKEN`/`X-XSRF-TOKEN`, credentials, and a specific CORS origin; use `lib/api.ts` to preserve this contract. Public checkout is currently exempt from CSRF in `SecurityConfig`.
- `frontend/AGENTS.md.backup` retains instructions to read the Next documentation bundled with the installed version before changing Next APIs. Preserve the principle of consulting version-matched documentation when working on the frontend; the backup is not an active AGENTS.md.

## Validation and completion

- For backend business changes, run `./gradlew test`; for schema, locking/inventory, or persistence changes, also run PostgreSQL integration tests against a separate test database. Both integration tests are skipped without `ASTAVET_TEST_DATABASE_URL`; fixtures call `deleteAll()`, so never point them at a database containing data that must be retained. H2 is a test dependency but cannot replace PostgreSQL advisory locks.
- For frontend changes, run `npm test`, `npm run lint`, and `npm run build`; existing tests focus on the cart model. For API contract changes, check both sides and manually verify affected flows if corresponding automated tests do not exist.
- For documentation-only work, compare paths/commands with source files and review the diff; installing dependencies or running the application is unnecessary. Work is complete when the diff stays within scope, documentation reflects the code, and the report identifies checks run, not run, or skipped.

## Constraints and gaps

- No active nested AGENTS.md, CI workflows, or separate type-check scripts exist in the inspected source tree. The current test suite has no E2E or controller/security tests; do not treat unit tests as evidence of HTTP flow coverage.
- Admin is bootstrapped on first startup from `ADMIN_EMAIL`/`ADMIN_PASSWORD`; later environment changes do not change the stored password. Do not commit `.env`/`.env.local`; replace sample passwords before running.
- Rate limiting currently uses memory and is unsuitable for multiple backend instances. HTTPS/secure cookies, a reverse proxy with the same origin, secrets, backups, and monitoring are production requirements in README; the repository does not provide automated deployment procedures.
- Declared versions have not been verified through installation/build. Do not upgrade versions or decide on architectural changes merely to resolve documentation uncertainty.

## Shared AI guidance

- When working on Java, explicitly read `.agent/rules/README.md` relative to this repository root and only the applicable rule groups. Shared reference files are not automatically loaded by Codex.
- AI skills and rules are maintained only in `.agent/`, including `.agent/skills/astavet-cod-validation/SKILL.md` for scoped COD validation. Read the relevant `SKILL.md` directly. Discover the project's actual JDK, framework, build and test versions; use matching primary documentation when behavior is uncertain. Keep framework guidance conditional on its presence.
- `.agent/` is the only AI skill directory in this workspace. Codex discovers the nine Java/COD skills through user-level symlinks in `~/.agents/skills/` pointing to `.agent/skills/<skill-name>`. GitNexus skills also live in `.agent/skills/`; invoke them by their paths or through separately installed user-level registrations. In Codex CLI or the IDE extension, select registered skills with `/skills` or mention them with `$<skill-name>`. User-level registrations are visible across workspaces; apply each skill only within its declared scope. Resolve symlinks before reading relative rule references; maintain workspace skill content only in `.agent/`.
- Project conventions take precedence over optional style defaults. Apply this repository's adopted architecture; the reusable layered controller/service/repository preference is not a Java/Spring mandate.

<!-- gitnexus:start -->
# GitNexus — Code Intelligence

This project is indexed by GitNexus as **Astavet** (1032 symbols, 2257 relationships, 85 execution flows).

> Index stale? Run `node .gitnexus/run.cjs analyze --index-only` from the project root — it auto-selects an available runner. No `.gitnexus/run.cjs` yet? Bootstrap with `npx`, `bunx`, or `pnpm dlx` — e.g. `bunx gitnexus@latest analyze` (npm 11 npx crash; #1939).

## Always Do

- **MUST run impact before editing.** Use `impact({target: "symbolName", direction: "upstream"})` or `node .gitnexus/run.cjs impact "symbolName" --direction upstream --repo .`; report callers, processes, and risk. Never substitute grep for graph analysis.
- **MUST analyze graph changes before committing.** Use `detect_changes({scope: "all"})` (MCP) or `node .gitnexus/run.cjs detect-changes --scope all --repo .` (CLI fallback). `partial: true` or `truncated: true` is not a clean check — a zero means unseen, not unaffected; re-run it. For regression review: `detect_changes({scope: "compare", base_ref: "main"})` or `node .gitnexus/run.cjs detect-changes --scope compare --base-ref "main" --repo .`.
- MUST warn on HIGH/CRITICAL `risk` pre-edit; never use `riskSharedAxes` to waive a HIGH/CRITICAL `risk` warning. Compare File/symbol: MCP File omits axes; Graph-RAG expands File.
- **MUST treat `risk: UNKNOWN` as unresolved, not as low.** An empty caller set is not evidence the symbol is unused — it can also mean the callers are not resolvable by the index (plain-object property access, dynamic dispatch, cross-language calls). `impact` pairs `UNKNOWN` with a `riskNote` saying so. Confirm with a text search before treating the symbol as safe to change or delete; do not proceed on the strength of a zero.
- **MUST use `query({search_query: "concept"})` for concepts/flows, `context({name: "symbolName"})` for a named symbol, or `impact` for blast radius, on read-only callers, dependencies, imports, or execution flow.** Graph first; text search only for empty/`UNKNOWN`/literals.
- For security review, `explain({target: "fileOrSymbol"})` lists taint findings (source→sink flows; needs `analyze --pdg`).

## Never Do

- NEVER edit a function, class, or method before MCP/CLI impact analysis.
- NEVER ignore HIGH or CRITICAL risk warnings from impact analysis, and never read `UNKNOWN` as an all-clear — it means the walk could not answer, which is the one verdict that requires confirming by other means.
- NEVER rename symbols with find-and-replace — use `rename` which understands the call graph.
- NEVER commit before MCP/CLI graph change analysis.

## Resources

| Resource | Use for |
| --- | --- |
| `gitnexus://repo/Astavet/context` | Codebase overview, check index freshness |
| `gitnexus://repo/Astavet/clusters` | All functional areas |
| `gitnexus://repo/Astavet/processes` | All execution flows |
| `gitnexus://repo/Astavet/process/{name}` | Step-by-step execution trace |

## CLI

| Task | Read this skill file |
| --- | --- |
| Understand architecture / "How does X work?" | `.agent/skills/gitnexus-exploring/SKILL.md` |
| Blast radius / "What breaks if I change X?" | `.agent/skills/gitnexus-impact-analysis/SKILL.md` |
| Trace bugs / "Why is X failing?" | `.agent/skills/gitnexus-debugging/SKILL.md` |
| Rename / extract / split / refactor | `.agent/skills/gitnexus-refactoring/SKILL.md` |
| Tools, resources, schema reference | `.agent/skills/gitnexus-guide/SKILL.md` |
| Index, status, clean, wiki CLI commands | `.agent/skills/gitnexus-cli/SKILL.md` |

<!-- gitnexus:end -->
