---
name: briefing
description: "Gather open reviews, in-progress tracker work and workspace Git state into a read-only session briefing."
---

# briefing

Read [the Codex adaptation contract](../../../harness/references/codex-adaptation.md) before using this workflow.

Gather three independent read-only views, then synthesize one actionable report.
Use the Repository Map/profile to resolve the actual repositories. When permitted
and available, run three bounded workers in parallel; otherwise gather the same
views sequentially. Each worker inherits the configured Codex model.

## View 1 — Open MRs/PRs

Inside every registered repo, query open reviews authored by the current user
using the available Git host CLI/MCP. GitLab example:
`glab mr list --author=@me --state=opened --output=json`.
Follow pagination. Capture review ID/title, source/target, URL and actual latest
pipeline status. Unavailable auth/network/tool is unknown, not zero open reviews.

## View 2 — Tracker work

Use the configured tracker. Jira examples:
`assignee = currentUser() AND status = "In Progress" ORDER BY updated DESC`
and `assignee = currentUser() AND status = "Review" ORDER BY updated DESC`.
Fetch all pages with summary/status/priority/updated/parent/issue type. Report key,
summary, status, parent and type. Match status names to the project's workflow.

## View 3 — Workspace Git state

For each repo (including Git worktrees where `.git` is a file), capture current
branch, dirty paths, upstream and ahead/behind counts. A missing upstream is
**unknown/not configured**, not zero commits ahead. Inspect GitNexus freshness
against the indexed ref and current HEAD/branch; timestamp-only checks are leads.
`python3 harness/scripts/workspace.py snapshot` supplies the local aggregate;
its availability/freshness hints do not prove remote pipeline or tracker state.

## Synthesis

Wait for all available views before summarizing. Use the user's local date.

| Open reviews | Repo | ID/title | Branch/target | Pipeline | URL |
| --- | --- | --- | --- | --- | --- |
| Tracker | Ticket | Summary | Status | Type/parent | |
| Git | Repo | Branch | Dirty | Ahead/behind | Index freshness |

Render these as separate tables when populated. Omit confirmed empty sections;
retain unavailable status explicitly. End with ≤6 actions, prioritized by failed
pipelines, blockers, waiting reviews, dirty work and stale navigation evidence.
No branch changes, ticket transitions, comments or publication in this skill.
