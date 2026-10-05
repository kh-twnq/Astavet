---
name: code-review
description: "Review scoped changes using correctness, project, standards, concurrency, performance, API and architecture lenses."
---

# Code Review

Read [the Codex adaptation contract](../../../harness/references/codex-adaptation.md) before using this workflow.

One review capability, scoped to what the change needs. Default target is the **working diff**;
review a package or branch when asked. Read-only — cite findings at `file:line`, rank
Blocker / Major / Minor / Nit, and distinguish a real rule violation from a style preference.

## Pick the lens(es) — load only what applies
| Scope | When | Depth reference |
|-------|------|-----------------|
| **correctness** (default) | any code change | [correctness.md](correctness.md) — null safety, exceptions, edge cases |
| **standards** | Java changed | [standards.md](standards.md) — coding-standards gate compliance (see `../../../harness/rules/java.md`) |
| **project-rules** | reviewing a diff before commit/MR | [project-rules.md](project-rules.md) — layering, cross-repo contract sync, migration-repo placement, toolchain, secrets |
| **concurrency** | threads / async / shared state touched | [concurrency.md](concurrency.md) |
| **performance** | hot paths, collections, streams, boxing | [performance.md](performance.md) |
| **api-contract** | public REST endpoint added/changed | [api-contract.md](api-contract.md) |
| **architecture** | package/module/dependency-direction questions | [architecture.md](architecture.md) |

For **security** (OWASP, injection, secrets, auth) use the separate [security-review](../security-review/SKILL.md) skill.

## Reasoning and delegation

Follow [model routing](../../../harness/docs/rules/model-routing.md). Routine correctness,
standards, project-rules and API-contract lenses can run inline. A concurrency or
architecture lens, a diff spanning ≥2 repos or >10 meaningfully changed files,
or an auth/JWT/rate-limit change requires a deep-reasoning review.

When delegation is available and permitted, use one native `deep-reviewer` role
([role config](../../../.codex/agents/deep-reviewer.toml)) carrying all applicable deep
lenses, including the security checklist for the same diff. Supply objective,
base/HEAD, relevant code/diff, rules and graph/rg evidence. Do not assume that the
worker has the orchestrator's MCP access. Await the result and verify findings
before combining them into one ranked table. Otherwise perform the deep pass
inline and report the limitation; never invent an independent review.

## Default flow (review a diff before MR)
1. **Detect scope** — `git status` / `git diff` (or `git diff <base>...`) in the changed repo(s);
   identify which repo(s) and what changed. Read the project profile and the touched repo's
   [`../../../harness/rules/`](../../../harness/rules/) files. Size the blast radius with the code-graph tooling if
   available (graph impact of the working diff + impact on changed public symbols, and route/shape
   checks when a route changed).
2. **Run the relevant lenses** from the table — always correctness + project-rules on a diff; add
   concurrency/performance/api-contract/architecture only if the change touches them.
3. **Cross-tier contract sync** — when no codegen exists, a DTO or route change in one service with
   no matching consumer edit is a finding (see project-rules.md). Use a route↔consumer registry
   first if the project maintains one; anything outside it stays a manual check, and a clean
   single-repo impact query is never proof of cross-tier safety.
4. **Rank findings** at `file:line` with a concrete fix; mark uncertain ones *Unknown / needs confirmation*.

## Output
```
## Review — <repo>(s), <N> files, scopes: <correctness, project-rules, …>
Gate: PASS | CHANGES REQUESTED
| Severity | file:line | Issue | Lens | Fix |
```

## Rules
- Read-only; never invent issues — cite a rule/lens + `file:line`.
- **Ground structural claims in the graph** — any finding that asserts a caller/consumer/dependency
  relationship ("nothing else calls X", "this breaks Y", "Z is the only consumer") must carry
  evidence from a code-graph query (impact/callers/trace, or a cross-repo group query where the
  tooling supports it) or an explicit grep + read, confirmed at `file:line`. Without that evidence,
  report it as *Unknown / needs confirmation*, not as a finding.
- One repo at a time; match each repo's configured JDK/toolchain. Route DB changes to the correct
  migration repo when the project splits them.
- Defer security depth to [security-review](../security-review/SKILL.md); deep standards policy lives
  in [`../../../harness/rules/java.md`](../../../harness/rules/java.md).
