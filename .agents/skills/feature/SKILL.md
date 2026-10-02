---
name: feature
description: Implement a requested new software behavior in a local project, including verification. Use for feature requests with a clear outcome; exclude bug diagnosis and read-only code review.
---

# Feature

First read [the shared workflow](../../harness/workflow.md), resolving the link from this `SKILL.md`. If it is unavailable, stop before editing and report the missing harness file. Use the repository named in the request, or the current repository when none is named. If the invocation gives no desired behavior and the conversation or a referenced issue/spec does not supply one, ask for the smallest missing outcome. Do not invent product requirements.

For substantial work that benefits from separate planning, execution, and independent review, read [the executable graph guide](../../harness/graph.md) and run `graph_runner.py` with `--mode feature`, the target project, and the user's request. The user need not mention graph or this skill. For a small change, work directly through the shared workflow. If the graph runner is blocked by sandbox permissions, request the required approval through the tool; do not silently substitute a prose workflow. Report the graph's actual status and state path.

For direct work, implement the smallest coherent change that meets the observable criteria, including affected contracts and meaningful tests. Apply the shared workflow's evidence, verification, and handoff gates.
