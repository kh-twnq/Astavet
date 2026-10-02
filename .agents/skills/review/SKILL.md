---
name: review
description: Review a code diff, branch, or PR in a local project for actionable defects and missing verification without editing. Use for review requests; exclude requests to implement or fix code.
---

# Review

First read [the shared workflow](../../harness/workflow.md), resolving the link from this `SKILL.md`. If it is unavailable, stop and report the missing harness file. Use the repository named in the request, or the current repository when none is named. With no change target, inspect staged, unstaged, and untracked changes in that repository. If there is no diff or another clear target in the conversation, ask for a branch, commit range, PR, or files.

For a substantial review, read [the executable graph guide](../../harness/graph.md) and run `graph_runner.py` with `--mode review`, the target project, and the requested change target. The user need not mention graph or this skill. For a small review, work directly through the shared workflow. If the graph runner is blocked by sandbox permissions, request the required approval through the tool; do not silently claim a graph review ran. Report the graph's actual status and state path.

For direct work, trace affected callers and behavior using source, tests, and relevant tools. Run focused read-only checks when useful and safe. Apply the shared workflow's guidance routing, evidence, and handoff gates.

Report actionable findings first, ordered by severity, with file and line, triggering scenario, and evidence. If no findings are supported, say so and describe the remaining verification gap.
