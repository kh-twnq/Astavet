---
name: java-build-diagnostics
description: "Diagnose Java build, toolchain, plugin, or dependency-resolution failures from project configuration and failure evidence. Do not use for ordinary feature work, behavioral test failures, or general code review."
---

# java-build-diagnostics

## Invocation prerequisites

Read applicable repository instructions (including nested instructions for affected files) and inspect existing changes before proceeding. Identify the actual target JDK, framework, build and test versions from targeted build files, wrappers, toolchains and existing reports. Distinguish declared, resolved and observed versions; mark unknowns and verify only what the task needs. Use matching primary documentation when behavior is uncertain. Do not assume Spring, JPA, Maven, Gradle or a database is present. Preserve unrelated changes; use no subagents unless explicitly requested. Do not install tools or upgrade dependencies automatically.

Resolve the actual path of this `SKILL.md`, following any discovery symlink. This collection stores skills at `<collection>/skills/<skill-name>/SKILL.md`; ascend from the skill directory through `skills` to obtain the collection root. Read [the shared rules README](../../rules/README.md) at `<collection>/rules/README.md` for authority/routing, then only the rule files selected below and affected source, callers, configuration or tests. Resolve relative references against this canonical skill file, not the discovery path or process working directory. References are not automatically loaded. If the collection is unavailable, report the missing path and proceed with verified project guidance while marking that limitation.

## Required inputs and targeted discovery

Failing command and working directory, sanitized failure output, relevant module and reproduction constraints; discover missing command details from CI/build files when possible.

Read `java-build-and-dependencies.md`; add `java-core.md` for language/API incompatibilities and `java-testing.md` for test-discovery failures only.

## Workflow and verification

1. Inspect the build root, wrappers, module/profile/task configuration and first causal error. Distinguish declared versions from runtime/resolved versions without printing sensitive settings.
2. Compare build-runtime and compiler/test toolchain compatibility using matching primary docs. Inspect only relevant dependency paths or plugin bindings.
3. Reproduce with the smallest verified non-destructive command when safe. Do not install tools, upgrade dependencies, delete caches or upload scans automatically.
4. Identify a supported remedy and verify the hypothesis with available configuration or command evidence.

## Output and completion

Report findings by default; edit configuration only when requested. Report cause and evidence, reproduction/check outcomes, proposed scoped remedy and remaining uncertainty. Complete when the cause is demonstrated or competing hypotheses and missing evidence are clearly bounded. Report verified facts, assumptions, recommendations and unexecuted checks separately.
