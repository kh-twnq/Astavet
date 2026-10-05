# Node: implement

| Field | Contract |
| --- | --- |
| Purpose | Make only the scoped source change after concrete plan approval; preserve unrelated work and match per-repo rules. |
| Capability | [change-implementation](../../../.agents/skills/change-implementation/SKILL.md) |
| Reads | Approved plan/base, current code/diff, consumers, Java gate, component rules and toolchain. |
| Allowed actions | Implement approved scope; run build/lint/checks; report touched files/risks. Independent repos can proceed concurrently only when contract/migration ordering permits and delegation is allowed. |
| Forbidden actions | Unapproved scope expansion, incidental refactors, comments/docstrings outside allowed exceptions, publication without authorization, final self-approval replacing review. |
| Dependencies | plan, only when routed into this run. |
| Routing | `always` in the JSON workflow. |
| Retained retry policy | Two corrective attempts per repo for the same problem; escalate persistent failure. |
| Native attempt ceiling | 3; this upper bound does not authorize extra attempts contrary to the retained policy. |
| Success/evidence | Scoped diff, files/risks and approved-plan reference. |

Record pass/fail/blocked with an actual evidence summary through `graph.py`;
results include the current fingerprint and attempt. Do not write Claude state
fields. Failure stops dependent nodes; correction belongs to the capability
that owns the gap. Use a reasoned `retry` only after new corrective work/input,
within the retained policy; it cannot bypass red or stale verifier evidence.
See [graph contract](../README.md) and [ledger](../../references/workflow.md).
