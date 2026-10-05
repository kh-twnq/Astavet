# Rules — Java coding standards (write-time gate)

**Authority for how Java is written/changed in this workspace.** This replaces the former
`java-coding-standards` *skill* — the policy lives here (a rule), the *teaching* lives in skills
(`code-craft`, `spring-stack-patterns`, `test-authoring`), enforcement lives in `code-review` /
`security-review`, and the deterministic reminders live in the workspace guard hook +
`.codex/hooks.json`. Apply this whenever you write or modify `.java` code.

## Phase 0 — Branch prerequisite
Before modifying any Java, confirm you are on a permitted working branch per
[`git-workflow.md`](git-workflow.md). Branch creation and base selection are owned by
`$start-task` and are not repeated here.

## Phase 1 — Design (pick what applies)
| Change involves… | Read |
|------------------|------|
| Any new/edited class, method, naming, responsibilities, patterns | [code-craft](../../.agents/skills/code-craft/SKILL.md) |
| Spring components, JPA entities/queries, logging | [spring-stack-patterns](../../.agents/skills/spring-stack-patterns/SKILL.md) |
| Threads/async, hot paths, public REST endpoints, package structure | the matching lens of [code-review](../../.agents/skills/code-review/SKILL.md) |
| Package/module boundaries, a new abstraction, cross-domain dependency, a new module/service | [code-review/architecture.md](../../.agents/skills/code-review/architecture.md) — discovery-first review process + concrete anti-pattern/abstraction criteria, and [java-architecture-enforcement.md](../docs/rules/java-architecture-enforcement.md) for the ArchUnit-vs-Modulith decision framework and the rules that make layering checkable |
| A schema change touching an existing (non-empty) table, or anything in a migration repo | [migration.md](../docs/rules/migration.md) — mandatory staged pattern for adding `NOT NULL`, precondition guidance |

Match the naming/structure/idioms of surrounding code first; reach for a pattern only when it earns
its place (YAGNI). Do not propose Spring Modulith, Hexagonal/Clean Architecture, or a
package-by-feature rewrite without a concrete driver — see the decision framework in
`java-architecture-enforcement.md`.

## Phase 2 — While writing
- **Comment policy — NO COMMENTS.** Full directive in [`java-comment-rules.md`](java-comment-rules.md).
  Core directive (retained harness policy): **when writing or modifying code, do NOT add
  comments — in any language, ever.** Make the code self-explanatory via naming and extraction; put
  any real explanation in the Jira ticket / MR / ADR, never in the source.
  Quick checklist:
  - ❌ No line/block/trailing comments, in any language
  - ❌ No Javadoc/JSDoc/TSDoc/docstrings added for explanation
  - ❌ No commented-out code; no TODO/FIXME/XXX/HACK (track work in a PROJ ticket)
  - ✅ Rename/extract/restructure instead of annotating
  - ✅ Only permitted: required license headers, `@Override`/functional annotations, and load-bearing
       machine directives (`eslint-disable`, `# noqa`, `# type: ignore`, `@SuppressWarnings`, shebang)
  - ⚠️ This governs code you *write/modify* — don't bulk-strip other people's existing comments as a drive-by
  (Applies to all languages — Java, TS/React, Python, C/C++/Q#, config.)
- Keep methods small; respect single responsibility and dependency direction (code-craft).
- Strict layering: **JPA entities never leave the repository layer** (`workspace.md`).
- Structured, MDC-aware logging (spring-stack-patterns, logging section).

## Phase 3 — Tests (risk-based, mandatory)
Every change requires validation. The required level depends on what changed
([test-authoring](../../.agents/skills/test-authoring/SKILL.md)):

| Change type | Required tests |
|-------------|---------------|
| Domain or calculation logic | Unit tests |
| Repository, query, transaction, integration boundary | Integration tests |
| REST or event contract | Controller / contract integration tests |
| Bug fix | Regression test at the lowest reliable layer that fails before the fix |
| Config or build-only change | Targeted build or smoke verification |

Not every change needs both unit and integration tests. A DTO rename or log
enrichment does not justify a new integration test; a transaction boundary
change does not get away with only a unit test.

## Phase 4 — Self-review gate (before commit)
Run as a checklist on the diff:
1. [code-review](../../.agents/skills/code-review/SKILL.md) — correctness + project-rules; add concurrency /
   performance / api-contract / architecture lenses if the change touches them.
2. [security-review](../../.agents/skills/security-review/SKILL.md) — for input handling / queries / auth.
3. For any Java service here: don't treat a green CI pipeline as sufficient — see
   [quality-gates.md](../docs/rules/quality-gates.md). Where a repo's Sonar/Trivy quality gates are
   non-blocking or dead-ruled in CI, Phases 3–4 here are the actual safety net, not a formality on
   top of one.

Do not commit until Phases 3–4 pass; then hand commit wording to
[commit](../../.agents/skills/commit/SKILL.md). (`$ship-task` automates review→test→commit→MR.)

## Anti-patterns
❌ **Any** comment/docstring added to code (see `java-comment-rules.md`) · ❌ committing without the
Phase 3/4 gate · ❌ a pattern the surrounding code doesn't use · ❌ returning JPA entities from
services/controllers.
