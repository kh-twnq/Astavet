# Node: plan

| Field | Contract |
| --- | --- |
| Purpose | Produce the mandatory think-first solution, ordered concrete steps and estimate before branching or editing; reuse an evidenced parent plan for a sub-task. |
| Capability | [solution-planning](../../../.agents/skills/solution-planning/SKILL.md) |
| Reads | Agreed atomic requirements, scoped paths, layering, testing exception, existing parent design and team estimate policy. |
| Allowed actions | Design alternatives, estimate in Vietnamese, prepare or publish authorized Jira plan/estimate/labels/requested sub-tasks; interview unresolved decisions. |
| Forbidden actions | Source edits, branch creation, status transitions or invented worklog time. |
| Dependencies | analyze, only when routed into this run. |
| Routing | `always` in the JSON workflow. |
| Retained retry policy | One design pass; rejection requires changed decisions/input or an interview. |
| Native attempt ceiling | 3; this upper bound does not authorize extra attempts contrary to the retained policy. |
| Success/evidence | Concrete Vietnamese plan/estimate, alternatives/reason, approval and parent-plan reference if reused; tracker result or explicit local draft/unavailability. |

Record pass/fail/blocked with an actual evidence summary through `graph.py`;
results include the current fingerprint and attempt. Do not write Claude state
fields. Failure stops dependent nodes; correction belongs to the capability
that owns the gap. Use a reasoned `retry` only after new corrective work/input,
within the retained policy; it cannot bypass red or stale verifier evidence.
See [graph contract](../README.md) and [ledger](../../references/workflow.md).
