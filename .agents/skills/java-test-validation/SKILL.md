---
name: java-test-validation
description: "Select and execute Java checks and assess behavioral regression coverage. Use for test validation or explicitly requested test authoring, not general diff review or build/toolchain diagnosis."
---

# java-test-validation

## Invocation prerequisites

Read applicable repository instructions (including nested instructions for affected files) and inspect existing changes before proceeding. Identify the actual target JDK, framework, build and test versions from targeted build files, wrappers, toolchains and existing reports. Distinguish declared, resolved and observed versions; mark unknowns and verify only what the task needs. Use matching primary documentation when behavior is uncertain. Do not assume Spring, JPA, Maven, Gradle or a database is present. Preserve unrelated changes; use no subagents unless explicitly requested. Do not install tools or upgrade dependencies automatically.

Resolve the actual path of this `SKILL.md`, following any discovery symlink. This collection stores skills at `<collection>/skills/<skill-name>/SKILL.md`; ascend from the skill directory through `skills` to obtain the collection root. Read [the shared rules README](../../rules/README.md) at `<collection>/rules/README.md` for authority/routing, then only the rule files selected below and affected source, callers, configuration or tests. Resolve relative references against this canonical skill file, not the discovery path or process working directory. References are not automatically loaded. If the collection is unavailable, report the missing path and proceed with verified project guidance while marking that limitation.

## Required inputs and targeted discovery

Behavior/change under test, affected modules, requested validation or authoring scope, and available safe test environments.

Read `java-testing.md`; add `java-core.md` for contracts under test, `java-build-and-dependencies.md` for selecting commands, and specialized groups only for behavior under test.

## Workflow and verification

1. Map acceptance criteria to existing tests, test layers and missing observable assertions. Inspect runner/engine versions, selectors, source sets, CI and reports.
2. Verify environment/fixtures and effective disposable database targets before any destructive setup. Select commands from actual project configuration.
3. Run relevant authorized checks and inspect results/discovery counts; distinguish passed, failed, skipped, aborted and unexecuted checks.
4. If test authoring is explicitly requested, add focused deterministic coverage in that scope and run it; do not alter production behavior merely to make tests pass.

## Output and completion

Validation is report-only for source by default, while running safe requested tests is allowed. Test authoring edits require requested scope. Report coverage mapping, commands/directories, results and gaps. Complete when selected checks are accounted for and limitations are explicit; classify infrastructure failures without silently switching to build fixes. Report verified facts, assumptions, recommendations and unexecuted checks separately.
