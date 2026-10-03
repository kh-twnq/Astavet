# Portable Codex workflow

Use the user's requested outcome and target project as the scope. Read its
AGENTS.md and README first. Project instructions override generic examples;
preserve personal preferences outside this managed section. Never transfer
domain invariants, memory or credentials from another project.

Understand → implement → verify/review. Trace the actual call path and tests,
state the expected behavior and a short plan. For behavior changes, load the
relevant TDD skill, run a meaningful failing regression before implementation,
then verify the fix. Documentation and formatting need lightweight checks.

After identifying affected paths, route once with:

```bash
bash "${CODEX_HOME:-$HOME/.codex}/ecc-harness/bin/route" route --project "$PWD" -- path/to/affected/file
```

Add `--behavior` before `--` for behavior changes, `--security` for security work.
Read only returned rules/skills relevant to the task. Prefer a project's pinned
skill when it declares one; otherwise use the matching `ecc:` plugin skill.
Load one source per skill. If the project declares its own router/checks, use
those and preserve its domain guidance. Unsupported stacks use the project's
documented APIs, dependency manager and verification commands; do not invent them.

Routing prints guidance and proposed checks, never executes commands. Inspect
package scripts and their side effects before running them. Use existing
wrappers/lockfiles. Review correctness, security, API compatibility and diff;
report actual passed, failed and skipped checks. Aim for meaningful 80%+ changed
behavior coverage where measurable; never claim coverage, E2E, scans, database
integration or benchmark success without evidence.

Use one agent by default; delegation requires authorization. Generic `ecc_explorer`,
`ecc_reviewer` and `ecc_build_resolver` roles are available when authorized.
Keep credentials/PII out of logs and artifacts, validate boundary input and use
parameterized queries. Keep sandbox and approval controls; do not bypass hook
trust or upstream `gate.isolation_required`. External publishing, deployments
and destructive operations need the authorization applicable to that action.

Keep personal notes at user scope and team/domain knowledge in the target
project's documentation. Never read another project's memory implicitly.
Record task acceptance criteria and RED/GREEN/check evidence in the final report
or existing project artifact. Do not claim pass@k from deterministic unit tests.
