# Java / Spring Boot guidance

Read the root `AGENTS.md` and relevant ECC Java rules and skills.

- Use Java 21, the checked-in Gradle wrapper and Spring Boot 4-compatible APIs.
- Match existing four-space indentation and package organization.
- Keep controllers thin: controller → service → repository; architecture tests
  enforce the controller/repository boundary.
- Use constructor injection and records for DTOs; respect JPA entity lifecycle.
- Keep business transactions in services. Verify locking, idempotency and stock
  restoration with real PostgreSQL tests when modifying concurrency behavior.
- Follow existing exception mapping and validation; do not change response
  envelopes simply because upstream examples use a different format.
- Tests live under `src/test/java/com/astavet/`; use JUnit Jupiter, AssertJ and
  Mockito as appropriate. New database tests should follow the project's
  existing opt-in PostgreSQL pattern rather than require an unrelated database.
- Run `bash scripts/verify.sh backend` from the repo root. For PostgreSQL tests,
  use `bash scripts/verify.sh integration` with `ASTAVET_TEST_DATABASE_*` set.
  These tests delete data: use only a disposable test database.

Upstream `@MockBean`, Maven and Testcontainers samples are conceptual references;
verify replacements against the project's actual Boot version and test setup.
