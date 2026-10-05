---
name: handoff
description: "Write a concise, redacted handoff with task refs, verified state and next actions for a fresh Codex session."
---

# handoff

Read [the Codex adaptation contract](../../../harness/references/codex-adaptation.md) before using this workflow.

Save a narrative handoff outside Git repos, for example
`~/.codex/handoffs/<workspace>/<yyyy-mm-dd>-<slug>.md`. Create only the needed
folder. Use the user's local date and confirm the path is outside target repos.
The native ledger's handoff summary complements this document; it is not a
second task-state schema.

## Content

- Current objective/ticket, acceptance criteria, approved plan/base and decisions.
- Each touched repo's current branch, HEAD, dirty/staged scope and upstream.
- Verified completed work, work in flight, blockers and the next concrete action.
- Actual per-repo test/review results and whether later edits invalidated them.
- Cross-repo producer/consumer/migration/deployment follow-ups still owed.
- Ticket, review URLs, commit refs and `repo:path:line` pointers; reference artifacts
  rather than reproducing their content or replaying conversation history.
- Suggested skills (`change-implementation`, `$ship-task`, `$review-mr`) for resumption.

## Tickets in flight

Read each target repo's `harness/state/*.json`; do not assume Claude fields such
as `final_status`, `completed_nodes` or `human_approval`. The native ledger uses
`phase`, task ID, objective/base, graph metadata, verification/review and handoff.
Use `harness.py status <ID>` to inspect records. Include non-shipped tasks in a
table of task/repo/phase/branch/last evidence; a shipped task with unresolved
remote merge/deploy or consumer follow-ups belongs in a separate pending list.
If no tasks exist, say so; corrupt/unavailable state is unknown, not empty.

## Finish

Write state, not narrative; tailor to the user's stated next-session focus.
Redact credentials and `.env` contents. If a native task exists, record its short
summary with `harness.py handoff <ID> --summary "<state and next action>"`; link
that ledger from the saved doc. Print the document path and a one-line resume hint.
A handoff does not authorize the next session to ship or edit outside approved scope.
