# Executable Codex graph

`graph_runner.py` runs a small, bounded execution graph over separate `codex exec` sessions. It works with a local project directory of any language. It reads that project's instructions during each node; it does not require GitNexus or project-specific skills.

```text
feature/fix: intake -> plan -> execute -> review -> complete
                          blocked -> stop     issues -> execute (at most one repair by default)
review:      intake -> review -> reviewed
```

The runner owns the edges and persists shared state in `state.json`. `plan` and `review` use the CLI's read-only filesystem sandbox; `execute` uses `workspace-write`. Each node returns structured JSON that the runner validates before routing. Node failures stop the graph. The review node is instructed not to edit. External MCP tools may have separate permissions, so review their access before using the runner with mutating integrations. No subagents are used.

## Run

From a terminal, or through Codex when a relevant global skill selects a graph run:

```bash
python3 .agents/harness/graph_runner.py --project . --mode fix --task 'Describe observed and expected behavior'
python3 .agents/harness/graph_runner.py --project . --mode feature --task-file /path/to/requirements.txt
python3 .agents/harness/graph_runner.py --project . --mode review --task 'Review the current diff'
```

In Codex chat, ordinary requests such as “Sửa lỗi lưu đơn hàng hai lần”, “Thêm bộ lọc giá”, or “Review diff hiện tại” are enough to make the matching skill eligible. The model selects skills from their descriptions, so automatic selection is not a hard guarantee; `$fix`, `$feature`, or `$review` remains an explicit fallback. The skill chooses the target from the request or current project and decides whether the task merits the graph.

The script prints the run's `state.json` path and final status. Its private temporary run directory also holds each node's structured result, JSONL CLI events, and stderr. Inspect these before relying on a reported check or verdict. A review with findings ends as `reviewed`; the findings appear in the output and state file.

Use `--max-repairs 0` to disable the repair edge, or `--max-repairs 2` for two attempts (maximum 3). `--timeout-seconds` bounds each Codex node (default 900). `--run-dir /path/to/new/private-directory` chooses a new state location. Keep it outside the project when possible so its logs do not enter the diff.
Use `--model MODEL` if the default CLI model is too costly for this workflow. Each node is a separate Codex session, so inspect its usage in the JSONL event files when cost matters.

The runner sets `approval_policy=never` and never bypasses the sandbox. A missing requirement, failed safety precondition, CLI error, invalid node response, or timeout stops the run and leaves evidence in `state.json`. Resolve the blocker and inspect the working tree before starting a new run; the runner does not automatically replay an interrupted edit. It does not commit, push, deploy, or publish.

This is an external Codex CLI orchestrator. The workspace `feature`, `fix`, and `review` skills can select it from a normal-language request; a user can still request it explicitly. Reading `AGENTS.md` alone does not execute the graph. Ordinary small tasks follow [the shared workflow](workflow.md) without starting extra sessions. When an interactive Codex sandbox blocks a nested CLI process, request permission for the runner command; do not bypass the sandbox or claim that the graph ran.
