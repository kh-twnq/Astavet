# Codex adaptation contract

This harness retains the source Claude harness's project policies. The port changes
client syntax, paths and runtime integration; it does not silently replace those policies.
User instructions and active system/developer constraints take precedence.

## Policy and authorization

Read the current code, scope and rules before implementation. Present a concrete plan
and obtain approval before source edits. Approval already given in the conversation
satisfies that gate; do not ask again for the same approved work. Confirm the branch base
before creating a branch, reusing explicit confirmation already supplied. A diagnosis,
review or audit request remains read-only on source.

Retain the no-comment policy, frontend unit-test-writing exception, Vietnamese planning,
Jira estimate/role/sub-task conventions and environment-specific release-note rules.
These are policies of this harness, not defaults imposed by Codex.

Tracker write-back is the default planning workflow when the user has authorized it and
the integration is available. Prepare the exact content first. Selecting a skill does
not itself authorize messaging, issue mutations, commit, push, tagging or publication.
Carry forward authorization already established for those operations. Missing tools or
access must produce a local result and an explicit unavailable status, not invented success.

## Client and tool translation

- Invoke a native skill as `$skill-name`; read linked capabilities in the same workflow.
- Use `AGENTS.md` for project instructions and Repository Map. Cross-cutting rules live
  in `harness/rules/`; component templates and navigation live in `harness/docs/rules/`.
- Read `harness/profiles/local.json` when present, otherwise `default.json`, plus
  `harness/profiles/default.md` for project identity and retained team policy.
- Claude model aliases and `/model` are not Codex model identifiers. Preserve the
  think-first/deep-review roles using the user's selected Codex model and available
  reasoning settings; see [model routing](../docs/rules/model-routing.md).
- Use the active Codex client's delegation tools when available and permitted. A source
  `Agent` or `Workflow` example is translated into a bounded read-only subagent task,
  awaited results, and orchestrator synthesis. No Claude `Workflow` engine is required.
- Tool names in tracker/navigation examples denote capabilities. Discover the active
  MCP/connector schema; do not fabricate a tool or copy Claude MCP configuration.
  Resolve the actual Atlassian cloud ID/site through the available integration rather
  than assuming a hostname is universally accepted. Follow pagination in every listing.
- Questions use the active client's supported mechanism; an asynchronous question must
  receive an answer before dependent work. A timeout is not approval.
- Fresh-session handoffs use `$handoff`; Claude `/clear` is not a prerequisite.

## Native evidence and hooks

The native ledger is `harness/state/<TASK>.json`. Do not write Claude state fields such as
`human_approval`, `final_status` or `completed_nodes` directly into this ledger. Use
`harness.py` and `graph.py` commands; see [ledger](workflow.md) and [graph](../graph/README.md).
Ledger helper skills supplement the source workflow; they do not bypass plan approval.
Reuse a verified parent plan for a sub-task and record that evidence for the plan node.

Run `python3 harness/scripts/harness.py --repo <repo-dir> scan` for the bundled secret
scan. It scans changed/untracked content and staged content, reports locations/categories
without token values, and exits nonzero for findings. It does not prove application
security. Hooks are configured by `.codex/hooks.json` and adapt native tool payloads.

Preserve unrelated user changes. Stage only intended files before final verification;
rerun verification and review after edits or staging changes. `ready` validates evidence
before commit. `ship --url` records a locally checked commit/review URL, not a remote
merge/deploy verdict. An audit stays read-only; inspect refs or isolated worktrees rather
than stashing and switching the user's active working tree.
