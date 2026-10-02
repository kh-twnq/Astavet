---
name: java-feature-implementation
description: "Implement a requested Java feature or behavior fix within an existing or new Java module. Use for authorized coding work, not report-only reviews or standalone build diagnostics."
---

# java-feature-implementation

## Invocation prerequisites

Read applicable repository instructions (including nested instructions for affected files) and inspect existing changes before proceeding. Identify the actual target JDK, framework, build and test versions from targeted build files, wrappers, toolchains and existing reports. Distinguish declared, resolved and observed versions; mark unknowns and verify only what the task needs. Use matching primary documentation when behavior is uncertain. Do not assume Spring, JPA, Maven, Gradle or a database is present. Preserve unrelated changes; use no subagents unless explicitly requested. Do not install tools or upgrade dependencies automatically.

Resolve the actual path of this `SKILL.md`, following any discovery symlink. This collection stores skills at `<collection>/skills/<skill-name>/SKILL.md`; ascend from the skill directory through `skills` to obtain the collection root. Read [the shared rules README](../../rules/README.md) at `<collection>/rules/README.md` for authority/routing, then only the rule files selected below and affected source, callers, configuration or tests. Resolve relative references against this canonical skill file, not the discovery path or process working directory. References are not automatically loaded. If the collection is unavailable, report the missing path and proceed with verified project guidance while marking that limitation.

## Required inputs and targeted discovery

Requested behavior, acceptance criteria, affected module and compatibility constraints. Ask early if missing information changes architecture or user-visible behavior; continue independent discovery.

Read `java-core.md` and `java-testing.md`; select `java-architecture.md`, `java-style.md` and conditional Spring, persistence, security, build or concurrency groups only for affected behavior.

## Workflow and verification

1. Inspect affected code, contracts, callers and tests. Establish a short plan and completion criteria for complex work.
2. Implement the smallest coherent change in the requested scope using repository conventions. Keep appropriate domain behavior and public contracts explicit; synchronize affected contracts when required.
3. Add meaningful regression tests when warranted. Execute the verified checks appropriate to the changed behavior; verify disposable storage before destructive fixtures.
4. Review the final diff for accidental edits and acceptance-criteria coverage.

## Output and completion

May edit only within the requested feature/fix scope. Report changed behavior, affected files, exact check outcomes and blockers. Complete when acceptance criteria and required validation are satisfied, or identify the precise unresolved blocker without claiming completion. Report verified facts, assumptions, recommendations and unexecuted checks separately.
