# Persistence and transactions

## Language/API requirements

Java alone provides no database transaction guarantee. Discover the storage API, database, transaction manager and durability/isolation contracts before reasoning about consistency.

## Upstream recommendations

Spring Data JPA recommends transaction boundaries at the unit of work. Its `readOnly` flag is a hint with provider/driver effects; it is not a portable safeguard against writes. Query methods support fetch/load graphs; lock metadata is available when required. Neither feature warrants automatic use.

## User-selected conventions

Keep transactions aligned with business atomicity and appropriately short. Investigate lazy access, actual SQL/query count, N+1, fetch plans and pagination using representative data. Do not fix N+1 by making every association eager. Verify stable page ordering and fetch/pagination behavior. Put uniqueness and integrity rules in appropriate database constraints; use the repository's migration process, preserving applied migrations. Choose optimistic/pessimistic locking, retry policy and idempotency only from actual contention, duplicate-delivery and consistency requirements. An in-process lock does not protect multiple application instances.

## Conditional framework rules

For Spring's default proxy transaction mode, only calls through the proxy are intercepted; self-invocation does not activate an annotated inner transaction. Verify visibility/proxy mode and lifecycle calls. Default rollback covers unchecked exceptions and errors, not checked exceptions; inspect explicit rollback rules and global overrides before relying on this. Caught errors may change rollback behavior. Check propagation, isolation, timeout and transaction manager; do not assume transactions cross async thread boundaries.

For JPA, inspect entity identity, lazy-loading boundaries, flushing and query behavior. Test commit-time constraints and rollback through actual framework entry points. For other stores, use their own contracts instead of JPA assumptions.

## Discover in the repository

Store/database/provider versions, schemas, migrations, OSIV/session lifetime, transaction configuration, call paths, queries/pages, constraints, lock order/timeouts, idempotency contract and disposable integration-test targets.

Sources: [Persistence source records](SOURCES.md#persistence-and-transactions); safe fixtures: [java-testing.md](java-testing.md).
