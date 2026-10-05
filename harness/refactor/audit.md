# Port audit

The first 0.1.0 release was a five-skill MVP and omitted most source capabilities.
0.2.0 mapped 115 sources but omitted the workspace template. Version 0.2.1
maps all 116 existing tracked source files, compared against the independent
source-inventory.json snapshot and optionally the live checkout. Source hashes
identify the working-tree snapshot; targets adapt behavior to Codex rather than
copying Claude APIs, model aliases and implicit external writes.

Run `python3 harness/scripts/check_parity.py` to check all mapped targets and the
native discovery manifest. Use `--source /absolute/path/to/claude` to check
current source inventory and digests. Run `check_native.py` separately for native
skill discovery and project config loading; it fails when project trust disables
config. Actual hook dispatch, agent behavior and external workflows still need
a trusted live session. Active runtime mechanics are exercised through deterministic
ledger/graph/workspace tests; language/stack teaching remains progressively loaded
Markdown. Connector-specific actions require an authenticated configured integration
and a live pilot; file coverage does not prove semantic parity or remote behavior.

## Markdown semantic repair

The earlier target manifests established file coverage but many capability bodies
were reduced to summaries, losing procedure, checklists, examples and source policy.
The current repair restores the 16 capability workflows, five lifecycle entrypoints,
five cross-cutting rules, profile, graph-node responsibilities and navigation detail.
It retains no-comment policy, concrete-plan/base approval, Vietnamese planning,
10–20% contingency, 1d = 8h / 1 SP = 4h, Jira labels/requested self-assigned sub-tasks,
frontend new-unit-test exception and environment-specific release-note rules.

Tool adaptations remain explicit: commands become `$skill` entrypoints, paths use
AGENTS.md and harness/.agents/.codex, native roles inherit user models, and scan/state
operations call the existing Python runtime. Authoring guidance uses required
name/description and `agents/openai.yaml` invocation policy. Broken links, leftover
XML and Claude Workflow scheduler/state fields are removed from active guidance.
Security references also correct unrestricted Jackson default-typing examples,
contradictory CSRF configuration, a wrong Quarkus namespace, incomplete SSRF
claims and an unclosed code fence, with primary-source references.
Read-only audits inspect refs/isolated worktrees instead of changing the user's
branch/index; stale-index errors require investigation rather than forced restoration.
Native graph ceilings are documented separately from the source's stricter retry
policy. Existing approval carries forward, and external tool permissions still apply.

See [Codex adaptation](../references/codex-adaptation.md) for the maintained contract.
Runtime caches, settings.local.json, histories, sessions and credentials are never
migrated. Retired source skills remain outside discovery in harness/archive.
