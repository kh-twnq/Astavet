---
name: start-task
description: "Open a ticket or task: scope, perform mandatory solution planning, confirm the base, update tracker status and create the working branch."
---

# start-task

Read [the Codex adaptation contract](../../../harness/references/codex-adaptation.md) before using this workflow.

Opening lifecycle only: prepare the task for implementation. Source edits belong
to [change-implementation](../change-implementation/SKILL.md). Read project identity
from `harness/profiles/default.md` and the effective JSON profile.

## Steps

1. **Capture the ticket.** Fetch description, comments and sub-tasks through the
   available tracker integration, or use pasted text. Reconcile later agreed
   scope with the description and list unresolved questions. If no ticket exists,
   prepare a new Story/Bug only within the user's tracker authorization.
2. **Resolve the unit.** A supplied sub-task is the unit. For a parent Story/Bug,
   match the discipline `[BE]`/`[FE]` and target repo to the next implementation
   sub-task; ask if multiple or no matches are plausible. Retain parent and unit IDs.
3. **Scope it.** Run [task-scoping](../task-scoping/SKILL.md): target repositories,
   real files/layers, per-repo toolchain, consumers, auth and database ownership.
4. **Think before branching.** Run [solution-planning](../solution-planning/SKILL.md)
   with a deep-reasoning pass before branch creation or code edits. This is mandatory.
   The source exception is a sub-task whose parent already has a plan: read and
   reuse that plan, record evidence, and inherit the estimate instead of re-planning.
   A sub-task label alone is not evidence that its parent was planned. If planning
   creates sub-tasks, re-resolve the unit before transitioning it.
5. **Confirm the base.** Propose the base from the task/release model and wait
   for confirmation unless the user already supplied it. Do not silently select
   a long-lived branch. Propose `feature|bugfix/<user>/<TICKET-KEY>-<short-desc>`.
6. **Transition the units.** With authorized tracker access, resolve valid
   transitions and move both parent and the selected sub-task to In Progress.
   If unavailable, retain a local draft and report the missing integration.
7. **Create the branch inside each target repo.** Preserve unrelated changes;
   do not stash or switch unrecognized work. Match the approved base and name.
8. **Record native state.** Initialize each repo's ledger with
   `python3 harness/scripts/harness.py start <ID> --objective "<goal>" --base <base>`.
   Initialize `graph.py init <ID> --workflow ticket` (or `bugfix`) with actual
   repo/security/contract flags; record intake/analyze/plan only after the work
   exists. For a reused parent plan, record its reference as the plan evidence.
   See [ledger](../../../harness/references/workflow.md) and
   [graph](../../../harness/graph/README.md). Never write Claude state fields.
9. **Hand off.** Report scope, design/estimate, approved base, branch, parent/unit
   status and native-state result. Stop before feature-code edits for a start-only
   request; continue to the implementation skill when the user requested the full
   lifecycle and the plan is approved. A fresh Codex session is optional; use
   `$handoff` when context needs to be transferred.

## Output contract

Repository/toolchain/file/consumer scope, solution plan or evidenced parent-plan
reuse, confirmed base, correct working branch, tracker status (or unavailable),
and local task state. Starting a branch does not imply authorization to publish.
