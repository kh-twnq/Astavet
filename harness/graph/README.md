# Workflow graph

Three machine-readable definitions: ticket, bugfix and read-only release.
Ten node contracts mirror intake/analyze/plan/implement/integrate/test/review/
security/ship/release-audit. JSON replaces YAML so the runtime uses only Python's
standard library. This is an evidence/dependency runner, not an autonomous DAG
scheduler or a substitute for the capability skills.

Initialize the task ledger with harness.py start, then:

```sh
python3 harness/scripts/graph.py init TASK --workflow ticket --security
python3 harness/scripts/graph.py record TASK intake --status pass --summary "Objective and base captured"
python3 harness/scripts/graph.py record TASK analyze --status pass --summary "Scope and consumers traced"
python3 harness/scripts/graph.py record TASK plan --status pass --summary "Concrete plan and checks established"
python3 harness/scripts/graph.py record TASK implement --status pass --summary "Scoped changes implemented"
```

Stage intended files, run harness.py verify and record the actual review verdict.
Then record the test, review and routed security nodes with evidence summaries.
A test/review node cannot pass without the matching verifier/review ledger.
Security requires a clean secret scan plus an actual security-review declaration.
It does not automatically prove auth/injection logic safe.

For multiple repos use `--repos api web --cross-repo-contract`; integrate is then
mandatory. Run each repo's verifier with workspace.py, inspect every declared
contract/consumer and record integrate evidence before downstream test/review.
Integration also binds each configured repo’s current verification fingerprint;
a later consumer edit invalidates readiness. A dependency that already shipped
can supply evidence only when its clean tree matches the verified snapshot.
A passing declaration is evidence supplied by the agent, not a protocol checker.

`ready` enforces all routed dependencies and current integrate/test/review/security
fingerprints. Failed upstream results invalidate downstream conclusions. Attempt
limits require a new corrective-work reason through `graph.py retry TASK NODE
--reason "..."`; this never overrides failed tests or stale readiness checks.

Bugfix routes test unconditionally and calls bug-investigation for analyze.
A docs-only ticket may skip the graph test node; the ledger still requires explicit
configured validation or allow_no_tests. Plan stays enabled to record the mandatory think-first pass. For a sub-task, reuse an evidenced parent plan without re-estimating; record its source in the plan result. Retained retry policies are specified in each node contract; the native JSON ceiling is not permission for extra blind attempts. The frontend new-unit-test exception does not waive existing verification.

Release routes only release_audit and cannot ship. Use completion-audit to inspect
acceptance criteria and target content; local workspace audit output is a lead,
not a remote merge/deploy verdict. Graph results live in the same task ledger,
with schemas in ../schemas, not in a second competing state file.
