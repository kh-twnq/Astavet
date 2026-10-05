# AstaVet MVP

## Repository Map

This independent repository owns one Java 21 / Spring Boot 4.1 application, its PostgreSQL schema, and same-origin static storefront/admin interfaces. Build inside this repository with the Gradle wrapper. No frontend package manager is required.

- `src/main/java/com/astavet/shop/`: controller/v1, DTO, domain, service interfaces/impl, repository interfaces/impl and repository-local JPA entities.
- `src/main/resources/static/`: accessible storefront, cart, checkout, confirmation, admin and assets.
- `src/main/resources/db/migration/`: Flyway migrations; Hibernate validates schema.
- `src/test/`: domain, API, transaction and inventory concurrency tests.

Read `harness/rules/workspace.md`, `testing.md`, `git-workflow.md`, `java-comment-rules.md`, `java.md`, `harness/references/codex-adaptation.md` and the effective profile before changes. Apply installed skills on demand. Preserve the no-comment rule. Keep JPA entities inside repository packages, service transaction boundaries and handwritten frontend/API contracts synchronized.

Run `./gradlew test bootJar`, `python3 harness/scripts/harness.py doctor`, `python3 harness/scripts/check_parity.py --installed`, and the harness secret scan. PostgreSQL integration tests use the `postgresTest` task with `TEST_DATABASE_URL`, `TEST_DATABASE_USERNAME` and `TEST_DATABASE_PASSWORD`.

Use the existing harness ledger/graph without bypassing it; it needs a committed base. Keep commits, pushes and publishing under explicit user authorization. Session-based guest carts require one application instance or sticky sessions. Environment credentials must stay outside Git.
