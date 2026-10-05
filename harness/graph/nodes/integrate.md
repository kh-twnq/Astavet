# Node: integrate

| Field | Contract |
| --- | --- |
| Purpose | Fan-in across all changed producer/consumer contracts; inspect every consumer and migration/deployment dependency before downstream validation. |
| Capability | [change-implementation](../../../.agents/skills/change-implementation/SKILL.md) |
| Reads | Every relevant repo diff, contract registry plus uncovered consumers, migration ownership and deployment order. |
| Allowed actions | Compare both sides, record named contract evidence and intentional rollout compatibility; route mismatches back to the owning repo implementation. |
| Forbidden actions | Skipping a routed contract check or silently changing another repo outside its approved implementation scope. |
| Dependencies | implement, only when routed into this run. |
| Routing | `cross_repo_contract` in the JSON workflow. |
| Retained retry policy | Two corrective checks of a mismatch; escalate if the claimed fix remains out of sync. |
| Native attempt ceiling | 3; this upper bound does not authorize extra attempts contrary to the retained policy. |
| Success/evidence | Named producer and every consumer, actual compatibility checks and current per-repo verifier fingerprints. |

Record pass/fail/blocked with an actual evidence summary through `graph.py`;
results include the current fingerprint and attempt. Do not write Claude state
fields. Failure stops dependent nodes; correction belongs to the capability
that owns the gap. Use a reasoned `retry` only after new corrective work/input,
within the retained policy; it cannot bypass red or stale verifier evidence.
See [graph contract](../README.md) and [ledger](../../references/workflow.md).
