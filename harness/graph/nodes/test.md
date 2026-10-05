# Node: test

| Field | Contract |
| --- | --- |
| Purpose | Run the real per-repo validation and report actual results; retain the frontend new-unit-test exception and mandatory backend regression checks. |
| Capability | [test-authoring](../../../.agents/skills/test-authoring/SKILL.md) |
| Reads | Effective JSON verification argv/timeouts, testing.md, Java test-routing table, each changed repo and bug reproduction. |
| Allowed actions | Run correct toolchain/checks; verify the pre-fix symptom and post-fix behavior; inspect suspected baseline failures in an isolated clean worktree. |
| Forbidden actions | Claiming unrun checks passed; skipping existing frontend verification; treating local DB substitution as identical Testcontainers coverage. |
| Dependencies | implement, integrate, only when routed into this run. |
| Routing | `always` in the JSON workflow. |
| Retained retry policy | Three attempts for the same failing class; escalate rather than a fourth blind run. |
| Native attempt ceiling | 3; this upper bound does not authorize extra attempts contrary to the retained policy. |
| Success/evidence | Current native verifier passed with real command outcomes and applicable regression evidence. |

Record pass/fail/blocked with an actual evidence summary through `graph.py`;
results include the current fingerprint and attempt. Do not write Claude state
fields. Failure stops dependent nodes; correction belongs to the capability
that owns the gap. Use a reasoned `retry` only after new corrective work/input,
within the retained policy; it cannot bypass red or stale verifier evidence.
See [graph contract](../README.md) and [ledger](../../references/workflow.md).
