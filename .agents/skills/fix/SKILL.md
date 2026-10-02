---
name: fix
description: Diagnose and repair an observed software bug in a local project, with focused verification. Use when the user asks to fix a symptom or failing test; exclude explanation-only questions, new features, and read-only review.
---

# Fix

First read [the shared workflow](../../harness/workflow.md), resolving the link from this `SKILL.md`. If it is unavailable, stop before editing and report the missing harness file. Use the repository named in the request, or the current repository when none is named. Identify the observed failure from the invocation, conversation, attached log, failing test, or readable issue. If none identifies a bug, ask for a symptom or expected-versus-actual behavior before changing code.

For a substantial fix that benefits from separate planning, execution, and independent review, read [the executable graph guide](../../harness/graph.md) and run `graph_runner.py` with `--mode fix`, the target project, and the user's symptom and expected behavior. The user need not mention graph or this skill. For a small fix, work directly through the shared workflow. If the graph runner is blocked by sandbox permissions, request the required approval through the tool; do not silently substitute a prose workflow. Report the graph's actual status and state path.

For direct work, reproduce the bug with a focused test or other concrete evidence when feasible. Correct the cause without expanding the requested behavior, and add a regression test when it meaningfully guards the failure. Apply the shared workflow's evidence, verification, and handoff gates.
