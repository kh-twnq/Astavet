# Java testing

## Language/API requirements

Test-engine discovery, lifecycle and outcome semantics depend on the installed runner. A successful build does not prove tests were discovered or integration checks executed.

## Upstream recommendations

JUnit Jupiter uses per-method test instances by default; other lifecycle choices require explicit state handling. Spring Boot supports focused slices and full-context tests. Testcontainers supports disposable real dependencies; shared versus per-test containers have different isolation/lifetime implications.

## User-selected conventions

Select unit, slice and integration tests by behavior. Use units for isolated logic, slices for relevant framework boundaries, and integration checks for real persistence, wiring, concurrency or external protocol behavior. Assert observable outcomes, invariants and failure behavior; avoid tests that merely duplicate implementation or verify incidental mocks. Add regression coverage when it meaningfully prevents recurrence.

Keep fixtures deterministic: control clocks, randomness, ordering and external dependencies where relevant. Isolate shared state and clean up owned resources. Verify the effective database target is disposable before destructive fixtures, including environment/profile overrides; never infer safety from a database name alone. If target safety is unknown, leave those checks unexecuted and report the blocker.

Report command, working directory, scope, counts where available and limitations. Distinguish **passed**, **failed**, **skipped/disabled**, **aborted** and **unexecuted** checks; map runner terminology without inventing counts. A skipped integration suite is not a pass. Unit tests alone are not evidence of HTTP/security or database flow coverage.

## Conditional framework rules

Use slices/clients/annotations matching the installed Spring stack and test version. Test transaction entry/rollback/commit behavior with the real relevant database where needed; an embedded substitute cannot prove vendor-specific locks or SQL. Discover Testcontainers runtime availability before proposing execution; do not install it automatically.

## Discover in the repository

Runner/engine versions, selectors/tags, CI commands, test source sets, reports, coverage expectations, fixture cleanup, database/runtime targets and known skips.

Sources: [Testing source records](SOURCES.md#java-testing).
