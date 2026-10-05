---
name: implement-task
description: "Implement a scoped feature or bug fix and verify the changed behavior in the target repository."
---

# implement-task

Read [change-implementation](../change-implementation/SKILL.md) and [Codex adaptation](../../../harness/references/codex-adaptation.md) first. Its concrete plan/base approval, no-comment policy and frontend test-writing exception apply to this helper too; carry existing approval forward. Read the current task ledger, AGENTS.md, and project profile. Inspect the code
and affected callers before editing. For a bug, reproduce it or document the
observed failure and limits; add a regression check where it meaningfully proves
the fix. Keep unrelated working changes intact.

Set `phase ID implement` using `python3 harness/scripts/harness.py`. Apply the
change, then run `verify ID`. The profile provides argv commands. If verification
fails, fix the cause and rerun; never relabel a failing command as passing. Read
[workflow](../../../harness/references/workflow.md) for evidence semantics. Review
the final diff before readiness. Carry on to shipping only within the user's
existing authorization; otherwise leave a concrete handoff.

For the complete capability workflow, read [change-implementation](../change-implementation/SKILL.md).

For an initialized graph, record implement only after the scoped changes exist.
Record integrate when routed only after per-repo validation and actual producer/
consumer inspection; use workspace.py for aggregate checks. Test and review
node passes require current ledger reports. Follow
[graph](../../../harness/graph/README.md) rather than bypassing its dependencies.
