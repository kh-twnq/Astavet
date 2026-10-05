---
name: merge-conflict-resolution
description: "Resolve merge or rebase conflicts by preserving each side’s intent and verifying resulting behavior."
---

# Merge Conflict Resolution

Read [the Codex adaptation contract](../../../harness/references/codex-adaptation.md) before using this workflow.

Resolve conflicts by **understanding intent, not by picking sides mechanically**. Run everything
inside the one affected repo (multi-root workspace — see [`../../../harness/rules/workspace.md`](../../../harness/rules/workspace.md)).

## Steps

1. **See the state.** `git status`, `git log --oneline --graph -15`, list conflicting files. Identify
   what is being merged into what, and why (MR sync? base update? release merge?).

2. **Find the primary sources for each conflict.** Understand why *each side* changed: commit
   messages (`git log -L` on the hunk), the MRs/PRs (e.g. on GitLab, `glab mr list --source-branch
   ...`), the `<TICKET-KEY>` tickets they reference. Don't resolve a hunk whose intent you can't
   state in one sentence per side.

3. **Resolve each hunk.** Preserve both intents where possible. Where incompatible, pick the one
   matching the merge's stated goal and note the trade-off in the summary. Do **not** invent new
   behavior. Resolve — don't `git merge --abort` — unless the user asks to abort.

4. **Re-run the repo's checks** per [`../../../harness/rules/testing.md`](../../../harness/rules/testing.md) with the
   repo's configured toolchain/JDK: typecheck/lint first (a JS/TS frontend: `yarn tsc`), then the
   affected tests. Fix anything the merge broke.

5. **Finish.** Stage only the resolved, intended files and commit (a merge commit keeps git's default message + conflict
   list); if rebasing, `git rebase --continue` until done. Verify with `git branch --show-current`
   before committing — checkout can silently land on a protected branch mid-flow.

## Common traps

- **Squash-merge breaks ancestry.** When a repo squash-merges an MR/PR into its base branch, after
  your change merges the original branch is NOT an ancestor of the base — verify presence of your
  change by **content diff** (`git diff origin/<base> -- <files>`), never by ancestry. When updating
  a long-lived branch, **merge the base into the branch first** so the target's content survives the
  squash; resolving conflicts in the MR UI or by force-pushing a rebase can silently drop the other side.
- **"Local changes would be overwritten" / "not uptodate" despite a clean status** — inspect index, file modes, filters and actual contents first. Refresh stat data with `git update-index --refresh`; do not force-remove index entries or overwrite a file before establishing what would be lost. Preserve both intents while fixing the underlying index/worktree discrepancy.
- If a workspace guard **denies `git checkout .` / `reset --hard` / `push --force`** — resolve
  file-by-file; if history rewriting is truly needed, hand the command to the user.

*Adapted from [mattpocock/skills](https://github.com/mattpocock/skills) `resolving-merge-conflicts` (MIT), plus workspace lessons.*
