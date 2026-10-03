# Codex harness for multiple projects

AstaVet hosts the harness source and its project overlay. The reusable harness
installs into the user's Codex home; it has no dependency on AstaVet paths after
installation. Project AGENTS.md, domain rules, wrappers and tests remain local.

## Layers

| Layer | Source | Installed/runtime scope |
| --- | --- | --- |
| Core workflow | `config/ecc/portable/AGENTS.md` | Managed section in `$CODEX_HOME/AGENTS.md` (default `~/.codex`) |
| Generic roles | `config/ecc/portable/*.toml` | `ecc_explorer`, `ecc_reviewer`, `ecc_build_resolver` in user config |
| Adapter/router | `scripts/codex-harness.mjs` | `$CODEX_HOME/ecc-harness/route.mjs` |
| Rules | Pinned common/Java/TypeScript Markdown | Portable package, selected on demand |
| Skills | Native `ecc@ecc` plugin | Namespaced `ecc:` catalog across projects |
| Runtime | Portable Node selector and MCP launcher | User-level Chrome DevTools replacement, no Git-root lookup |
| Audit | `scripts/audit-codex.py` | Copied into portable package; accepts any target directory |
| Project overlay | Each project's AGENTS/config/checks | Overrides generic workflow examples and domain assumptions |

No model, model provider, approval policy, sandbox setting or hook trust is set
by this installer. Roles describe authorized delegation; installation does not
spawn agents. Existing project routing takes precedence over the generic router.
AstaVet inherits the generic MCP from user config; it declares no additional
project launcher or incomplete `enabled = false` server entry.

## Install and update

Prerequisites: Codex CLI, enabled native ECC plugin, Python 3.11+, and compatible
Node (20.19+, 22.12+, or 23+). The launchers select installed Node automatically;
`ECC_NODE=/absolute/path/to/node` overrides selection and fails if incompatible.
The installer itself does not fetch packages or install/upgrade ECC. Use the
existing native plugin setup separately when the plugin is absent.

From this source repository:

```bash
# Use python3.11 if your default python3 is older.
python3 scripts/install-codex-harness.py
python3 scripts/install-codex-harness.py --apply

# Optional: install to an isolated Codex home for validation.
python3 scripts/install-codex-harness.py --codex-home /tmp/codex-fixture --apply
```

Preview is read-only. Apply writes self-contained assets and two marked sections;
personal text outside those sections is preserved. Original existing AGENTS.md
and config.toml are backed up once to `ecc-harness/backups/` with mode 0600.
Repeated apply updates unchanged managed assets; locally edited managed files,
conflicting role/server names, malformed markers, symlink destinations and a
shadowing user AGENTS.override.md stop installation. There is no force overwrite.
On write failure, modified destination files are restored from in-memory
snapshots; new directories/backups may remain. Run preview again after resolving
a conflict. No automatic update runs at session startup.

Reload Codex after installation. The current session may still use its old
instructions and tool inventory. Plugin hook trust must be reviewed in Codex;
installation neither grants nor bypasses it.

## Use from a different project

```bash
cd /path/to/project
bash "${CODEX_HOME:-$HOME/.codex}/ecc-harness/bin/route" route \
  --project "$PWD" --behavior -- src/changed-file.ts
python3 "${CODEX_HOME:-$HOME/.codex}/ecc-harness/audit-codex.py" \
  --project "$PWD"
# Also inspect MCP startup; this does not invoke browser actions.
python3 "${CODEX_HOME:-$HOME/.codex}/ecc-harness/audit-codex.py" \
  --project "$PWD" --mcp
```

Routine users describe the desired result; the agent follows the workflow and
selects guidance. They need not manually invoke every skill. Routing returns
`status`, `summary`, rules, skills, proposed checks (`cwd` plus argument array),
warnings, `next_actions` and artifacts. It never executes project code.

## Adapters and fallback

- Java/Gradle and Maven: detect build manifests, offer existing local wrappers;
  Spring guidance is selected when the manifest identifies Spring Boot.
- Node/React/Next.js: choose declared package manager or an unambiguous lockfile;
  offer only test/lint/typecheck/build/coverage/E2E script names actually present.
  Conflicting/unsupported managers withhold commands and request inspection.
- Changed paths select their nearest detected module; sibling modules do not
  contribute their commands. With no paths, all detected modules are listed.
- Discovery is bounded to root plus two levels, excludes generated/tool trees
  and does not follow directory symlinks. Deeper modules, parent workspace
  wrappers and nonstandard manifests need the project's own routing/commands.
- Unknown stacks receive core rules and instructions to inspect their runner;
  the router does not invent commands or claim every language has an adapter.
- Auth/migration/behavior flags add relevant guidance. Database-sensitive work
  still requires the target project's disposable database and integration tests.

Package scripts and wrappers are untrusted project input. Review their effects
before execution; argument arrays are not an authorization or a safety proof.
Source routing rejects traversal, external/broken symlinks and invalid manifests.
It reads no other project's memory. The upstream hook has its own context logic;
that is separate from the router's isolation guarantee.

## Verification and evidence

`bash scripts/verify.sh ecc` runs pinned-source integrity, local graph validation,
both project and portable regression suites, and whitespace checks. The wrapper
selects compatible Node even when the shell's default Node is obsolete.

```bash
node --experimental-test-coverage --test scripts/tests/codex-portable.test.mjs
```

Fixture tests cover Java/Next/Maven/unknown stacks, manager conflicts, module
selection, path boundaries, install preservation/idempotence, modified-file
refusals and audit cwd/protocol. The installed launcher is executed from a
different project directory. These are deterministic harness checks, not model
completion benchmarks. Node's report measures the JavaScript router in the test
process; it does not measure Python installer or application coverage.

Native config/discovery/prompt and MCP smoke checks must be repeated after
installation or Codex upgrades. Do not equate registration/trust with hook
execution, discovered browser tools with successful browser behavior, or an eval
utility receipt with safe candidate containment. Preserve `gate.isolation_required`.

The [2026-10-03 runtime receipt](evals/portable-runtime-2026-10-03.json) records
27 passed regression tests, independent Java/Next/unknown project discovery,
prompt inheritance, installed asset integrity and the 30-tool global MCP smoke.
It contains selected metadata rather than personal config or prompt contents.

To measure agent effectiveness, define representative target-project tasks,
independent graders and isolated execution first; record attempts, completion,
retries, tokens/cost and latency. Report pass@k/pass^k only from actual model
trials. The current deterministic suite does not supply those measurements.

## Remove or roll back

First review the installed manifest and your current files. Remove only the
marked portable sections from user AGENTS.md/config.toml and the owned package
when uninstalling. Preserve any subsequent personal edits. Initial backups are
for recovery, not an instruction to overwrite newer user configuration blindly.
Do not remove the native ECC plugin or change hook trust unless intended.
