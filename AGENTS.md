# AstaVet daily workflow

AstaVet uses Next.js (`frontend/`), Java 21 / Spring Boot 4.1 / Gradle
(`backend/`), PostgreSQL and Flyway. Read README.md for setup and domain rules.
Use the existing wrappers, npm lockfile and project APIs.

## Three steps

1. **Understand:** inspect the actual call path and tests; state the expected
   result and a short plan. Routine work needs no separate planning document.
2. **Implement:** make a scoped change. For behavioral fixes/features, load the
   relevant TDD skill and add meaningful regression tests before implementation.
   For documentation, formatting and similarly low-impact edits, use suitable
   lightweight checks instead of adding implementation-mirroring tests.
3. **Verify and review:** run affected checks, review correctness/security/API
   compatibility, then report the change, checks and remaining limitations.

The user describes the outcome; the agent owns routing, skill selection and
verification. Ask for clarification only when missing information changes the
intended result or blocks correct implementation. Keep routine plans and final
reports short. Use one agent by default; delegation requires authorization.

## Skills and rules on demand

After identifying affected files, run
`node scripts/ecc-harness.mjs route <repo-relative-files...>` once. Re-route only
when the scope changes. Read the returned baseline rules and skill bindings.
Read `skillOptions` only when the `when` condition matches the actual task.
Typical Java work uses `java-coding-standards`, plus `jpa-patterns` for persistence.
Behavior changes add `springboot-tdd`; auth changes use `springboot-security`;
schema changes use `database-migrations` and `postgres-patterns`.
Use `springboot-verification` for deep checks, major upgrades or release work.

Use `skillBindings.preferredSource` (the pinned repo file) or its namespaced
`ecc:<skill>` alternative, never both for the same task. Do not load every ECC
skill, all common rules, graph diagnostics or role documents for routine work.
Local instructions and the user's request take precedence over generic upstream
examples. Translate Maven/Claude/outdated Spring examples to the actual stack.

## Verification

Run `bash scripts/verify.sh backend`, `frontend` or `ecc` for the affected scope;
use `all` only for changes affecting both application modules. PostgreSQL
integration is required for relevant schema, locking, stock or transactional
behavior changes; use a disposable database with `ASTAVET_TEST_DATABASE_*` set.
Tests delete data. Report missing-database skips explicitly. Do not claim static
analysis, coverage, CVE scans or unexecuted checks passed. Aim for meaningful
80%+ coverage of changed service/domain behavior; the build has no coverage gate.

## Domain invariants

- Recalculate prices/totals on the backend; reserve stock transactionally and
  prevent duplicate orders. Cancellation restores stock exactly once.
- Preserve documented order/payment transitions and audit actor/time.
- Preserve session auth, CSRF, constrained CORS and admin authorization.
- Validate DTOs, parameterize queries, avoid N+1 access and keep secrets/PII out
  of logs and commits. Add Flyway migrations; do not rewrite existing ones.

## Harness references

ECC plugin evidence: `config/ecc/plugin-lock.json`. Pinned source/license/hashes:
`tools/ecc/manifest.json`. Routing: `config/ecc/harness.json`. Daily examples:
`docs/ecc/DAILY.md`. Integration/audit/graph: `docs/ecc/README.md`, `AUDIT.md`,
`GRAPH.md`. Load those diagnostics only for harness/configuration tasks.
Native roles live in `.codex/agents/`; source graph inspection is available via
`node scripts/ecc-harness.mjs deps`. Use primary docs for uncertain framework APIs.
Upstream audit scores and eval receipts do not prove product readiness or safe
candidate isolation. Preserve `gate.isolation_required` refusals.
