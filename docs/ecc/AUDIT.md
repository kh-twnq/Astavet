# ECC / Codex integration audit

Historical integration snapshot. Current portable implementation, hook trust
observation and runtime evidence are documented in [PORTABLE.md](PORTABLE.md),
[the scope audit](AUDIT-2026-10-03.md) and
[the native smoke receipt](evals/portable-runtime-2026-10-03.json).
The counts and pending trust status below describe the earlier audit.

Refetched GitHub `affaan-m/ECC` and audited against Codex CLI **0.160.0**.
The fetched branch still points to `ef648e01899ba3e8dc6371642deaaf64b4477775`
(ECC **2.2.3**). No upstream revision update was required.

## Evidence

| Surface | Result | Evidence / limitation |
| --- | --- | --- |
| Upstream baseline | PASS | All 90 mapped files match bytes from freshly fetched `FETCH_HEAD` |
| Installed plugin baseline | PASS | All 90 mapped files also match the native plugin cache |
| Native install | PASS | `codex plugin list --json`: `ecc@ecc`, installed and enabled |
| Project config | PASS | App-server `config/read`: project layer present, no disabled reason; workspace-write / on-request |
| Native roles | PASS registration | All five named role config paths resolve in effective config; no agent was spawned for this audit |
| Root instructions | PASS | `codex debug prompt-input` contains AstaVet root AGENTS.md |
| Repo skill discovery | PASS | `skills/list`: 16 enabled repo skills, no skill parse errors |
| Plugin skill discovery | PASS | 328 enabled namespaced ECC skills, including 35 generated command adapters |
| Skill overlap | Explicit fallback | 16 repo names map to `ecc:<name>` alternatives; routing now selects an explicit source |
| Coding rules | Instruction-driven | Common/Java Markdown referenced through AGENTS.md and local routing; not native command-policy rules |
| Config graph | Local adaptation | AstaVet graph is not an upstream/native Codex auto-routing feature; it prints guidance/checks on demand |
| Native SessionStart hook | PENDING TRUST | `hooks/list`: source plugin, enabled, `trustStatus = untrusted`, no parse errors |
| MCP startup before repair | FAIL | Default Node 16 caused Chrome DevTools MCP handshake failure |
| MCP startup after repair | PASS | Same package 1.10.1, runtime selector, 30 discovered tools, no startup/tools error |
| Harness regression tests | PASS | 11 tests cover routing, source binding, invalid paths, graph references/cycles, imports, runtime rejection and setup validation |

The 293 source skill directories, 122 Markdown rules and 68 agent documents are
bundle inventory counts. They are not proof that every document is automatically
loaded. Native plugin components are declared in `.codex-plugin/plugin.json`;
the source agent Markdown is not equivalent to the five locally registered roles.

## Corrections applied

- Added explicit `skillBindings`: a pinned repo file plus its `ecc:` alternative.
- Updated runtime skill counts and clarified namespace/content overlap.
- Native setup now verifies the provider install result and independently
  checks installed/enabled state before reporting success.
- Changed legacy `agents.max_threads` to canonical
  `agents.max_concurrent_threads_per_session`; effective limit remains four.
- Added `scripts/ecc-mcp.sh` and a project MCP override. It selects a compatible
  Node executable instead of using the default unsupported Node 16. The
  original plugin launcher is disabled only in AstaVet, avoiding double startup.
- Added read-only `scripts/audit-codex.py` for repeatable config, skills, hooks
  and optional MCP startup inspection. It does not start model turns, approve
  hook trust or invoke browser tools.

## What remains unverified

Hook trust was deliberately not granted on the user's behalf. Review it in
Codex's hook UI before expecting bootstrap execution. Browser operations were
not exercised; discovering MCP tools proves handshake, not browser behavior.
No subagent/model rollout was started. GitHub CI, application coverage, static
analysis, CVE scanning and PostgreSQL integration are separate checks.

## Sources and compatibility

- [ECC source revision](https://github.com/affaan-m/ECC/tree/ef648e01899ba3e8dc6371642deaaf64b4477775)
- [Codex configuration reference](https://learn.chatgpt.com/docs/config-file/config-reference): project layers, role config files, concurrency limits and per-plugin MCP overrides.
- [Codex skills](https://learn.chatgpt.com/docs/build-skills): repository discovery and skill selection.
- [Codex plugins](https://learn.chatgpt.com/docs/plugins): skills/MCP/hooks and explicit hook trust.
- [Codex rules](https://learn.chatgpt.com/docs/agent-configuration/rules): `.rules` governs command execution outside the sandbox, not Java coding style.

Role registration was verified against this CLI's supported
`agents.<name>.config_file` configuration. Future Codex releases may use
different custom-agent discovery or schemas; repeat the runtime audit after
upgrading instead of relying only on TOML syntax validation.
