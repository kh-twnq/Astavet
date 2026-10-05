# Node: ship

| Field | Contract |
| --- | --- |
| Purpose | Human-authorized commit/push/review lifecycle after all required current evidence; reuse existing publication authorization. |
| Capability | [ship-task](../../../.agents/skills/ship-task/SKILL.md) |
| Reads | Current verifier/review/graph evidence, confirmed base/branch, intended staged scope and publication authorization. |
| Allowed actions | Prepare exact artifacts, run ready, commit/push/open review and authorized tracker/worklog actions; log actual user-confirmed time only. |
| Forbidden actions | Publishing with red tests/stale evidence/unresolved Blocker or Major; guessing target/tag/time; equating local ship with remote merge/deploy. |
| Dependencies | test, review, security, integrate, only when routed into this run. |
| Routing | `always` in the JSON workflow. |
| Retained retry policy | One gate/publication attempt; upstream red returns to correction, not a self-override. |
| Native attempt ceiling | 1; this upper bound does not authorize extra attempts contrary to the retained policy. |
| Success/evidence | Checked committed snapshot, review URL, actual target/pipeline/tracker/worklog outcomes and explicit remote-state limits. |

Record pass/fail/blocked with an actual evidence summary through `graph.py`;
results include the current fingerprint and attempt. Do not write Claude state
fields. Failure stops dependent nodes; correction belongs to the capability
that owns the gap. Use a reasoned `retry` only after new corrective work/input,
within the retained policy; it cannot bypass red or stale verifier evidence.
See [graph contract](../README.md) and [ledger](../../references/workflow.md).

The ledger CLI records this terminal node after the commit is checked; `graph.py record` does not publish or stand in for `harness.py ship`.
