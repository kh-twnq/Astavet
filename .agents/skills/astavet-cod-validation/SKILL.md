---
name: astavet-cod-validation
description: Review and validate AstaVet COD order-flow changes involving creation, idempotency, inventory, cancellation, delivery status, or payment collection/refunds. Use for scoped order-flow reviews and regression validation, not unrelated Java reviews, UI-only changes, documentation edits, or admin authentication work.
---

# AstaVet COD validation

## Scope and inputs

Default to review and validation. Modify application code or tests only when requested. Invocation does not authorize dependency installation, database provisioning, deployment, or unrelated refactoring.

Required input: the relevant diff/base revision, file list, or order-flow scenario. If scope cannot be inferred from the request and working-tree changes, ask before reviewing unrelated code. Database execution also requires resolved connection configuration and evidence that its target is a dedicated disposable test database. Never expose credentials or customer data.

## Workflow

1. Locate the target AstaVet application repository root, not this AI collection's Git root, and read its `AGENTS.md`. If the target application cannot be identified from the request or working directory, ask for its path. Consult `README.md`, especially “Kiểm tra” and “Quy tắc đơn hàng”, for commands and business requirements. Paths below are relative to the root; recheck current files rather than treating this skill as a frozen specification.
2. Review only the relevant diff and affected order-flow code. Follow changed callers/dependencies as needed; inspect controllers, DTOs, mappers, frontend callers, and migrations only when affected. Do not expand into a repository-wide audit.
3. Separate documented requirements, implemented behavior, and test evidence using the source map below. Report conflicts explicitly. Refer to README instead of copying its business rules; do not infer undocumented requirements such as automatic restocking on returns.
4. Map changed behavior to existing assertions. Inspect transaction boundaries, idempotency request comparison, order/variant locking, stock updates/rollback, and payment history where relevant. Distinguish mocked assertions from PostgreSQL and HTTP evidence.
5. Select checks using `AGENTS.md` and `backend/build.gradle`; apply the database safety gate before any command that can execute integration fixtures. Honor review-only/no-execution requests. Continue source review if execution is blocked.
6. Inspect fresh test results, skipped tests, and Gradle task status. Cached/up-to-date tasks are not newly executed tests. Suggest missing regression tests without adding them unless requested.

## Source and coverage map

Under `backend/src/main/java/com/astavet/`:

- `service/order/OrderService.java`: implemented totals/reservation, idempotency comparison, cancellation stock release, and transactional order/payment updates.
- `entity/order/OrderStatus.java` and `entity/order/CustomerOrder.java`: implemented transitions and history updates. Compare these with README requirements; implementation alone does not establish product intent.
- `repository/order/OrderRepository.java`: PostgreSQL advisory transaction lock for idempotency and pessimistic order lock. `repository/product/ProductVariantRepository.java`: pessimistic variant locks ordered by ID. H2 cannot verify PostgreSQL advisory locks.
- When persistence changes, inspect relevant `backend/src/main/resources/db/migration/` files and `application.yml`; Flyway is enabled and Hibernate validates the schema.

Under `backend/src/test/java/com/astavet/`:

- `service/order/OrderServiceTest.java`: mocked totals/reservation and conflicting customer data for a reused idempotency key.
- `entity/order/OrderStatusTest.java`, `entity/order/CustomerOrderTest.java`, `entity/product/ProductVariantTest.java`: unit evidence for transitions, COD collection/refunds, payment history, and stock operations; no PostgreSQL required.
- `service/order/OrderCancellationConcurrencyIntegrationTest.java`: PostgreSQL evidence for concurrent cancellation restoring inventory once, not concurrent checkout/idempotency.
- `service/product/ProductManagementIntegrationTest.java`: PostgreSQL product reconciliation; review only if affected, but account for its destructive fixture when selecting tests.

Current tests do not establish full HTTP/security/E2E correctness, concurrent checkout/idempotency correctness, or every negative branch. Recheck assertions before naming a coverage gap; untested behavior is not an established invariant.

## Commands and database safety gate

README documents these commands, both run from `backend/`:

```sh
./gradlew test
./gradlew test --tests '*IntegrationTest'
```

`backend/build.gradle` uses JUnit Platform without integration exclusions or a separate integration task. The normal command selects unit and integration tests. Both integration classes use `@EnabledIfEnvironmentVariable(named = "ASTAVET_TEST_DATABASE_URL", matches = ".+")`: absent/empty URL skips them; nonempty URL enables them. The integration filter selects both integration classes, excludes unit classes, and does not bypass the condition.

Both classes use `@DynamicPropertySource` to resolve:

- URL from `ASTAVET_TEST_DATABASE_URL`.
- Username from `ASTAVET_TEST_DATABASE_USERNAME`, falling back to JVM `user.name` when absent.
- Password from `ASTAVET_TEST_DATABASE_PASSWORD`, falling back to an empty string when absent.

These override the application's ordinary `DATABASE_*` configuration. Refer to README for the example invocation; example credentials or a test-looking name do not prove disposability.

Before either command can execute enabled integration tests:

1. Inspect selected test classes and resolve effective JDBC host, port, database, username, and configuration overrides for the actual execution environment. Keep the password private.
2. Establish through concrete provisioning/configuration evidence or explicit user confirmation tied to that exact target that it is dedicated to disposable tests and contains no data to retain. A `test` substring, localhost, or a successful connection alone is insufficient.
3. Account for Flyway schema changes and both fixtures' `@BeforeEach` calls to `orderRepository.deleteAll()` and `productRepository.deleteAll()`.
4. If target identity or disposability cannot be established, do not run these fixtures, including through the normal command; report the blocker. If unit validation is authorized, run the normal command with `ASTAVET_TEST_DATABASE_URL` explicitly absent from that process environment and report integrations as skipped. Never silently rely on inherited environment state.

Missing safe PostgreSQL execution leaves persistence/locking validation incomplete. Never report skipped tests as passed. Do not provision databases or install dependencies merely to clear a blocker without authorization.

## Output

Report concisely in the user's requested language:

- Scope and findings ordered by severity, with file/line references, affected behavior, and evidence distinguishing documented intent from implementation.
- Executed checks: working directory, exact command, observed results, and whether tests ran or were cached/up-to-date.
- Skipped/not-run checks and blockers, including database safety evidence or its absence, without credentials.
- Relevant coverage gaps and recommended follow-up, separating source reasoning from runtime verification. If no findings are supported, state that without claiming complete coverage.
