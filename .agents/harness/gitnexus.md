# GitNexus workflow for AstaVet

Use this page for code understanding, impact analysis, refactoring, or a meaningful code diff. GitNexus is code-graph evidence; confirm important conclusions in current source. The project is indexed as **Astavet**. Read `gitnexus://repo/Astavet/context` for current index details and freshness.

Run the project launcher for the GitNexus CLI fallback. It selects an installed Node 22 or newer with GitNexus, or accepts `GITNEXUS_NODE` and `GITNEXUS_CLI` overrides:

```text
sh .agents/harness/gitnexus-launcher.sh <command>
```

The generated `node .gitnexus/run.cjs` examples may select an older Node shim from `PATH`. The project launcher selects a compatible runtime. If the index is stale, run `sh .agents/harness/gitnexus-launcher.sh analyze --index-only`. `.gitnexusrc` sets `indexOnly` to avoid regenerating skills/guidance.

## Before reading or editing code

- For read-only callers, dependencies, imports or execution flow, use `query({search_query: "concept"})` for concepts/flows or `context({name: "symbolName"})` for a named symbol. Search source directly when graph results are empty, unresolved, or when looking for literals.
- **Run upstream `impact` before editing a function, class, or method.** Use `impact({target: "symbolName", direction: "upstream"})` or the CLI `impact "symbolName" --direction upstream --repo .`. Report callers, processes, and risk. Do not substitute grep for this pre-edit graph step.
- Warn on HIGH or CRITICAL `risk` before editing. Never use `riskSharedAxes` to waive such a warning. MCP File omits axes; Graph-RAG expands File.
- Treat `risk: UNKNOWN` as unresolved. An empty caller set can reflect unresolved plain-object property access, dynamic dispatch or cross-language calls. Confirm with text search before treating a symbol as safe to change or delete. A missing graph edge is not proof a runtime path is absent.
- Never rename symbols with find-and-replace; use graph-aware `rename` and verify the result in source.
- For security reviews, `explain({target: "fileOrSymbol"})` lists taint findings when a `--pdg` layer exists. Missing PDG coverage must be reported.

## Before handoff or commit

- For a meaningful code diff, run `detect_changes({scope: "all"})` or CLI `detect-changes --scope all --repo .` before handoff, even if no commit is requested. `partial: true` or `truncated: true` is incomplete, not a clean result; rerun or report the limitation.
- Before any authorized commit, graph change analysis is mandatory. For regression comparison use `detect_changes({scope: "compare", base_ref: "main"})` or CLI `detect-changes --scope compare --base-ref "main" --repo .`.
- If the index is stale, a symbol unresolved, or coverage incomplete, search current source and report what the graph could not establish.

## Resources and skill routing

| Resource | Use |
| --- | --- |
| `gitnexus://repo/Astavet/context` | Overview and freshness |
| `gitnexus://repo/Astavet/clusters` | Functional areas |
| `gitnexus://repo/Astavet/processes` | Execution flows |
| `gitnexus://repo/Astavet/process/{name}` | Process trace |

Read the relevant skill: `.agents/skills/gitnexus-exploring/SKILL.md` for architecture/flows; `gitnexus-impact-analysis` for blast radius; `gitnexus-debugging` for errors; `gitnexus-refactoring` for renames or extraction; `gitnexus-guide` for tools/schema; `gitnexus-cli` for index/status/clean/wiki commands. Resolve skill paths under `.agents/skills/`.
