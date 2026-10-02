---
name: java-architecture-review
description: "Review Java package/module responsibilities, boundaries, dependency direction, and cycles. Use for a focused architecture assessment, not routine diff review or an unrequested restructuring."
---

# java-architecture-review

## Invocation prerequisites

Read applicable repository instructions (including nested instructions for affected files) and inspect existing changes before proceeding. Identify the actual target JDK, framework, build and test versions from targeted build files, wrappers, toolchains and existing reports. Distinguish declared, resolved and observed versions; mark unknowns and verify only what the task needs. Use matching primary documentation when behavior is uncertain. Do not assume Spring, JPA, Maven, Gradle or a database is present. Preserve unrelated changes; use no subagents unless explicitly requested. Do not install tools or upgrade dependencies automatically.

Resolve the actual path of this `SKILL.md`, following any discovery symlink. This collection stores skills at `<collection>/skills/<skill-name>/SKILL.md`; ascend from the skill directory through `skills` to obtain the collection root. Read [the shared rules README](../../rules/README.md) at `<collection>/rules/README.md` for authority/routing, then only the rule files selected below and affected source, callers, configuration or tests. Resolve relative references against this canonical skill file, not the discovery path or process working directory. References are not automatically loaded. If the collection is unavailable, report the missing path and proceed with verified project guidance while marking that limitation.

## Required inputs and targeted discovery

Requested subsystem/boundaries, architectural decisions or constraints, and review scope; discover established architecture before judging it.

Read `java-architecture.md`; add `spring-components-and-api.md` for actual scanning/DI boundaries and `java-build-and-dependencies.md` for module/source-set evidence only.

## Workflow and verification

1. Inspect applicable design decisions and targeted package/module dependencies, public APIs and representative use-case paths.
2. Compare actual dependencies with adopted rules. Trace cycles and boundary leakage with concrete edges; preserve appropriate domain behavior.
3. Apply the personal layered preference to new layered backends; report conflicts in established architectures without silently restructuring them.
4. Run existing scoped architecture checks if configured. Do not install ArchUnit or invent mandatory interfaces, architecture styles or size limits.

## Output and completion

Report findings by default. Provide responsibility/dependency evidence, impact, adopted-rule violations versus optional suggestions, existing exceptions and checks/limitations. Complete when requested boundaries are assessed and recommendations are scoped; refactoring requires a separate authorized scope. Report verified facts, assumptions, recommendations and unexecuted checks separately.
