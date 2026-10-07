# AstaVet MVP

## Repository Map

This independent repository owns one Java 21 / Spring Boot 4.1 application, its PostgreSQL schema, and APIs consumed by the independent React JavaScript project in `../astavet-frontend`. Build inside this repository with the Gradle wrapper; run frontend npm commands in its own repository.

- `src/main/java/com/astavet/shop/`: `controller/v1/` (HTTP), `controller/support/` (session helpers), `service/` and `service/impl/` (business/transactions), `repository/`, `repository/impl/` and `repository/jpa/` (persistence), `dto/`, `domain/`, `entity/`, `config/`, `exception/`.
- `../astavet-frontend/`: storefront, cart, checkout, confirmation, admin and reference assets.
- `src/main/resources/db/migration/`: Flyway migrations; Hibernate validates schema.
- `src/test/java/com/astavet/shop/`: `domain/` and `service/` unit tests, `integration/` Spring/API/persistence/concurrency tests.

Read `harness/rules/workspace.md`, `testing.md`, `git-workflow.md`, `java-comment-rules.md`, `java.md`, `harness/references/codex-adaptation.md` and the effective profile before changes. Apply installed skills on demand. Preserve the no-comment rule. Keep entity usage inside repository adapters/JPA, service transaction boundaries and handwritten frontend/API contracts synchronized.

Run `./gradlew test bootJar`, `python3 harness/scripts/harness.py doctor`, `python3 harness/scripts/check_parity.py --installed`, and the harness secret scan. PostgreSQL integration tests use the `postgresTest` task with `TEST_DATABASE_URL`, `TEST_DATABASE_USERNAME` and `TEST_DATABASE_PASSWORD`.

Use the existing harness ledger/graph without bypassing it; it needs a committed base. Keep commits, pushes and publishing under explicit user authorization. Session-based guest carts require one application instance or sticky sessions. Environment credentials must stay outside Git.

## Java guidance

Canonical Java conventions are maintained in `../codex/harness/rules/java.md`;
source style is in `../codex/harness/rules/java-style.md`. Use Google two-space
indentation, 100 columns, explicit sorted imports, one statement per line and
required control-flow braces. Compatible Oracle practices and project exceptions
are recorded there. Run `./gradlew javaFormat` to apply layout and
`./gradlew javaStyleCheck` to verify it; `check` includes the read-only style gate.
The installed `spring-stack-patterns` and `java-backend-verification` entrypoints
reference the canonical workflows in `../codex/.agents/skills/`; load their references
only for the affected concern. Preserve Java 21, Boot 4.1.1, JPA/PostgreSQL/Flyway,
constructor injection, persistence-only entity usage, domain service contracts, session
security and the existing message error contract. The user-authorized structure uses
`entity/` for JPA mappings and `exception/` for ShopException/API advice; this project
map supersedes historical guidance preserving their old package paths. Domain models
and HTTP DTOs remain separate from entities; services/controllers must not import them.
