---
name: task-scoping
description: "Map a ticket or task to repositories, toolchains, affected files, consumers and migration ownership before implementation."
---

# Task Scoping

Read [the Codex adaptation contract](../../../harness/references/codex-adaptation.md) before using this workflow.

Read-only scoping skill. Turns a ticket/task into a concrete map of **what to change and where** in the
workspace, so `change-implementation` can start from a confirmed plan. **No code changes.**

## When to Use
- User says "analyze <TICKET-KEY>", "scope this ticket", "where does this go", "what's affected".
- Before `change-implementation` on anything non-trivial.

## Workflow Steps
1. **Read the ticket/task — including its comment thread.** If a ticket key, fetch it with `comment` in
   `fields`; otherwise use the description. Comments from PO/BA/dev/QA often refine or override the
   description — reconcile both (latest comment usually wins) and carry open questions as Unknowns.
   Restate the goal + acceptance criteria in your own words.
2. **Identify target repo(s)** from `AGENTS.md` → Repository Map, mapping the work to the responsible
   role (the backend service, the web frontend, the admin/IMS frontend, the AI/codegen service, the DB
   migration repo, an IDE extension, a shared library, …).
3. **Read that repo's rules**: the matching `harness/rules/*.md` (+ `workspace.md`). Note the repo's
   configured **toolchain/JDK**.
4. **Locate affected files** — GitNexus first ([`../../../harness/docs/rules/gitnexus.md`](../../../harness/docs/rules/gitnexus.md)):
   `query` the concept + `gitnexus://repo/{name}/clusters` to find the owning area, then `context` on
   candidate symbols; fall back to grep for config/YAML/SQL. List concrete paths.
5. **Assess cross-cutting impact**:
   - Contract impact across tiers (frontend ↔ backend ↔ other services) — no codegen, so list every
     consumer that needs a matching edit.
   - DB/schema impact → which migration repo owns the affected database.
   - Auth/JWT, secrets, rate-limiting touch points.
6. **Produce the report** and suggest the next skill.

## Rules Codex Must Follow
- **Read-only.** Do not modify source. Do not invent architecture.
- Work from code + `harness/rules/` evidence; cite file paths.
- Mark anything unverifiable as **Unknown / needs confirmation** — don't guess.
- Don't assume a monorepo; name the specific repo(s).

## Output Format
```
## Ticket: <key/title>
Goal: <1–2 lines>   Acceptance: <bullets>

Target repo(s): <repo> (toolchain/JDK <…>)
Affected files:
  - path:line — why
Cross-repo / contract impact: <consumers to update, or "none">
DB / migration impact: <which migration repo, or "none">
Auth / secrets / other risks: <…>
Open questions (Unknown/confirm): <…>
Suggested next: change-implementation | bug-investigation
```

## Verification Checklist
- [ ] Correct repo(s) and toolchain/JDK identified.
- [ ] Affected files are real paths (not guessed).
- [ ] Cross-tier consumers + migration repo impact considered.
- [ ] Unknowns flagged, not invented.

## Reminders
- Not a monorepo — one repo at a time; match each repo's configured toolchain/JDK (`workspace.md`).
- Keep each database's migrations in the repo that owns it (`migration.md`).
- Some repos keep their agent rules in an `AGENTS.md` (a `AGENTS.md` there may be a symlink) — check
  the repo's rules file.
