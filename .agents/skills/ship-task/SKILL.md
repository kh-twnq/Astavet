---
name: ship-task
description: "Close an implemented task: review, secret scan, per-repo validation, verified commit, push/MR and authorized tracker/worklog updates."
---

# ship-task

Read [the Codex adaptation contract](../../../harness/references/codex-adaptation.md) before using this workflow.

Close an **already implemented** task by sequencing review, tests and publication.
Use [change-implementation](../change-implementation/SKILL.md) to implement a fix.
Read the task's approved base and project identity; publication steps require
user authorization already established for their scope.

## Steps

1. **Review the actual change.** Run [code-review](../code-review/SKILL.md) with
   correctness and project-rules lenses, including layering, every affected
   consumer, migration ownership and toolchain. For input/queries/auth, also run
   [security-review](../security-review/SKILL.md). Combine deep lenses in one
   permitted native deep-reviewer; resolve Blocker/Major findings before shipping.
2. **Validate each repo.** Run the effective profile's real test/lint/build argv
   with its configured toolchain. Backend bugs need regression tests; the frontend
   new-unit-test exception does not skip existing suites or behavior verification.
   For suspected baseline failures, reproduce on a clean base using an isolated
   worktree, not stash/pop in the user's dirty tree. Report exact outcomes.
3. **Run the secret scan.** For every touched repo:
   `python3 harness/scripts/harness.py --repo <repo-dir> scan`.
   A nonzero scan, failed test or unresolved Blocker/Major blocks commit/push/MR.
   Fix the problem and rerun the affected checks; do not relabel red evidence.
4. **Bind native evidence.** Stage only intended files before final `verify <ID>`.
   Record the actual `review <ID> --verdict pass|fail --summary "<evidence>"`.
   For initialized graphs, record integrate/test/review/security with actual
   evidence and honor routed dependencies. Run `ready <ID>` immediately before
   commit; any later edit/index change invalidates verification and review.
   See [ledger](../../../harness/references/workflow.md) and
   [graph](../../../harness/graph/README.md). Record every changed repo separately.
5. **Commit.** Use [commit](../commit/SKILL.md). The ticket key belongs in the
   footer. Confirm the working branch and staged scope; preserve unrelated work.
6. **Push and open the MR/PR.** Use the authenticated Git host adapter inside
   the target repo. Retained policy: title = short work description; description
   = `Task: <jira-base-url>/browse/<TICKET-KEY>` only. Target the **confirmed task
   base**, not an environment branch chosen by guessing. Environment promotion is
   a separate release operation. Capture the URL and actual pipeline status.
7. **Tag-based release only.** If this repo publishes by tags and the user
   authorized release tagging, inspect the repo's CI trigger regex and tag history.
   Preserve its version convention (for example `vX.Y.Z.devN` or `vX.Y.Z.preN`);
   increment the appropriate suffix for the confirmed dev/publish target. Create
   and push the release tag only after verifying the intended HEAD. A pushed
   branch/MR is not evidence of a deployed release.
8. **Record ship evidence.** After the checked commit and review URL exist, run
   `ship <ID> --url <URL>` per repo. This verifies the local committed snapshot;
   it does not open the MR, validate its remote state or mean merged/deployed.
9. **Transition the tracker.** With authorized integration, use valid transitions
   to the project's review/done state. Report unavailability without inventing status.
10. **Log work on the matching sub-task.** Retain `[FE]`/`Frontend` for UI and
    `[BE]`/`Backend` for services/migrations; mixed work logs each discipline on its
    own unit. Match implementation versus `Resolve feedback merge request` units;
    clarify ambiguous matches. If creation is authorized and no unit exists,
    create a labeled, estimated, self-assigned sub-task. Log **actual user-confirmed
    time**, using Jira duration format (`2h`, `1h 30m`), never the planning estimate.
11. **Report.** Changed repos/files, validation and review, commit/MR URLs, target,
    pipeline, tag/deploy evidence if checked, tracker state, sub-task worklog and
    remaining consumer follow-ups. Clearly distinguish local ship from remote merge.
