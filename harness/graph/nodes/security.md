# Node: security

| Field | Contract |
| --- | --- |
| Purpose | Deep OWASP/input/query/auth review when routed, plus the unconditional secret-scan gate at ship. |
| Capability | [security-review](../../../.agents/skills/security-review/SKILL.md) |
| Reads | Actual diff, secret-scan result and relevant input-validation/web-output/auth-secrets references. |
| Allowed actions | Run native scan; execute relevant OWASP checklist; share one deep-reviewer with other deep lenses. |
| Forbidden actions | Editing source, overriding credential findings without remediation, declaring logic secure from a clean scan alone. |
| Dependencies | implement, integrate, only when routed into this run. |
| Routing | `security` in the JSON workflow. |
| Retained retry policy | Deterministic scan is rerun only after a relevant change; one checklist re-review after a fix. |
| Native attempt ceiling | 3; this upper bound does not authorize extra attempts contrary to the retained policy. |
| Success/evidence | Clean native secret scan plus actual security-review evidence tied to the current snapshot. |

Record pass/fail/blocked with an actual evidence summary through `graph.py`;
results include the current fingerprint and attempt. Do not write Claude state
fields. Failure stops dependent nodes; correction belongs to the capability
that owns the gap. Use a reasoned `retry` only after new corrective work/input,
within the retained policy; it cannot bypass red or stale verifier evidence.
See [graph contract](../README.md) and [ledger](../../references/workflow.md).
