# Node: intake

| Field | Contract |
| --- | --- |
| Purpose | Capture the ticket/objective, comment context and parent/sub-task identity; never requires a tracker connection. |
| Capability | [start-task](../../../.agents/skills/start-task/SKILL.md) |
| Reads | Ticket text/comments/sub-tasks or pasted request; project identity and user scope. |
| Allowed actions | Read/restate objective; identify the unit; prepare ticket creation only within tracker authorization. |
| Forbidden actions | Source editing, premature branch creation or ticket transition before confirmed planning/base. |
| Dependencies | none, only when routed into this run. |
| Routing | `always` in the JSON workflow. |
| Retained retry policy | One capture pass; ask for missing input instead of blind retry. |
| Native attempt ceiling | 3; this upper bound does not authorize extra attempts contrary to the retained policy. |
| Success/evidence | Captured task ID/objective and ticket/parent/unit references. |

Record pass/fail/blocked with an actual evidence summary through `graph.py`;
results include the current fingerprint and attempt. Do not write Claude state
fields. Failure stops dependent nodes; correction belongs to the capability
that owns the gap. Use a reasoned `retry` only after new corrective work/input,
within the retained policy; it cannot bypass red or stale verifier evidence.
See [graph contract](../README.md) and [ledger](../../references/workflow.md).
