---
name: change-implementation
description: "Implement a scoped task with project conventions, consumer sync, migration discipline and verified completion."
---

# Change Implementation

Read [the Codex adaptation contract](../../../harness/references/codex-adaptation.md) before using this workflow.

Drives a change through an **approval-gated, minimal-diff** flow. Code is only written after the user
approves the plan (step 5).

> **Reasoning and roles:** follow [model routing](../../../harness/docs/rules/model-routing.md); retain the think-first and deep-review requirements using the selected Codex model.

## When to Use
- User says "implement <TICKET-KEY>", "do this ticket", "build the feature/fix", "apply the change".
- After `task-scoping` has scoped the work.
- **Any ad hoc "fix/build/change X" request that never went through `$start-task`** — steps 1–4 below
  are the substitute think-first pass for that case; don't skip straight to editing just because there's
  no ticket.

## Workflow Steps (mandatory, in order)
**Steps 1–4 perform the mandatory deep-reasoning pass — the think-first gate. Do not shortcut this by starting at step 6.**
1. **Read the current code first.** Open the actual files involved and understand existing patterns
   before forming any plan. Use GitNexus `context` on the symbols you'll touch and `impact` to list
   every caller the diff must keep working ([`../../../harness/docs/rules/gitnexus.md`](../../../harness/docs/rules/gitnexus.md));
   fold that blast radius into the plan.
2. **Identify affected repo(s).** Map the task to repo(s) via `AGENTS.md` → Repository Map; note each
   repo's toolchain/JDK. List every consumer if the change crosses tiers (frontend ↔ backend ↔ other services).
3. **Read relevant `harness/rules/`.** The matching repo file(s) + `workspace.md` (+ `testing.md`).
4. **Propose a plan.** List the exact files to change, the approach, the cross-tier/DB impact, and the
   checks you'll run. Keep it minimal. If a `solution-planning` result already exists for this ticket,
   this step just translates that plan into the tactical file list — don't re-litigate the design.
5. **WAIT for approval.** Do not edit source until the user approves. Approval already given for this concrete plan satisfies the gate; ask only for a missing decision or changed scope.

**Steps 6–8 execute the approved plan, after approval.**
6. **Implement a minimal diff.** Change only what the plan covers; match surrounding style; reuse
   existing helpers. No drive-by refactors, no TODOs/dead code. **NO comments/docstrings in any
   language** — only license headers, `@Override`/annotations, and load-bearing machine directives are
   allowed (`harness/rules/java-comment-rules.md`); make the code self-explanatory instead.
7. **Run relevant checks.** Use the per-repo test/lint commands from `harness/rules/testing.md` with the
   repo's correct toolchain/JDK. Watch for per-repo nuances (e.g. a repo that splits integration/controller
   tests into a separate task from plain unit tests) — check that repo's `testing.md` entry.
   If the change needs new tests, add minimal focused ones in the same step; defer deep JUnit/AssertJ
   authoring to the `test-authoring` skill.
8. **Summarize** changed files (table), what/why, checks run + results, and risks/follow-ups.

## Rules Codex Must Follow
- **No source edits before the step 5 approval. Do not invent architecture.**
- Minimal diff; operate in **one repo at a time**, but a single task may require a coordinated
  sequence across multiple repos (e.g. backend DTO + frontend consumer + another service's contract). Do not
  declare the task complete until all required consumers are updated or explicitly recorded as
  follow-up work. Match each repo's toolchain/JDK and conventions.
- **NO comments/docstrings in any language** (`harness/rules/java-comment-rules.md`) — only license headers,
  annotations, and load-bearing machine directives; self-documenting code, explanation in ticket/MR/ADR.
- Java: keep strict layering; **never expose JPA entities** outside the repository layer (`workspace.md`).
- Frontend: use the repo's package manager (check for `yarn`/`pnpm`/`npm`). IDE extensions may keep
  their agent rules in `AGENTS.md` (not a symlinked `AGENTS.md`) and need a build step after TS edits.
- Keep cross-tier contracts in sync (frontend ↔ backend ↔ other services) — update every consumer.
- Schema changes → a new migration changeset in the **correct** migration repo; never rely on `ddl-auto`.
- Don't weaken auth/rate-limiting; never commit secrets. **Commit/push/MR only if explicitly asked**
  (then defer to `$start-task` / `$ship-task` / `commit`).
- Report real check results — never claim green if not run.

## Output Format
**Before approval:**
```
## Plan — <ticket>
Repo: <repo> (toolchain/JDK <…>)
Files to change:
  - path — change
Approach: <short>
Contract/DB impact: <…>   Checks I'll run: <…>
```
**After implementation:**
```
## Done — <ticket>
| File | Change | Why |
Checks: <command> → <result>
Risks / follow-ups: <…>   Cross-tier edits needed elsewhere: <…>
```

## Verification Checklist
- [ ] Current code + relevant `harness/rules/` read before planning.
- [ ] Affected repo(s) and toolchain/JDK identified.
- [ ] Plan approved before any edit; existing approval carried forward.
- [ ] Diff is minimal and matches repo conventions.
- [ ] Correct toolchain/JDK; relevant tests/lint actually run and reported.
- [ ] Cross-tier consumers + migration repo handled.
- [ ] No secrets committed; no commit/push unless asked.

## Token hygiene
In the final summary, recommend running **`$ship-task` in a fresh session** (a fresh Codex session) — the diff,
branch, and ticket carry all required state; dragging the scoping/implementation transcript into the
ship phase is the single largest token cost in a full-lifecycle session.

## Reminders
- See `AGENTS.md` safety rules and the repo's `harness/rules/*.md`.
- Match each repo's configured toolchain/JDK — a mismatch fails confusingly (`workspace.md`).
- No generated cross-repo client — DTO/endpoint changes don't propagate automatically.
