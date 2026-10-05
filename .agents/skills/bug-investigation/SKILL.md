---
name: bug-investigation
description: "Diagnose a reported failure read-only using a reproducible feedback loop, competing hypotheses and root-cause evidence."
---

# Jira Bug Root-Cause Investigation Skill

Read [the Codex adaptation contract](../../../harness/references/codex-adaptation.md) before using this workflow.

Diagnose **why** a bug happens and **where** in the code, starting from a Jira ticket. This skill is
**read-only** — it produces a root-cause analysis, not a fix. When the cause is confirmed, hand off to
[$ship-task](../ship-task/SKILL.md) to verify and ship an implemented fix; use [change-implementation](../change-implementation/SKILL.md) for the actual code change.

> **Reasoning and roles:** follow [model routing](../../../harness/docs/rules/model-routing.md); retain the think-first and deep-review requirements using the selected Codex model.

## When to Use
- User says "investigate JIRA-123" / "find the root cause of PROJ-456" / "why does this bug happen"
- Before fixing — to understand the defect rather than patching the symptom
- Triage of a hard or intermittent bug

## Prerequisites
- An available tracker connector/MCP or the pasted ticket text. See [integrations](../../../harness/docs/rules/integrations.md); example tracker operation names must be mapped to actual tools.
- Read access to the codebase (and logs, if available).

## Workflow

### Step 1 — Read the ticket
`jira_get_issue PROJ-456` (include `comment` in `fields`). Extract the evidence:
- **Steps to reproduce**, expected vs actual behavior,
- **stack trace / error message**, affected version, environment,
- **the comment thread** — QA/devs often add better repro steps, logs, screenshots, scope corrections,
  or partial diagnoses below the description; the latest comment can override the original report,
- linked issues / recent related tickets (`jira_search` for duplicates or regressions).

If repro steps or the stack trace are missing, state what's needed — don't guess the cause from a vague summary.

### Step 2 — Build a feedback loop, then locate the failure point

**The loop IS the investigation** — hypotheses without a pass/fail signal are guesses. Before Step 3,
build a **tight, red-capable signal**: one command you have already run at least once, that goes red
on *this* bug's exact symptom (not just "doesn't crash") and will go green once fixed. Spend
disproportionate effort here; a 2-second deterministic loop is a debugging superpower.

Ways to construct one, in rough order (adjust to your environment — if Docker/Testcontainers aren't
available locally, prefer paths that don't need them; see [`../../../harness/rules/testing.md`](../../../harness/rules/testing.md)
per repo):
1. **Failing unit test** at whatever seam reaches the bug (e.g. `./gradlew test --tests <Class>`, `yarn test <file>`).
2. **curl/HTTP script** against a locally running service (run it with a local profile, or hit dev-env endpoints).
3. **Replay a captured payload/trace** (request body from logs or the ticket) through the code path in isolation.
4. **Throwaway harness** — minimal subset (one service, mocked deps) exercising the failing path; for DB-shaped bugs, a throwaway local database instance.
5. **Bisection** — bug appeared between two known commits → automate the check and `git bisect run`.

Tighten it: faster (narrow test scope), sharper (assert the exact symptom), deterministic (pin time,
seed randomness). Non-deterministic bug → don't chase a clean repro, **raise the reproduction rate**
(loop the trigger 100×, add stress/timing pressure) until it's debuggable. If you genuinely cannot
build a loop, **stop and say so** — list what you tried, ask for a captured artifact (log dump, HAR,
payload) or env access; do not proceed to hypotheses without one.

Then locate the failure point:
- Map the stack trace top frame to a concrete `file:line`; read that method and its callers —
  use GitNexus `context` (all callers/refs in one call) and `trace` ("how does entrypoint A reach
  failing symbol B?") instead of hand-chaining callers ([`../../../harness/docs/rules/gitnexus.md`](../../../harness/docs/rules/gitnexus.md)).
- Pin the **exact line/condition** where behavior first goes wrong (the failure point), distinct from
  where the symptom surfaces (e.g. NPE thrown at line 88, but the null originates at line 40).

*(Loop-first discipline adapted from [mattpocock/skills](https://github.com/mattpocock/skills) `diagnosing-bugs`, MIT.)*

### Step 3 — Form hypotheses
List candidate causes, most-likely first. For each: what code path would produce the observed symptom?
Common buckets to consider:
- null / missing-data not guarded; boundary / off-by-one; wrong condition or operator,
- state/lifecycle/ordering assumption; concurrency / race (see [code-review](../code-review/SKILL.md)),
- transaction / lazy-loading / N+1 (see [jpa-patterns](../spring-stack-patterns/jpa.md)),
- config / environment difference; external dependency contract change; regression from a recent commit.

### Step 4 — Verify the cause (don't just assert it)
Confirm the chosen hypothesis with evidence, not intuition:
- trace the data/flow from input to the failure point and show the broken link (GitNexus `trace`
  gives the call path; verify each hop by reading the code — graph edges are evidence, not proof),
- size the blast radius of the suspect symbol with `impact` (other affected call sites → Scope line),
- check git history for when it was introduced (`git log -L`, `git blame` on the suspect lines),
- drive the **Step-2 feedback loop** against the hypothesis: it must go red for the reason the
  hypothesis predicts (and stay green when the suspect condition is neutralized),
- rule out the other hypotheses explicitly.

Distinguish **root cause** (the underlying defect) from **trigger** (the input that exposes it) and
**symptom** (what the user saw).

### Step 5 — Report
Produce a concise analysis:
```
## Root cause: PROJ-456 — "NPE when plugin directory is missing"

Symptom:    NullPointerException at PluginManager.scan() (PluginManager.java:88)
Trigger:    app started with no /plugins directory
Root cause: PluginScanner.list() returns null (not empty list) when the dir is absent;
            scan() iterates the result without a null check.  → PluginScanner.java:40
Introduced: commit a1b2c3d (PROJ-300), 2026-05-12 — null path added then.
Scope:      affects any caller of PluginScanner.list(); 2 other call sites (X.java:55, Y.java:71)
Confidence: High — reproduced with a failing test (no /plugins dir → NPE)

Suggested fix direction (NOT applied):
- Return Collections.emptyList() from list() when dir is missing; add regression test.
- Alternative: null-guard at scan() — narrower but leaves other call sites exposed.
```
Optionally `jira_add_comment` to post the root-cause summary on the ticket (ask first if it's a shared board).

### Step 6 — Hand off
Recommend [change-implementation](../change-implementation/SKILL.md) to implement the chosen fix direction, then [$ship-task](../ship-task/SKILL.md) for verification and publication. This skill stops at diagnosis.

## Output Contract
1. Symptom / Trigger / Root cause clearly separated, each with `file:line`.
2. Evidence for the root cause (trace, blame/commit, or a reproducing test) — not just a claim.
3. Scope (other affected call sites) + a confidence level.
4. Suggested fix direction(s), explicitly **not applied**.

## Anti-patterns
❌ Naming the symptom location as the "root cause" (e.g. "the NPE line") without tracing the origin.
❌ Asserting a cause without reproducing or tracing it.
❌ Applying a fix — that's [$ship-task](../ship-task/SKILL.md), not this skill.
❌ Stopping at the first plausible hypothesis without ruling out the others.
✅ Symptom→trigger→root-cause chain, evidence-backed, scope-aware, fix left to the fix workflow.

## Example
```
> "Investigate PROJ-456 — why does it crash?"
1. jira_get_issue → stack trace + repro
2. trace NPE at PluginManager.scan():88 → null from PluginScanner.list():40
3. hypotheses: (a) list() returns null, (b) caller mutates, (c) race → verify (a)
4. git blame → introduced in PROJ-300; failing test reproduces
5. report symptom/trigger/root-cause + scope + fix direction
6. hand off to $ship-task
```

## References
- [$ship-task](../ship-task/SKILL.md) (verifies and ships the implemented fix) · [completion-audit](../completion-audit/SKILL.md)
- [code-review](../code-review/SKILL.md) · [jpa-patterns](../spring-stack-patterns/jpa.md) · [logging-patterns](../spring-stack-patterns/logging.md)
- [mcp-atlassian server](https://github.com/sooperset/mcp-atlassian)
