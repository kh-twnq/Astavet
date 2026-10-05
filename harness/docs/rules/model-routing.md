# Reasoning and role routing

The Claude source routes work to Haiku/Sonnet/Opus. Those are not valid Codex
model aliases. Preserve the division of responsibility and reasoning depth;
inherit the user's selected Codex model unless the user explicitly configures
another available model. Native roles live in `.codex/agents/*.toml`.

## Routing table

| Work | Codex execution requirement |
| --- | --- |
| `$briefing`, `$start-task`, `$ship-task`, `$review-mr`, `$handoff` | Orchestrate the workflow; use the specialist passes below when applicable. |
| `task-scoping` | Read-only repository, toolchain and impact mapping. |
| `solution-planning` | Mandatory deep-reasoning design and estimate before branching/implementation; reuse an evidenced parent plan for a sub-task. |
| `change-implementation`, steps 1–4 | Mandatory think-first pass for ticket and ad hoc work; read code/rules and identify concrete changes before editing. |
| `change-implementation`, steps 6–8 | Execute the approved plan, run checks, summarize evidence. |
| `bug-investigation` | Deep reasoning; establish a feedback loop, verify the cause and alternatives. |
| `code-review`, routine lenses | Inline correctness, standards, project-rules and API-contract review. |
| `code-review`, deep lenses | `deep-reviewer` for concurrency/architecture, ≥2 repos, >10 meaningfully changed files or auth/JWT/rate-limit changes, when delegation is available and permitted. |
| `security-review` | Always a deep review; combine with the same diff's other deep lenses in one reviewer. |
| `completion-audit`, per-ticket evidence | Bounded read-only workers; complete all results before conflict synthesis. |
| `completion-audit`, cross-ticket synthesis | Deep reasoning over contracts, shared symbols, migration/deployment order and acceptance gaps. |
| `commit` | Inline message drafting; a worker solely for a small message is unnecessary. |
| `changelog` | Inline for a small range; `drafter` for bulk drafting when delegation is permitted. |
| `release-note`, `mr-feedback`, `merge-conflict-resolution` | Execute the scoped workflow; deepen reasoning for conflicting intent, auth or cross-repo contracts. |
| `grilling` | Interview at the session model, one decision at a time. |
| `code-craft`, `spring-stack-patterns`, `test-authoring` | Load relevant references in the current task. |
| `engineering-advisor` | Manual scarce advice after deep review at a genuine decision boundary. |

## Native roles

`deep-reviewer`, `reviewer`, `investigator`, `drafter` and `engineering-advisor`
inherit the selected model. Deep-review roles configure high reasoning effort;
that is a reasoning setting, not an automatic upgrade to another model.
Read-only sandbox settings constrain filesystem writes; the orchestrator must
also enforce no ticket mutations, posting, branch switching or publication.

Use active-client delegation tools, never Claude `Agent(model: "opus")` or a
fictional `Workflow` API. Honor system/user limits on delegation. If delegation
is unavailable, perform the required pass inline and identify that limitation.
Do not claim worker independence or model routing that did not actually occur.

## Escalation and cost

Deepen reasoning for contradictory evidence after two hypotheses, cross-repo
contracts, auth/JWT/rate-limit surfaces, concurrency or multi-database migrations.
Mechanical summaries can use the drafter role; small work stays inline. The
think-before-code pass remains mandatory regardless of task size. Do not change
the user's model solely to imitate the source tier table.

The executor verifies findings, owns the final recommendation and respects user
approval. The advisor's recommendation is neither authorization nor a final verdict.
