# Node: review

| Field | Contract |
| --- | --- |
| Purpose | A findings-only pass on the final snapshot, separate from implementation; inspect applicable correctness, project-rule and deep lenses. |
| Capability | [code-review](../../../.agents/skills/code-review/SKILL.md) |
| Reads | Objective/criteria, actual final diff, current verification, component rules and graph/rg evidence. |
| Allowed actions | Read/rank findings; use one permitted deep-reviewer for all deep lenses. If only inline review is possible, report that limitation explicitly. |
| Forbidden actions | Fixing code within a read-only review, approving/merging on the host, inventing structural findings without evidence. |
| Dependencies | implement, integrate, only when routed into this run. |
| Routing | `always` in the JSON workflow. |
| Retained retry policy | Initial review plus one re-review for the same persistent Blocker/Major. |
| Native attempt ceiling | 3; this upper bound does not authorize extra attempts contrary to the retained policy. |
| Success/evidence | Ranked findings and declared verdict tied to the current snapshot; no unresolved Blocker/Major. |

Record pass/fail/blocked with an actual evidence summary through `graph.py`;
results include the current fingerprint and attempt. Do not write Claude state
fields. Failure stops dependent nodes; correction belongs to the capability
that owns the gap. Use a reasoned `retry` only after new corrective work/input,
within the retained policy; it cannot bypass red or stale verifier evidence.
See [graph contract](../README.md) and [ledger](../../references/workflow.md).

Ship always requires the current review result; a small diff does not waive it.
