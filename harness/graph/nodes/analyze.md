# Node: analyze

| Field | Contract |
| --- | --- |
| Purpose | Map the task to repositories, toolchains, actual files, all consumers, auth and database ownership; select routing flags. |
| Capability | [bug-investigation](../../../.agents/skills/bug-investigation/SKILL.md) |
| Reads | AGENTS.md Repository Map, component rules, current code and fresh graph/rg evidence. |
| Allowed actions | Read code/docs/graph; output scope and concrete routing decisions; clarify material ambiguity. |
| Forbidden actions | Source edits, ticket mutations or branch creation. |
| Dependencies | intake, only when routed into this run. |
| Routing | `always` in the JSON workflow. |
| Retained retry policy | One evidence pass; ambiguity is a clarification, not a retry. |
| Native attempt ceiling | 3; this upper bound does not authorize extra attempts contrary to the retained policy. |
| Success/evidence | Repository/toolchain/files/consumer/migration scope and justified route flags. |

Record pass/fail/blocked with an actual evidence summary through `graph.py`;
results include the current fingerprint and attempt. Do not write Claude state
fields. Failure stops dependent nodes; correction belongs to the capability
that owns the gap. Use a reasoned `retry` only after new corrective work/input,
within the retained policy; it cannot bypass red or stale verifier evidence.
See [graph contract](../README.md) and [ledger](../../references/workflow.md).

For `workflow: bugfix`, use [bug-investigation](../../../.agents/skills/bug-investigation/SKILL.md) as the analyze capability.
