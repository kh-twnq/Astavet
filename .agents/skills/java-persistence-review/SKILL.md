---
name: java-persistence-review
description: "Review Java storage, query, transaction, migration, locking, and idempotency behavior. Use for a focused persistence assessment; exclude projects without relevant storage and general diff reviews."
---

# java-persistence-review

## Invocation prerequisites

Read applicable repository instructions (including nested instructions for affected files) and inspect existing changes before proceeding. Identify the actual target JDK, framework, build and test versions from targeted build files, wrappers, toolchains and existing reports. Distinguish declared, resolved and observed versions; mark unknowns and verify only what the task needs. Use matching primary documentation when behavior is uncertain. Do not assume Spring, JPA, Maven, Gradle or a database is present. Preserve unrelated changes; use no subagents unless explicitly requested. Do not install tools or upgrade dependencies automatically.

Resolve the actual path of this `SKILL.md`, following any discovery symlink. This collection stores skills at `<collection>/skills/<skill-name>/SKILL.md`; ascend from the skill directory through `skills` to obtain the collection root. Read [the shared rules README](../../rules/README.md) at `<collection>/rules/README.md` for authority/routing, then only the rule files selected below and affected source, callers, configuration or tests. Resolve relative references against this canonical skill file, not the discovery path or process working directory. References are not automatically loaded. If the collection is unavailable, report the missing path and proceed with verified project guidance while marking that limitation.

## Required inputs and targeted discovery

Storage workflow/change, consistency and duplicate-delivery requirements, relevant schema/query scope, and safe integration environment if execution is requested.

Read `persistence-and-transactions.md`; add `java-core.md` for entity/value contracts, `java-testing.md` before tests and `concurrency-and-performance.md` for relevant races.

## Workflow and verification

1. Identify the actual store, database/provider versions, transaction manager and call paths; do not assume JPA or relational storage.
2. Trace unit-of-work boundaries, rollback/commit, proxy calls where applicable, lazy access, SQL/query counts, page behavior and constraints/migrations.
3. Evaluate locks, retries and idempotency against real consistency requirements. Distinguish in-process from cross-instance guarantees.
4. Use existing relevant integration checks only after proving effective targets are disposable. Report query/locking hypotheses as unverified until observed.

## Output and completion

Report findings by default; no automatic schema, code or database changes. Provide concrete data-integrity/query evidence, failure scenario, impact and scoped recommendation, plus check outcomes. Complete when the requested storage path is assessed and unsafe or unavailable checks are disclosed. Report verified facts, assumptions, recommendations and unexecuted checks separately.
