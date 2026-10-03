# Harness acceptance criteria

Automated in `scripts/tests/ecc-harness.test.mjs` and `scripts/check-ecc.mjs`:

- Java service selects Java/JPA guidance and PostgreSQL verification.
- Flyway SQL selects migration guidance independently of Java extensions.
- Frontend changes do not load Java rules; mixed paths deduplicate references.
- Root instructions, Gradle files and Codex configs match the intended routes.
- Paths outside the repository are rejected.
- Missing assets, unknown checks and workflow cycles fail validation.
- Static source graph resolves explicit Java imports and JS relative imports,
  excludes generated dependency directories, and reports limitations.
- Pinned upstream copies match manifest SHA-256 hashes.
- Routing identifies the preferred repo skill file and its namespaced plugin alternative.
- An explicitly invalid MCP runtime is rejected before package startup.
- Native setup refuses success when provider inventory reports ECC disabled.

Additional installed-provider checks are local, not portable CI assumptions:

- `codex plugin list --json` reports `ecc@ecc` installed and enabled.
- The installed bundle contains its declared skills, MCP and native hook manifest.
- Hook execution is only enabled after Codex's explicit trust decision.

Actual product tests, coverage, security scan results and PostgreSQL checks are
separate evidence; these harness criteria do not certify product readiness.
