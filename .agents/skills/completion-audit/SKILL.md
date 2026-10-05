---
name: completion-audit
description: "Audit one ticket or a release for acceptance completeness, landed changes and conflicts across tickets or repositories."
---

# Completion Audit

Read [the Codex adaptation contract](../../../harness/references/codex-adaptation.md) before using this workflow.

Audit ticket **completeness** against the codebase — for a **single ticket** (single-ticket mode; the
per-ticket logic lives in [single-ticket.md](single-ticket.md)) or a **whole release** (an explicit
list of tickets), where it additionally audits whether the tickets are **conflict-free**.

**Mode:** 1 ticket → run the per-ticket audit only (single-ticket.md, including **its own Step 0
baseline sync** — the "confirm-the-real-base-and-verify-it's-current" requirement applies in both
modes, not just release mode), skip the conflict pass. ≥2 tickets → fan out one per-ticket audit each
(parallel) + one cross-ticket conflict pass. For a release list, establish two things with codebase
evidence:

1. **Completeness** — every ticket's requirements are 100% implemented with applicable test/verification evidence, respecting the frontend exception.
2. **Conflict-free** — no two tickets collide: same-file overwrites, broken cross-repo contracts, or
   clashing shared config/auth.

Read-only. **Never edits code, never transitions tickets.** It audits, scores, and produces a plan.

## When to Use
- User pastes a set of ticket keys and asks "is this release ready / complete / conflict-free".
- Pre-release / release-candidate gate, before cutting a release per your branch model.
- After several `feature/*` + `bugfix/*` branches have merged and you need a combined verdict.

## Prerequisites

- Ticket text and complete comments/sub-tasks through an available tracker
  connector, or pasted requirements. Discover its real schema and Atlassian
  site/cloud ID; do not assume a hostname is universally accepted as `cloudId`.
  An inaccessible ticket is Unknown, not an invented requirement list.
- Read access to every implementing/consumer/migration repo in AGENTS.md's Map.
- An explicit user-supplied ticket list. Do not derive it from Fix Version or
  commit history unless requested.
- Each ticket's **real current base/target ref**, confirmed under the release
  model and fetched/verified before evidence gathering. Inspect refs or isolated
  worktrees; the active working branch need not be switched to the audit base.
  Resolve missing bases before delegating evidence gathering.

## Execution model — bounded Codex delegation

For a release, use one read-only audit task per ticket when native delegation is
available and permitted. Wait for **all** results before one cross-ticket conflict
synthesis. Give each task only its scope, explicit refs, rules and graph/rg evidence;
all workers inherit the user's selected model. The orchestrator verifies every
structural/conflict claim before scoring. If delegation is unavailable, execute
the same passes inline and report the limitation. Single-ticket work follows
[single-ticket.md](single-ticket.md); release-specific safeguards are in
[release.md](references/release.md). No Claude Workflow API or model alias is needed.

## Workflow

### Step 0 — Establish the real, current baseline

Resolve each ticket's base/target from the confirmed release model; ask only if
missing. Inspect branch/HEAD/dirty work, fetch the relevant remote refs when
available, and record exact base and ticket SHAs. Preserve the user's branch,
index and working contents. Read files at refs or use isolated worktrees for
merge simulation; do not stash/switch the active tree for a read-only audit.

Run the outdated-branch gate (base commits absent from the ticket branch) and
simulated-merge gate against the fresh explicit base. A conflict is a blocker;
unavailable gates are Unknown. Being behind the base alone is a synchronization
risk, not proof that acceptance code is missing. `workspace.py audit --base ...
--branch ...` provides local evidence leads, not a remote release verdict.

After squash/duplicate commits, confirm target **content** and expected behavior
at each relevant file; ancestry or a three-dot diff alone cannot establish whether
an equivalent change landed. Track later overwrites at the release target.
Read [single-ticket.md](single-ticket.md) for evidence/scoring and verify the
navigation index matches the examined ref before relying on graph results.

### Step 1 — Normalize the release
Collect the ticket keys from the user. For each, `getJiraIssue` (include `comment` in `fields`) and
extract the **flat checklist of atomic requirements** (acceptance criteria, or decompose the
description) — **reconciled with the comment thread**: PO/BA/dev/QA replies refine, drop, or add
requirements after the description was written, and the audit must score against the *final agreed*
spec (latest agreed scope governs; unresolved questions become ⚠️ Unknown criteria). Note issue type
(a Bug's criterion is *defect no longer reproduces + regression test*), linked subtasks, and the issue
key (for grepping branches/commits). Restate each ticket's goal in one line so the user can confirm scope.

**Only audit code vs. requirement — filter subtasks down to code-implementation ones.** A story's
subtasks normally mix real dev work with the fixed trailing lifecycle pattern `solution-planning`
always appends: `[QA] Verify: ...`, `[BE]/[FE] Review code`, `[BE]/[FE] Resolve feedback merge
request`. These are process subtasks, not requirements — never decompose them into criteria, and
never let their Jira status gate a *code* requirement's score (see [single-ticket.md](single-ticket.md)
Step 1 for the full rule). Only `[BE]`/`[FE]` implementation subtasks are in scope for evidence.

### Step 2 — Fan out: per-ticket completeness audit (parallel)
One bounded worker per ticket when delegation is available and permitted; otherwise gather inline. Each evidence pass:
- Maps every requirement to implementing code **and** tests (`path:line`), using the
  [rules/java.md](../../../harness/rules/java.md) routing table and `harness/rules/*.md` to
  know where things should live. Prefer GitNexus (`query`/`context`/`impact` — load via the active client’s tool discovery;
  see [docs/rules/gitnexus.md](../../../harness/docs/rules/gitnexus.md)) over grep to find implementing symbols and to
  fill `filesTouched[].symbols`; for `contracts[]`, a cross-repo Contract Registry (if configured)
  resolves route↔consumer for the linked endpoints — everything it doesn't cover stays manual.
- Scores each requirement ✅ Done / 🟡 Partial / ❌ Missing / ⚠️ Unknown and computes the ticket's
  completion % (Done=1.0, Partial=0.5, Missing=0; Unknown excluded from denominator, reported separately).
- Records its **footprint** — the raw material for conflict detection:

```
TICKET_FOOTPRINT schema (per ticket):
{
  ticket, title, issueType, completionPct,
  criteria: [{ id, text, status, evidence }],          // status ∈ ✅🟡❌⚠️
  filesTouched: [{ repo, path, symbols, lines }],       // every file the ticket implies a change in
  contracts:   [{ repo, kind, name, change }],          // kind ∈ dto|endpoint|ws|sse|stomp; change ∈ added|modified|removed
  configAuth:  [{ repo, area, key, change }],           // area ∈ security|jwt|rate-limit|env|yaml|constants
  migrations:  [{ repo, changeset, table }],            // which DB / migration repo
  unknowns: [ ... ]
}
```

### Step 3 — Barrier: cross-ticket conflict detection
Collect all footprints, then detect collisions. **Focus, in priority order (as requested):**

1. **Cross-repo contracts** — the highest-risk class when there is "no generated client"
   (`workspace.md`). For every `contracts[]` entry, check whether another ticket changes a **consumer
   or producer** of the same DTO/endpoint/WS/SSE/STOMP channel in an incompatible way. Map the tiers:
   web frontend(s) ↔ the backend service ↔ downstream/AI service `/api/v1` ↔ any IDE-extension client.
   A backend DTO/route change with no matching frontend/consumer edit in the release is itself a
   conflict (a **missing-counterpart** conflict), not just completeness debt.
2. **Shared config/auth** — overlapping edits to `security`, JWT issuer/JWK, rate-limit, env vars,
   shared `*.yml`, or `constants/`. Two tickets setting the same key to different values, or one
   weakening auth another tightens, is a hard conflict.
3. **Same-file edits** — two tickets touching the same `repo:path` (especially the same symbol / method /
   adjacent lines). Flag as 🔴 if the intents are incompatible (overwrite, contradictory logic), 🟠 if
   merely co-located (likely a merge-resolve, not a logic clash).

Secondary (report if present, even though not the primary focus): **DB migrations** — duplicate/colliding
migration changesets, same-table changes, or ordering hazards in the same migration repo; remember the
per-DB / per-migration-repo split (`migration.md`).

Classify each finding:
| Severity | Meaning |
|----------|---------|
| 🔴 Conflict | Will break at build/runtime or silently corrupt behavior if both ship as-is |
| 🟠 Risk | Co-located / overlapping; needs a deliberate merge decision but not inherently broken |
| 🟢 Clear | Footprints overlap by path only, logically independent |

**Ground every 🔴 before it reaches the report.** The synthesis agent reasons over footprint JSON —
its verdicts are hypotheses, not facts. The orchestrator must verify each 🔴 with a targeted check:
- *same-file / same-symbol* → GitNexus `impact`/`context` on the contested symbol + read the actual
  lines both tickets touch;
- *cross-repo contract* → `api_impact`/`route_map` on the provider side (and `group contracts`/
  `group impact` on the cross-repo group when the contested route is cross-linked there — see
  [`harness/docs/rules/gitnexus.md`](../../../harness/docs/rules/gitnexus.md)), then confirm the consumer at `file:line`;
- *config/auth* → read both tickets' actual edits to the key.

A 🔴 that survives verification is reported with its evidence attached. One that can't be verified
is **downgraded to 🟠 with an explicit ⚠️ "unverified — needs human check" note** — never silently
kept at 🔴 on the synthesis agent's word alone.

### Step 4 — Release verdict
Aggregate:
- **Release completeness %** = mean of per-ticket completion % (state the math; weight by effort only if
  the user gives weights). Call the release **complete only if every ticket is 100%** — a 90% average
  with one 60% ticket is **not** release-ready; say so explicitly.
- **Conflict status** = ✅ none / ⚠️ N risks / 🔴 N conflicts.
- **Go / No-Go**: No-Go if any ticket < 100%, any 🔴 conflict exists or a required gate fails. Unknown acceptance/gate evidence prevents a verified Go, even when the known-criterion score is 100%.

## Output Contract
```
## Release Audit — <N tickets>
Verdict: NO-GO   (completeness 88%, 1 ticket < 100%; 🔴 2 conflicts, 🟠 1 risk)

### Completeness (per ticket)
| Ticket    | Title                  | %    | ✅ | 🟡 | ❌ | ⚠️ | Top gap (path) |
|-----------|------------------------|------|----|----|----|----|----------------|
| PROJ-101  | Web user-guide toggle  | 100% | 5  | 0  | 0  | 0  | —              |
| PROJ-102  | Inactive session …     | 60%  | 3  | 1  | 1  | 0  | SessionSvc:30 no test |

### Conflicts
| # | Sev | Tickets            | Where (repo:path / contract / key)        | Why it conflicts                |
|---|-----|--------------------|-------------------------------------------|---------------------------------|
| 1 | 🔴  | PROJ-102 ↔ 103     | backend AuthFilter.java:88 (same method)  | both rewrite session-expiry; logic contradicts |
| 2 | 🔴  | PROJ-101 → (none)  | contract: GET /api/v1/…/state             | backend DTO changed, no frontend consumer edit in release |

### Plan to release-ready (gaps + conflicts only)
- PROJ-102: add regression test … (path) — via rules/java.md gate
- Conflict #1: reconcile AuthFilter session-expiry — decide owning ticket, re-test both
- Conflict #2: add matching frontend edit in src/services/… or descope PROJ-101
```
Then: the per-ticket evidence tables on request. **No code changes, no ticket transitions.**

## Rules Codex Must Follow
- **Only audit code vs. requirement, scoped to code-implementation subtasks.** Filter out process/
  lifecycle subtasks — `[QA] Verify: ...`, `[BE]/[FE] Review code`, `[BE]/[FE] Resolve feedback merge
  request` (the standard `solution-planning` trailing pattern) — from the requirement checklist and
  from evidence scope. Their Jira status never gates a code requirement's ✅/🟡/❌; report it as
  context only. See [single-ticket.md](single-ticket.md) Step 1 for the full rule.
- **Confirm each ticket's real base (per your project's release model), fetch it, and
  verify it is current before evidence-gathering** (Step 0) — in **both** single-ticket and release
  mode — with explicit refs or isolated worktrees; ask the user for the base if it isn't already
  recorded. Run the **outdated-branch gate** (branch behind `origin/<base>` → ⚠️ needs rebase) and the
  **no-conflict gate** (dry-run merge onto the fresh base → 🔴 if it conflicts) before scoring, and
  surface both in the verdict. Preserve each repo's original branch, index and working contents throughout the audit.
- **Never call a ticket branch "unmerged" or a requirement missing from `git merge-base
  --is-ancestor` alone** — squash-merges and duplicate/parallel commits break ancestry while the
  content already shipped; confirm with a target-content comparison (`git show <base>:<path>` and `git show <branch>:<path>`, plus relevant patch/history)
  before scoring or reporting a gap.
- **Read-only**; cite real `path:line`. Mark anything unverifiable **⚠️ Unknown**, never invent.
- **Multi-repo workspace** — name the specific repo for every file/contract/migration (`workspace.md`).
- Untested code is **🟡 Partial**, not Done — **except in-scope FE source** (the web frontend repos),
  which per [`harness/rules/testing.md`](../../../harness/rules/testing.md) does not require new unit tests;
  implemented-but-untested code there scores Done. Backend/server code (e.g. an IDE extension's server
  half) and dedicated UI/e2e suites are **not** covered and keep the normal rule.
- A backend/downstream-service contract change with **no matching consumer edit** in the release set
  is a conflict (missing counterpart), because no codegen propagates it.
- Match each repo's toolchain only matters if you build — this skill doesn't build; it reads.
- Multiple DBs: keep migration findings in the correct migration repo (`migration.md`).
- Repos may keep their agent rules in a different file (e.g. an `AGENTS.md` that another agent instruction file symlinks
  to) — read the repo's own rules file.

## Verification Checklist
- [ ] Each ticket's real base was confirmed (per the project's release model; asked if
      unknown), fetched, and verified current before evidence-gathering, using explicit refs or isolated worktrees — single-ticket mode included.
- [ ] Outdated-branch gate ran (branch behind `origin/<base>` → ⚠️ flagged, not passed clean) and the
      no-conflict gate ran (dry-run merge onto fresh base → 🔴 if it conflicts); both surfaced in the verdict.
- [ ] Any branch that looked "unmerged" via `git merge-base --is-ancestor` was double-checked with a
      content diff before being scored as a gap or reported as an unmerged conflict.
- [ ] Every supplied ticket was fetched (or flagged ⚠️ Unknown) and decomposed into atomic requirements,
      excluding `[QA] Verify`/`Review code`/`Resolve feedback merge request` process subtasks.
- [ ] Each requirement traced to code **and** test, or marked ❌/🟡/⚠️ with a reason.
- [ ] No requirement was downgraded solely because a QA/E2E, review, or resolve-feedback-MR subtask
      hadn't completed — only code-level evidence gates the score.
- [ ] Release marked complete only if **every** ticket is 100%.
- [ ] Cross-repo contracts, shared config/auth, and same-file edits each checked across all ticket pairs.
- [ ] Each conflict has both tickets, the exact location, and why; severity assigned.
- [ ] Every 🔴 conflict was verified by a graph query or direct file read (evidence attached);
      unverifiable ones downgraded to 🟠 + ⚠️, not reported as fact.
- [ ] Go/No-Go stated with the math behind it.

## Anti-patterns
❌ Trusting ticket status; ❌ counting untested code as done outside the in-scope-FE-source exception
(`harness/rules/testing.md`); ❌ averaging % and calling 88% "done" when a ticket is at 60%; ❌ checking
only same-file edits and missing a broken cross-repo contract; ❌ editing code or moving tickets;
❌ calling a branch "unmerged"/a requirement "missing" from `git merge-base --is-ancestor` alone
without a content diff (squash-merges and duplicate commits break ancestry, not content); ❌ treating
a `[QA] Verify`/`Review code`/`Resolve feedback merge request` subtask as a requirement, or its To Do
status as a reason to downgrade code that's actually implemented. ✅
Evidence-linked statuses, pairwise conflict checks, transparent Go/No-Go.

## Native coordination and state

Use active-client subagent tasks for the per-ticket footprints described above,
await all results, then synthesize conflicts and verify them against code. Do not
copy the source's JavaScript `Workflow` scheduler template into a Codex skill.

A release graph routes only `release_audit`; it cannot ship. Record actual audit
results with `graph.py` into the existing native ledger if initialized, rather
than writing Claude state fields or claiming that a ledger proves completion.
The footprint is audit-report data, not a second task-state schema. Preserve
unavailable tools, unknown criteria and failed gates in the report.

## References
- [single-ticket.md](single-ticket.md) (per-ticket logic) · [task-scoping](../task-scoping/SKILL.md)
  (where things live) · [code-review](../code-review/SKILL.md) (diff-level rules) ·
  [rules/java.md](../../../harness/rules/java.md) (gate for the plan)
- `harness/rules/workspace.md` (no codegen / cross-repo contracts), `migration.md` (per-DB migration repos)
