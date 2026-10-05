# Node: release_audit

| Field | Contract |
| --- | --- |
| Purpose | Read-only requirement and cross-ticket conflict audit at the actual release target; never ships the release. |
| Capability | [completion-audit](../../../.agents/skills/completion-audit/SKILL.md) |
| Reads | Explicit ticket list/comments, actual current target refs, all implementing/consumer code, relevant test evidence and process-subtask context. |
| Allowed actions | Collect per-ticket footprints, await bounded permitted workers, synthesize cross-ticket contracts/config/files/migrations and verify each serious finding. |
| Forbidden actions | Source/ticket/publication changes, switching or stashing the active user tree, scoring process-subtask status as code evidence, declaring Go with unknown required evidence. |
| Dependencies | none, only when routed into this run. |
| Routing | `always` in the JSON workflow. |
| Retained retry policy | One read-only pass; another pass needs changed code/evidence/input. |
| Native attempt ceiling | 1; this upper bound does not authorize extra attempts contrary to the retained policy. |
| Success/evidence | Per-criterion statuses/counts, unknowns, gate results, ticket footprints, verified conflict findings and transparent Go/No-Go. |

Record pass/fail/blocked with an actual evidence summary through `graph.py`;
results include the current fingerprint and attempt. Do not write Claude state
fields. Failure stops dependent nodes; correction belongs to the capability
that owns the gap. Use a reasoned `retry` only after new corrective work/input,
within the retained policy; it cannot bypass red or stale verifier evidence.
See [graph contract](../README.md) and [ledger](../../references/workflow.md).

Release routes only this node. Local dependency evidence is an audit input, not an automatic requirement-completeness proof. It cannot proceed to ship.
