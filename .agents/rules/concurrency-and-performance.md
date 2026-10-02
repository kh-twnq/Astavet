# Concurrency and performance

## Language/API requirements

Shared-state visibility follows the Java Memory Model and happens-before relationships. `volatile` does not make compound read-modify-write operations atomic. Concurrent containers do not automatically make a multi-step business operation atomic. Respect each API's publication, locking and iteration contracts.

Interruption is cooperative; blocking methods may clear the interrupt status when throwing `InterruptedException`. Propagate it, or restore the flag when translating/handling it at a boundary that cannot propagate. Cancellation requests and timeouts do not universally stop underlying work. `shutdown` rejects new tasks but does not wait for termination; discover executor-specific shutdown/close behavior.

## Upstream recommendations

Use suitable standard concurrency primitives and explicit executor ownership/lifecycle. Virtual threads suit many blocking tasks and are not faster CPU execution. Use per-task virtual threads rather than pooling them; constrain scarce resources separately. Their standard API is available from JDK 21. Pinning behavior depends on JDK version: avoid transferring JDK 21 monitor-pinning advice unchanged to newer JDKs; the consulted JDK 25 guide describes native/foreign-call pinning. Check the actual runtime's documentation before replacing synchronization.

## User-selected conventions

Trace shared state, lock ordering, blocking calls, queues and executor capacity before changing behavior. Specify cancellation, deadlines, retries and cleanup from requirements. Do not close borrowed/container-managed executors. Measure before optimizing; report workload, baseline, metrics and uncertainty. Use observed traces, profiles/query counts or reproducible experiments instead of speculative throughput/latency claims. Coordinate concurrency tests with barriers/latches and bounded waits, not arbitrary sleeps. Repeated success does not prove absence of races.

## Conditional framework rules

Spring singleton beans require appropriate shared-state discipline; async/security/transaction context propagation is framework-dependent. Reactive event-loop work has different blocking constraints. Check driver/framework virtual-thread support before adoption.

## Discover in the repository

JDK/runtime, concurrency model, executors/ownership, shared mutable state, locks, deadlines, cancellation propagation, context boundaries, workload evidence and allowed diagnostic targets.

Sources: [Concurrency source records](SOURCES.md#concurrency-and-performance).
