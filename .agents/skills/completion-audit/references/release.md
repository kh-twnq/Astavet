# Release audit

Resolve target environment/base and ticket list. Audit each atomic criterion on
the release content, including comments that changed acceptance. Distinguish done,
partial, not_done and unknown; failed/inaccessible tools remain unknown. Aggregate
file overlaps, later overwrites, shared auth/config, schema ordering and contract
mismatch across tickets and repos. The local audit command supplies evidence
leads only; inspect actual target content, especially after squash merge. Return
per-ticket reasons, conflicts, blockers and go/no-go. No branch switches, tickets
transitions, source edits or release publication.

Apply the single-ticket checklist to each ticket, including fresh explicit bases,
outdated-branch and simulated-merge gates. A gate failure remains a release
blocker even when a criterion's code is present; an unavailable gate stays unknown.
Inspect later overwrites of earlier ticket changes at the release ref, not only
shared filenames. Check shared auth/config, migration dependencies and actual
producer/consumer compatibility. A green local ledger cannot replace this content
review. Keep process subtasks separate from acceptance deliverables and surface
partial or missing test evidence according to the retained testing policy, including the frontend new-unit-test exception. A 100% known score with unknown criteria/gates does not establish release completeness or Go.
