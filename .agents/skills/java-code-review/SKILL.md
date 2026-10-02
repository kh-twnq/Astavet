---
name: java-code-review
description: "Review a requested Java diff for actionable defects and regressions. Use for general code review, not a dedicated architecture, persistence, security, concurrency, test-validation, or build-failure investigation."
---

# java-code-review

## Invocation prerequisites

Read applicable repository instructions (including nested instructions for affected files) and inspect existing changes before proceeding. Identify the actual target JDK, framework, build and test versions from targeted build files, wrappers, toolchains and existing reports. Distinguish declared, resolved and observed versions; mark unknowns and verify only what the task needs. Use matching primary documentation when behavior is uncertain. Do not assume Spring, JPA, Maven, Gradle or a database is present. Preserve unrelated changes; use no subagents unless explicitly requested. Do not install tools or upgrade dependencies automatically.

Resolve the actual path of this `SKILL.md`, following any discovery symlink. This collection stores skills at `<collection>/skills/<skill-name>/SKILL.md`; ascend from the skill directory through `skills` to obtain the collection root. Read [the shared rules README](../../rules/README.md) at `<collection>/rules/README.md` for authority/routing, then only the rule files selected below and affected source, callers, configuration or tests. Resolve relative references against this canonical skill file, not the discovery path or process working directory. References are not automatically loaded. If the collection is unavailable, report the missing path and proceed with verified project guidance while marking that limitation.

## Required inputs and targeted discovery

The requested diff/base revision or file scope, expected behavior, and relevant acceptance criteria. Resolve an ambiguous base before treating unrelated work as part of the review.

Read `java-core.md`; add `java-style.md` only for relevant adopted conventions, and the architecture, Spring, persistence, security or concurrency group only when the diff touches that behavior. Read `java-testing.md` before executing checks.

## Workflow and verification

1. Inspect status and the requested diff, then only changed code, relevant callers/contracts and existing tests.
2. Trace concrete regression paths and edge cases. Separate introduced defects from pre-existing issues; optional preferences are not bugs.
3. Verify important hypotheses with targeted source inspection or existing non-destructive checks. Explain missing regression coverage without expanding into a full test audit.

## Output and completion

Report findings by default; do not edit unless requested. Rank findings by impact with file/line evidence, triggering conditions and consequence. State no actionable findings when justified, plus residual uncertainty and checks executed. Complete when the requested diff and material affected behavior are assessed. Report verified facts, assumptions, recommendations and unexecuted checks separately.
