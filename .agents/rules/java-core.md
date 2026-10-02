# Java core

## Language/API requirements

- Respect `equals` reflexivity, symmetry, transitivity, consistency and null behavior. Equal objects must have equal `hashCode`; override them together when providing value equality. Keep equality/hash inputs stable while objects are hash keys; inspect comparator consistency for sorted collections.
- Handle or declare checked exceptions. Respect `AutoCloseable` contracts and the suppressed-exception behavior of try-with-resources.
- Check syntax and API availability against the target release and preview policy; compilation on a newer JDK alone does not establish older-runtime compatibility.
- Exact decimal division can fail for nonterminating results. Specify precision, scale and rounding from the business contract. `BigDecimal.equals` includes scale whereas `compareTo` compares numeric value.

## Upstream recommendations

Keep state private and invariants explicit. Prefer immutable values where suitable; `final` references and records do not deeply freeze mutable members. Copy mutable inputs/outputs when ownership requires it. Describe nullability and distinguish absent, empty and invalid values. Choose `Instant` for timeline events, `LocalDate` for dates, and offset/zoned types when that information is required; a `LocalDateTime` has no offset or zone.

## User-selected conventions

Catch exceptions where recovery, translation or boundary reporting is meaningful; preserve causes when translating, and avoid swallowing failures or logging the same failure at every layer. Use try-with-resources for resources owned by the current scope, not borrowed resources. Do not force `Optional` onto every field/parameter or convert every object to a record. For decimal business quantities use appropriate exact representation, explicit units and rounding; integer minor units may be appropriate if range and currency rules support them. Avoid creating decimal values from binary floating-point approximations when exact decimal input is intended.

## Conditional framework rules

ORM identity, proxy equality, constructor restrictions, serialization and nullability annotations need the installed framework's contracts; do not apply plain value-object assumptions blindly to entities.

## Discover in the repository

JDK/toolchain, compiler release, nullability annotations, public API compatibility, error taxonomy, resource ownership, money units, precision, clock/zone conventions, collection types and entity identity strategy.

Sources: [Core source records](SOURCES.md#java-core); supporting concurrency contracts are in [concurrency-and-performance.md](concurrency-and-performance.md).
