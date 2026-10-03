# ECC in AstaVet

Full ECC **2.2.3** is installed and enabled through Codex's native plugin lifecycle
as `ecc@ecc`. The installed bundle contains **293 skills, 122 rule documents,
68 agent references**, MCP configuration and the native Codex hook manifest.
Codex CLI 0.160.0 discovers **328 namespaced ECC skills**, including 35 generated
command adapters in addition to the 293 source skill directories. See [AUDIT.md](AUDIT.md)
for the fresh upstream comparison and actual runtime checks.
The verified upstream revision is `ef648e01899ba3e8dc6371642deaaf64b4477775`.
Installation evidence is recorded in `config/ecc/plugin-lock.json`; the user
plugin cache itself is managed by Codex and is not committed to this repository.

AstaVet additionally keeps a pinned project baseline for team use and offline CI.
The MIT license is in `tools/ecc/LICENSE`. This installation does not certify
application correctness, coverage or dependency security.

## Configuration surfaces

| Surface | Location | Behavior |
| --- | --- | --- |
| Native full plugin | `ecc@ecc` in Codex's plugin cache | Complete upstream skills, rules, role references, MCP and hook declarations |
| Project instructions | `AGENTS.md`, `backend/AGENTS.md` | AstaVet workflow, rule selection and domain invariants |
| Codex config | `.codex/config.toml` | Sandbox, approval defaults, multi-agent limits and registered roles |
| Native Codex roles | `.codex/agents/*.toml` | Explorer, Java reviewer/build resolver, security reviewer, harness optimizer |
| Routing graph | `config/ecc/harness.json` | File patterns → rules → skills → roles → checks; ordered workflow phases |
| Graph visualization | [GRAPH.md](GRAPH.md) | Generated Mermaid view of the configuration graph |
| Repo-native baseline skills | `.agents/skills/` | 16 pinned skills needed by project routes, available without global plugin installation |
| Active rule references | `docs/ecc/rules/common/`, `docs/ecc/rules/java/` | Selected by routing and loaded by the agent as instructed in AGENTS.md |
| Upstream config catalogs | `tools/ecc/config/`, `tools/ecc/manifests/`, `tools/ecc/schemas/` | Unchanged upstream stack mappings, profiles, components, modules, assets and schemas |
| Source graph implementation | `tools/ecc/scripts/lib/agent-proximity/`, `scripts/ecc-harness.mjs` | Upstream JS/TS import graph plus a project Java import adapter |
| Harness utilities | `tools/ecc/scripts/` | Upstream configuration audit and eval capsule/receipt tools |
| Source integrity | `tools/ecc/manifest.json` | Commit, paths and SHA-256 hashes for 90 upstream files |
| Verification + CI | `scripts/verify.sh`, `.github/workflows/verify.yml` | Integrity, graph validation/tests, build/tests and disposable PostgreSQL integration |
| Memory and acceptance criteria | `MEMORY.md`, `evals/harness.md` | Durable decisions and testable harness expectations |

Upstream profiles/modules are catalogs, not commands automatically executed in
AstaVet. The project routing graph is the active local adaptation. Similarly,
upstream agent Markdown documents are references; the five TOML roles are the
registered native Codex agents. Delegation is only used when authorized.

## Daily workflow

The default is **understand → implement → verify/review**. See [DAILY.md](DAILY.md)
for the user-facing workflow and short task prompts.

1. Identify the affected repository-relative paths.
2. Select relevant rules, skills, role guidance and checks:

   ```bash
   node scripts/ecc-harness.mjs route backend/src/main/java/com/astavet/service/order/OrderService.java
   ```

3. Read the returned default rules/skills. Activate `skillOptions` only when their
   trigger matches the task. Write behavioral regression tests, implement and
   run checks for the affected scope.
4. Review the diff and report passed, failed and skipped checks separately.

Routing is an explicit tool call directed by AGENTS.md, not an invisible hook
trigger after every edit. It prints command argument arrays but does not execute
commands, migrate databases or spawn agents. The baseline contains only common coding-style, security and testing; Java,
API, persistence, security and Flyway paths receive targeted additional guidance.
A routine Java service selects 2 default skills and 5 rules, instead of the
previous 7 skills and 15 rules. TDD and deeper checks activate when relevant.

## Commands

Use **Node.js 20+** for verification (CI selects Node 22).

```bash
bash scripts/verify.sh ecc
bash scripts/verify.sh backend
bash scripts/verify.sh frontend
bash scripts/verify.sh all
node scripts/ecc-harness.mjs validate
node scripts/ecc-harness.mjs graph --write
node scripts/ecc-harness.mjs deps
python3 scripts/audit-codex.py       # Read effective config, skills and hook trust
python3 scripts/audit-codex.py --mcp # Also start configured MCP servers
node tools/ecc/scripts/harness-audit.js --root . --format json
node tools/ecc/scripts/eval-harness.js example
```

`deps` emits JSON to stdout. Java uses explicit imports; JS/TS uses ECC's relative
import scanner. Same-package Java references, Spring runtime wiring, TS aliases
and dynamically resolved dependencies are omitted. It is not a call graph.
The eval example creates and cleans a temporary directory; use `--keep` directly
with the example script to retain artifacts.

PostgreSQL integration requires `ASTAVET_TEST_DATABASE_URL`,
`ASTAVET_TEST_DATABASE_USERNAME` and `ASTAVET_TEST_DATABASE_PASSWORD`. Set them
for a **disposable test database**, then run `bash scripts/verify.sh integration`.
Test setup deletes records. Regular backend verification reports skips when no
URL is configured; CI provisions an isolated PostgreSQL 15 service.

## Native plugin setup and trust

The official installation for another development machine is:

```bash
bash scripts/setup-ecc.sh
codex plugin list --json
```

The setup script writes Codex user plugin state, needs network access and uses
the native marketplace/add commands. It refreshes to the marketplace's current
version; compare it with `config/ecc/plugin-lock.json` before accepting an update.
The repository baseline remains pinned independently of plugin updates.

Reload/reopen Codex to use the installed full plugin and native role settings.
**Review native hook trust in Codex**: this integration did not manufacture or
grant hook trust. ECC's Codex hook is the upstream SessionStart bootstrap;
Claude's hook profiles and events are not mapped onto Codex. The upstream plugin declares Chrome DevTools MCP. This project replaces only
its launcher with `scripts/ecc-mcp.sh`, which selects an installed runtime matching
the package engine requirement (Node ^20.19, ^22.12 or >=23). The plugin launcher
is disabled only in this project, and `astavet-chrome-devtools` starts the same
pinned package. Set `ASTAVET_MCP_NODE` to an executable for nonstandard Node
installations. Startup was verified with 30 tools; browser actions were not invoked.

Plugin skills have runtime names such as `ecc:springboot-patterns`; the repo
fallback is named `springboot-patterns`. These 16 pairs overlap in content, not
in runtime identifier. Routing returns `skillBindings` with the preferred repo
file and namespaced plugin alternative. Load one source per task; use plugin
skills for additional ECC workflows. Keep user model settings inherited. No model is pinned by AstaVet.

See [official plugin documentation](https://learn.chatgpt.com/docs/plugins),
[skills discovery](https://learn.chatgpt.com/docs/build-skills) and
[Codex configuration reference](https://learn.chatgpt.com/docs/config-file/config-reference).

## Framework adaptations and verification limits

- Gradle, existing npm scripts and Boot 4 APIs take precedence over generic
  Maven, Node back-end, outdated Spring and Claude command examples.
- Upstream files stay unchanged for source verification. Project behavior lives
  in AGENTS.md, native role TOML, routing config and project scripts.
- Rules are loaded through the routed agent workflow; Claude `paths` frontmatter
  is not automatically interpreted by Codex.
- JaCoCo, Checkstyle, PMD, SpotBugs and CVE scanners are not configured in the
  application build. Their installed ECC guidance does not mean those checks ran.
- Upstream audit scores include Claude-specific assumptions; do not optimize
  the score by introducing nonfunctional Claude configuration.
- ECC candidate execution still refuses with `gate.isolation_required` on every
  OS. Capsule/receipt integrity and utility tests do not prove safe candidate
  containment, benchmark scores or promotion readiness.

## Updating

Use native plugin marketplace upgrade/add for the user plugin. For repository
baseline updates, separately review the selected upstream files, replace mapped
files, and regenerate hashes and commit in `tools/ecc/manifest.json`. Review
routing and role changes separately. Regenerate `GRAPH.md`, then run integrity,
harness regression tests and relevant product checks. The manifest is a local
review baseline, not a cryptographic signature.

ECC coding-guideline Markdown files are distinct from Codex native `.rules`
execution-policy files. AstaVet loads coding guidelines by explicit instructions;
this integration does not convert them into command allowlists.
