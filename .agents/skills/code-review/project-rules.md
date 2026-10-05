# Project-Rules Review

Read-only review of pending changes against the workspace's own rules. Produces severity-ranked
findings at `file:line` and a pass/fail gate. **No edits.**

## When to Use
- User says "review my diff", "pre-MR check", "does this follow the rules", before commit/MR.
- After implementation, as an independent check.

## Workflow Steps
1. **Detect scope.** Run `git status` / `git diff` (and `git diff <base>...` for a branch) in the
   changed repo(s). Identify which repo(s) and what changed.
2. **Load the rules** for each touched repo: the matching `harness/rules/*.md` files plus the
   cross-cutting workspace rules.
3. **Check the diff** against the project's conventions, for example:
   - Backend/Java: strict layering, naming, **no persistence-entity leakage** out of the repository
     layer, the project's controller/versioning convention.
   - Frontend: the project's component/data-source placement, no `console.log`/`any` drift, the
     project's package manager.
   - IDE extensions / generated docs: edit the source-of-truth file, not its symlinks; keep
     backend/frontend logic separate.
   - Cross-tier **contract sync** — flag DTO/endpoint edits in one service with no matching consumer
     edit when no codegen keeps them in sync.
   - DB changes land in the **correct migration repo**; no reliance on `ddl-auto`.
   - **Secrets** never committed; auth/rate-limiting not weakened; correct JDK/toolchain per repo.
   - Minimal-diff hygiene: no TODOs, dead code, or unrelated changes.
4. **Rank findings** (Blocker / Major / Minor / Nit) at `file:line` with a concrete fix.
5. For deep dives, point to specialist lenses (don't duplicate them).

## Rules Codex Must Follow
- **Read-only.** Do not modify source. Do not invent issues — cite the rule + `file:line`.
- Distinguish a real rule violation from a style preference; say which.
- Mark uncertain findings **Unknown / needs confirmation**.

## Output Format
```
## Diff Review — <repo>(s), <N> files
Gate: PASS | CHANGES REQUESTED

| Severity | file:line | Issue | Rule | Fix |
|----------|-----------|-------|------|-----|

Deep-dive suggestions: correctness | security-review | standards | architecture
```

## Verification Checklist
- [ ] Reviewed the actual `git diff`, not assumptions.
- [ ] Each finding cites a rule + `file:line`.
- [ ] Contract-sync and migration-repo placement checked.
- [ ] No secrets / no auth weakening introduced.

## Reminders
- One repo at a time; match each repo's configured JDK/toolchain.
- With no generated cross-repo client, a DTO/route change must have matching consumer edits.
- Specialist lenses already exist (correctness, standards, concurrency, performance, api-contract,
  architecture, security-review) — use them for depth.
