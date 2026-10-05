---
name: review-change
description: "Review the current change for concrete correctness, security, and verification gaps, and record a snapshot-bound verdict."
---

# review-change

Read [code-review](../code-review/SKILL.md) and [Codex adaptation](../../../harness/references/codex-adaptation.md). Apply the retained project rules and report independent-review limitations. Read the task objective and acceptance criteria. Inspect the diff, affected
callers, and verification evidence. Prioritize reproducible regressions and
missing acceptance behavior. Report each actionable finding with severity,
file/line, trigger, impact, and supporting evidence. State uncertainty explicitly.

Use an independent reviewer only when requested or justified and permitted by
the environment. Do not publish comments or edit code during a review-only
request. If the user also authorized fixes, resolve findings and verify again.
Record `review ID --verdict pass|fail --summary "..."` with the harness CLI only
after examining that snapshot. A passing review does not imply tests passed.
Read [workflow](../../../harness/references/workflow.md) for invalidation rules.

For the complete capability workflow, read [code-review](../code-review/SKILL.md).
