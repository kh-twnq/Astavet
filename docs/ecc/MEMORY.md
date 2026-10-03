# Durable harness decisions

- Full ECC is installed with Codex native plugin lifecycle, not Claude installers.
- The repository baseline stays pinned independently of the user plugin cache.
- Runtime plugin skill names are prefixed `ecc:`. The 16 repo skills are fallback
  copies with unprefixed names; select the explicit repo path, not both sources.
- Project graph routes select rules, skills, review roles and verification commands.
- Root and backend AGENTS.md preserve AstaVet stock, price, order and auth invariants.
- Source graph is static: JS/TS uses upstream relative import scanning; Java adds
  explicit import edges. It is not a complete Spring wiring graph or call graph.
- Native Codex hooks require provider trust. Do not manufacture trusted state.
- Evaluation candidate execution remains disabled by upstream until OS containment exists.
- Local Node defaults may be older than required. Verification uses Node 20+;
  CI selects Node 22. Do not accept a test runner that silently omits subtests.

- Daily workflow is understand → implement → verify/review. Routine tasks use
  the compact baseline and task-triggered skillOptions; no full catalog loading.
