# Single-ticket completion audit

Read-only audit of **code against the final agreed requirements**, including
applicable tests. Tracker status, a local ship record or branch ancestry alone
is not completion evidence. Use the user's explicit ticket list or pasted text.
Follow [Codex adaptation](../../../harness/references/codex-adaptation.md),
[testing policy](../../../harness/rules/testing.md) and the release skill when
more than one ticket is being coordinated.

## Step 0 — Baseline and branch gates

Resolve the ticket's real base/target under the confirmed release model. Inspect
branch/HEAD/index/dirty work and relevant remote refs; fetch before claiming the
base is current. Record exact inspected refs and unavailable network/auth.
Do not stash, switch branches or overwrite the user's work for this read-only
request. Read via `git show <ref>:<path>` or use isolated worktrees for checks.

Check whether the ticket branch lacks base commits; report behind/outdated status
as a synchronization risk. Simulate its merge onto the fresh explicit base
without changing the active tree. Conflicts block readiness; unavailable merge
or freshness checks remain Unknown. Local `workspace.py audit --base <ref>
--branch <ref>` output is a lead that still requires content review.

For squash merges or duplicate commits, compare target content and behavior,
not ancestry or three-dot diffs alone. Trace patch/history and inspect whether
later tickets overwrote the change. A branch may be obsolete while equivalent
acceptance code already exists at the release target.

## Step 1 — Derive atomic requirements

Fetch the full ticket discussion and sub-tasks through the available connector,
or use the supplied text. Reconcile the description with final agreed scope;
keep unresolved requirements Unknown. A bug criterion includes the symptom no
longer reproducing and the applicable regression evidence.

Exclude lifecycle/process units (`[QA] Verify`, `[BE]/[FE] Review code`,
`Resolve feedback merge request`) from the code-requirement checklist. Report
those statuses as context only; their To Do status does not lower correct code's
score. `[BE]`/`[FE]` implementation sub-tasks name real repo/deliverables and are
in scope. Never turn every process sub-task into another acceptance criterion.

## Step 2 — Gather evidence

Map **every** criterion to actual `repo:path:line`, implementing symbols/config,
producer and all consumers, applicable tests and passing-run evidence. Read
component rules, [Java gate](../../../harness/rules/java.md), migration ownership
and [navigation](../../../harness/docs/rules/gitnexus.md). Prefer a fresh graph
for structural relationships; use rg and code reads for uncovered/dynamic paths.
A clean single-repo graph query cannot prove cross-repo DTO/endpoint/WS/SSE safety.

When permitted, bounded read-only workers may gather per-repo evidence; provide
explicit refs and relevant graph/rule context and wait for every result. Workers
inherit the configured Codex model. The orchestrator verifies evidence and owns
scoring and the completion plan; do not copy Claude Agent/Workflow model flags.

## Step 3 — Score each criterion

| Status | Required evidence |
| --- | --- |
| Done (1) | Implemented correctly and applicable tests have passing evidence. |
| Partial (0.5) | Incomplete implementation, only happy-path coverage, or implemented without required test coverage. |
| Missing (0) | Inspected target contains no implementation for the criterion. |
| Unknown | Insufficient source/runtime/test/tool access to establish the result. |

Retained **frontend exception**: implemented UI source can score Done without
new unit tests; it still needs the existing applicable validation. This covers
web UI and an extension's TS/React frontend, not its Python server half or
expectations of a dedicated UI/e2e suite. Do not downgrade UI solely for absent
new Jest specs, or extend the exception to backend code.

## Step 4 — Calculate and report

Known completion = `(Done + 0.5 × Partial) / (Done + Partial + Missing) × 100`.
Unknown is excluded from that denominator **and counted separately**. If all
criteria are Unknown, completion is N/A. A 100% known score with Unknown criteria
or required gates is not verified completion or a verified Go.

Output ticket/title, inspected repo/ref/base and freshness, criterion/status/
evidence-or-gap table, counts and arithmetic, branch/merge gates, consumer/DB/auth
findings, process-subtask context and confidence. Cite evidence for structural
claims; uncertainty remains explicit.

## Step 5 — Plan only the gaps

If complete, state that no implementation gap was found, with verification limits.
Otherwise order the concrete missing/partial work by dependency: approved working
branch/base, implementation files, all consumers, correct migration changeset,
required regression/unit/integration checks, review and ship. Use the same
five-field step structure as solution-planning. Do not edit source, transition
tickets, post comments or publish merely because the audit found a gap.
