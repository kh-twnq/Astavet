# Rules — GitNexus (code knowledge graph)

**Single source of truth for how skills use GitNexus in this workspace.** Skills link here instead of
restating tool usage. GitNexus is an MCP server + per-repo knowledge graph (symbols, calls, imports,
routes, processes) that replaces blind grep-crawling for navigation, tracing, and impact questions.

## What's indexed

Each repo is indexed **individually** — every repo has its own `.gitnexus/` directory and registry entry
(a multi-repo workspace is not a monorepo; there is **no workspace-root index**). Use the registered repo name reported by the integration.

- Inspect the actual index capabilities. Embeddings and PDG/taint (`--pdg`) may be absent; `explain` and `pdg_query` need a built layer. Do not claim a layer exists from this template.
- Small repos (e.g. migration repos that are mostly SQL changelogs, or a handful-of-files service) get
  little from the graph — plain file reads are fine there.
- **Reindexing the whole workspace**: run `analyze -f --skip-agents-md --skip-skills` per repo. The
  two skip flags matter — without them `analyze` rewrites each repo's `AGENTS.md`/`AGENTS.md` and
  `.agents/skills/`, which dirties working trees that are mid-ticket (and in some repos those files
  are tracked).

## Freshness (check before trusting results)

The index is a snapshot of one commit + branch (visible in `gitnexus://repo/{name}/context` and
`node .gitnexus/run.cjs status`). It goes **stale after a commit, merge, or branch switch** in that repo.
- Stale → run `node .gitnexus/run.cjs analyze` **inside that repo** (regenerates in place).
- Uncommitted working-tree edits are *not* in the graph — combine `detect_changes` (graph-aware diff
  impact) with `git diff` for review of in-flight work.
- MCP connection note: configure GitNexus through the active Codex client or its supported MCP settings; use a compatible Node runtime. Do not copy `~/.claude.json` into this harness.

## Which tool for which phase

| Phase / question | Tool(s) | Instead of |
|------------------|---------|-----------|
| Scope a ticket — "where does this live?" | `query` (concept → flows), `gitnexus://repo/{name}/clusters` | broad grep across `src/` |
| Understand a symbol before editing | `context` (360° refs + processes) | reading every caller by hand |
| Root-cause a bug — "how does A reach B?" | `trace` (shortest CALLS path), `context` | manually chaining callers |
| Blast radius — "what breaks if I change X?" | `impact` (depth 1–3 + confidence) | grep for the name |
| Review a diff / pre-MR | `detect_changes` (git-diff impact), `impact` on changed symbols | eyeballing consumers |
| REST route ↔ consumer mapping | `route_map`, `api_impact`, `shape_check` | hand-matching services ↔ controllers |
| Rename / move / extract | `rename` (coordinated multi-file edits), `impact` first | find-and-replace |
| Anything structural the tools don't cover | `cypher` (read `gitnexus://repo/{name}/schema` first) | — |

Full tool reference + workflows live in the **global `gitnexus-*` skills** (`gitnexus-guide`,
`gitnexus-exploring`, `gitnexus-debugging`, `gitnexus-impact-analysis`, `gitnexus-refactoring`,
`gitnexus-cli`) — load the one matching the task.

## Cross-repo: a contract group + Contract Registry (what it can and cannot do)

Each per-repo graph stops at its repo boundary. If your workspace has HTTP contracts spanning repos,
a **GitNexus group** (`~/.gitnexus/groups/<group>/`) can link a provider service and its consumers
through a **Contract Registry** (`contracts.json`): every extracted HTTP provider route plus consumer
links, cross-linked with provenance down to the provider method's `file:line`.

**How the consumer side works (and its limit):** GitNexus's built-in TS consumer detection only
recognizes `axios`/`fetch`, so a custom HTTP-request wrapper (as many frontends use) yields zero
auto-detected consumers. In that case the consumer side is **generated** by a sync script that matches
the frontend's endpoint-constant file against the extracted provider routes and writes manifest
`links:` into `group.yaml`; endpoints that build URLs dynamically at call sites stay manual.

| Question | Works? | How |
|----------|--------|-----|
| "Who serves `GET /v1/users/{id}`?" (route → provider method `file:line`) | ✅ | `group contracts <group>` / `group_list`+`contracts.json`, or `route_map`/`api_impact` on the provider repo |
| "Does the frontend consume this route at all?" | ✅ for the registry-linked endpoints | cross-link in `contracts.json` names the frontend endpoint constant path |
| "Which frontend components/hooks break?" (consumer-side symbol fan-out) | ❌ | manifest consumers are synthetic nodes — `group impact` reports the frontend as `truncated` instead of fanning out. Grep the endpoint constant and trace its uses with the frontend repo's own `context`/`impact` |
| **A second frontend/consumer that is not a group member** consuming a provider route | ❌ | its endpoints are never in `contracts.json`. Always grep that consumer by hand before calling a route/DTO change safe |
| IDE-ext↔service, WS/STOMP/SSE contracts, dynamic-URL endpoints | ❌ | manual check per `workspace.md`, unchanged |

**Maintenance:** after `analyze` on any member repo, re-run the sync script then
`gitnexus group sync <group>` (check `group status <group>` for staleness). The script is idempotent.

**The rule stands:** never let a clean single-repo `impact` result — or a registry miss on a
dynamic-URL endpoint — be claimed as proof that a DTO/route change is safe across tiers. The
registry upgrades a subset of the manual check into a deterministic lookup; everything outside it
remains manual.

## Rules

1. **Prefer fresh graph queries for structural navigation; use rg for text/file search rather than blind grep** for navigation/tracing/impact in the code-heavy repos; fall
   back to grep/Glob when the graph lacks the answer (config files, YAML, SQL, comments).
2. **Check freshness first** (`gitnexus://repo/{name}/context`) when results will drive a decision;
   re-analyze after switching branches — an index built on another branch silently lies.
3. GitNexus output is **evidence to verify, not proof**: confidence-tagged edges can be wrong; confirm
   at `file:line` by reading the code before reporting a finding (matches the code-review rule).
4. Cross-tier impact: check the contract group's Contract Registry first (see above); anything the
   registry doesn't cover keeps the manual contract-sync check.
5. `rename` proposes edits — review them like any diff (approval gate in `change-implementation` still
   applies; no source edits before plan approval).
6. Don't run `analyze` mid-flight of someone else's long task, and never at the workspace root — always
   inside one repo.
7. **Worker MCP access depends on the active Codex client.** Before delegating a structural review, gather `impact`/`detect_changes`/`trace` or equivalent rg + code evidence in the orchestrator and include it in the packet. Workers must verify that evidence; do not assume Claude tool allowlists or automatic tool discovery apply to Codex.
8. **Ground evaluator claims in the graph** — a review/audit finding that asserts a structural
   relationship ("nothing calls X", "this breaks Y", "ticket A and B collide on Z") must cite a graph
   query result or an explicit grep + read confirmed at `file:line`; otherwise report it as
   *Unknown / needs confirmation*. (Mirrored in `code-review` and `completion-audit`.)
