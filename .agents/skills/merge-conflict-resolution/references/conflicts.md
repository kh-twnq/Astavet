# Conflict traps

Squash merges break original ancestry: inspect target content to establish that
a change landed. Preserve each side's intent when updating a branch from its
base. A clean status with stale index diagnostics needs disk/index/HEAD inspection
and a safe refresh (`git update-index --refresh`) before any restoration.
Never force-remove entries then restore as a blanket remedy. Keep user changes
and resolved hunks intact. Check branch, merge/rebase metadata, unresolved stages
and actual tests before finishing. Abort only when requested or necessary to
preserve work, with a clear explanation.
