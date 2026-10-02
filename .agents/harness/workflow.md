# Incremental work and session handoff

Read the root `AGENTS.md` for requirements that always apply. Use this workflow when a task has multiple dependent steps, may cross sessions, or needs a reviewable restart point. Small tasks can use a short in-conversation plan.

## Start

1. Inspect `git status --short --branch` in the application repository and relevant existing diffs, including `.agents/` changes. Record user changes and avoid taking ownership of them without reason.
2. State the requested outcome and observable completion criteria. Resolve routine choices from current source and project conventions. Use [plan template](templates/plan.md) for a complex task; store a task-specific copy only when a persistent plan will help the next session.
3. Read the smallest relevant guidance and source set. For Java, begin at `../rules/README.md`; for COD validation, use `../skills/astavet-cod-validation/SKILL.md`; for code graph work, use [GitNexus guidance](gitnexus.md).

## Work and verify

1. Complete one bounded step and record decisions that change scope or behavior. Update the plan's progress with observed evidence, not estimated success.
2. Run checks for the affected behavior using [repository guidance](repository.md). Record the exact command, working directory, result and any skipped or blocked check. Keep the PostgreSQL integration database safety gate.
3. Inspect the final diff and, for meaningful code changes, run GitNexus `detect_changes` as directed. Confirm key graph conclusions in current source.
4. Mark work complete only when all required outcomes and applicable checks are satisfied. If work must continue in another session, write a [handoff](templates/handoff.md) with the next action and evidence. Do not infer user authorization to commit, push, deploy or publish from a template.

## Handoff quality

The next reader should be able to tell what changed, what was verified, what is still uncertain and exactly where to restart. Use relative paths to repo artifacts and commit hashes when useful. Never place credentials, customer data or full sensitive logs in a handoff. Remove stale task handoffs when their value has ended; keep durable decisions in the relevant project documentation.

Design basis: [OpenAI's repository map and execution plans](https://openai.com/index/harness-engineering/), [Anthropic's incremental work and handoff artifacts](https://www.anthropic.com/engineering/effective-harnesses-for-long-running-agents), and [Anthropic's outcome-oriented eval guidance](https://www.anthropic.com/engineering/demystifying-evals-for-ai-agents). These sources inform this project workflow; their example agent architectures are not AstaVet requirements.
