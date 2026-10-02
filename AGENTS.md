# AstaVet — repository instructions

## Start here

- AstaVet is a Java 21/Spring Boot backend and a Next.js storefront/admin frontend for COD orders. Read [README.md](README.md) for setup, commands, and order rules. Versions in project files are declarations; verify the installed tools when compatibility matters.
- Backend code is under `backend/src/main/java/com/astavet/`; tests are under `backend/src/test/java/com/astavet/`. Frontend code is under `frontend/src/`. See [.agents/harness/repository.md](.agents/harness/repository.md) for the package map, conventions, test selection, and known gaps.
- The canonical AI guidance collection is [.agents/](.agents/README.md). For Java work, read [.agents/rules/README.md](.agents/rules/README.md) and only relevant rule groups. For COD order-flow reviews, read [.agents/skills/astavet-cod-validation/SKILL.md](.agents/skills/astavet-cod-validation/SKILL.md). Codex discovers workspace skills directly from `.agents/skills/`; GitNexus skills may be invoked by path or separate registration. Apply each only within its stated scope.

## Work on a task

1. Define scope and observable completion criteria. Inspect `git status` and the relevant existing diff before editing; preserve user changes.
2. Read relevant source and instructions. For code exploration and symbol edits, follow [.agents/harness/gitnexus.md](.agents/harness/gitnexus.md): check index freshness, use graph evidence first, run upstream `impact` before editing a symbol, then verify important conclusions in current source. Treat `UNKNOWN` and missing graph edges as unresolved.
3. Implement the scoped change and run checks appropriate to its behavior. For a meaningful code diff, run GitNexus `detect_changes` before handoff. Review the final diff and report checks actually run, skipped, or blocked.
4. For work spanning sessions, use [.agents/harness/workflow.md](.agents/harness/workflow.md) and its plan/handoff templates. Record a concrete restart point. Do not commit, push, deploy, or publish without authorization.

## Invariants

- Keep the project's controller -> service -> repository business dependency direction; controllers do not access JPA repositories directly. `AuthController` handles security/session directly, and `CheckoutController` reads `StoreProperties`. This is an AstaVet convention, not a Spring requirement.
- Keep API DTOs and frontend types/client synchronized. Preserve server-side price recalculation, inventory reservation, idempotency locks, status transitions, and COD collection/refund rules. Flyway owns schema changes; add a new migration rather than editing an applied one.
- Admin uses session cookies, CSRF and a specific CORS origin; use `frontend/src/lib/api.ts` for this contract. Keep user-facing text in Vietnamese. Consult version-matched Next documentation before changing Next APIs.
- PostgreSQL integration fixtures call `deleteAll()`. Run them only against a proven dedicated disposable test database. A skipped integration test is not a pass.
- Do not add dependencies, abstractions, or architectural changes solely for harness work. Do not use subagents unless explicitly requested.

## Validation map

| Change | Checks |
| --- | --- |
| Backend business logic | `cd backend && ./gradlew test` |
| Schema, locking, inventory, persistence | Backend tests plus PostgreSQL integration tests after the database safety gate; see [repository guidance](.agents/harness/repository.md). |
| Frontend | `cd frontend && npm test && npm run lint && npm run build` |
| API contract | Check backend DTO and frontend client/types; manually verify affected flow if automated coverage is absent. |
| Documentation only | Check paths and commands against source, links, and diff; application tests are unnecessary. |

The commands above are instructions, not a record of executed checks. See [README.md](README.md) for setup and exact integration-test environment variables.
