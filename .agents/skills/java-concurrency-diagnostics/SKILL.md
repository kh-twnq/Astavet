---
name: java-concurrency-diagnostics
description: "Investigate concurrent Java execution, races, deadlocks, executor saturation, cancellation, or latency supported by evidence. Exclude routine persistence reviews and speculative optimization requests without a diagnostic scope."
---

# java-concurrency-diagnostics

## Invocation prerequisites

Read applicable repository instructions (including nested instructions for affected files) and inspect existing changes before proceeding. Identify the actual target JDK, framework, build and test versions from targeted build files, wrappers, toolchains and existing reports. Distinguish declared, resolved and observed versions; mark unknowns and verify only what the task needs. Use matching primary documentation when behavior is uncertain. Do not assume Spring, JPA, Maven, Gradle or a database is present. Preserve unrelated changes; use no subagents unless explicitly requested. Do not install tools or upgrade dependencies automatically.

Resolve the actual path of this `SKILL.md`, following any discovery symlink. This collection stores skills at `<collection>/skills/<skill-name>/SKILL.md`; ascend from the skill directory through `skills` to obtain the collection root. Read [the shared rules README](../../rules/README.md) at `<collection>/rules/README.md` for authority/routing, then only the rule files selected below and affected source, callers, configuration or tests. Resolve relative references against this canonical skill file, not the discovery path or process working directory. References are not automatically loaded. If the collection is unavailable, report the missing path and proceed with verified project guidance while marking that limitation.

## Required inputs and targeted discovery

Observed symptom, expected behavior, workload/timing context, relevant concurrent path and available sanitized traces or reproduction constraints.

Read `concurrency-and-performance.md`; add core, Spring or persistence groups only for implicated state/context, and `java-testing.md` before diagnostic tests.

## Workflow and verification

1. Identify JDK/runtime and execution model; inspect relevant shared state, publication, lock ordering, executor ownership/capacity and blocking calls.
2. Trace interruption, cancellation, deadlines and context propagation. Use version-matched virtual-thread guidance if relevant.
3. Form falsifiable hypotheses from observed traces, queue/latency metrics or existing profiles. Request missing evidence when it changes the diagnosis.
4. Use safe bounded reproductions with deterministic coordination or authorized profiling. Do not attach to production or change synchronization/pools speculatively.

## Output and completion

Report findings by default; edits need requested scope. State evidence-backed cause or ranked hypotheses, affected state/threads, reproduction/check outcomes and scoped next action. Complete when the symptom is explained or the precise evidence gap is established; measurements must include workload and limitations. Report verified facts, assumptions, recommendations and unexecuted checks separately.
