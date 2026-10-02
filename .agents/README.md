# AI guidance collection

All workspace AI rules and agent skills are maintained in this directory, tracked by the AstaVet application repository.

## Contents

- [harness/workflow.md](harness/workflow.md): incremental task workflow, plan and session handoff templates.
- [harness/repository.md](harness/repository.md): AstaVet layout, commands, conventions and validation details routed from root `AGENTS.md`.
- [harness/gitnexus.md](harness/gitnexus.md): graph workflow, safety rules and local CLI fallback.
- [harness/graph.md](harness/graph.md) and `harness/graph_runner.py`: bounded Codex execution graph for substantial feature, fix, or review work.
- [evals/README.md](evals/README.md): paired trial protocol, seeded task cases, source log, comparison script and pilot results for harness evaluation.
- [rules/README.md](rules/README.md): reusable Java rule routing, authority and skill invocation examples (11 reference files).
- [rules/SOURCES.md](rules/SOURCES.md): source URLs, consulted sections, dates, version scopes and retrieval limitations.
- `skills/java-*/SKILL.md`: eight reusable Java skills referencing the shared rules.
- `skills/{feature,fix,review}/SKILL.md`: workspace task skills that select the shared workflow or execution graph.
- [skills/astavet-cod-validation/SKILL.md](skills/astavet-cod-validation/SKILL.md): project-specific AstaVet COD review and validation; apply only to the target AstaVet application repository.
- `skills/gitnexus-*/SKILL.md`: GitNexus workflows for this workspace; use the GitNexus MCP or project-local runner with the target repository bound explicitly.

## Use in a workspace

Keep a single canonical checkout. The current workspace stores all AI guidance under `.agents/`:

```text
.agents/
├── README.md
├── harness/
│   ├── repository.md
│   ├── gitnexus.md
│   ├── graph.md
│   ├── graph_runner.py
│   ├── workflow.md
│   └── templates/
├── evals/
│   ├── README.md
│   ├── SOURCES.md
│   ├── cases.json
│   ├── compare.py
│   ├── prepare.py
│   ├── run.py
│   └── results/
├── rules/
│   ├── README.md
│   ├── SOURCES.md
│   └── <rule-group>.md
├── tests/
│   ├── test_evals.py
│   └── test_graph_runner.py
└── skills/
    └── <skill-name>/
        └── SKILL.md
```

Each skill uses the standard `SKILL.md` format with `name` and `description` frontmatter. Optional scripts, references, assets and `agents/openai.yaml` are added only when a workflow needs them. The application repository's root `AGENTS.md` directs Java work to `rules/README.md`; shared rule references must be read explicitly.

Codex discovers the skill directories directly under `.agents/skills/`; no workspace symlinks or user-level registrations are needed. `.codex/config.toml` and `.codex/hooks.json` hold the Codex project layer, with hook scripts in this collection. Project hooks require project trust and separate hook trust. See [Codex skill discovery](https://learn.chatgpt.com/docs/build-skills#where-codex-loads-local-skills) and [project hooks](https://learn.chatgpt.com/docs/config-file/config-advanced#hooks).

The application repository's `.gitnexusrc` sets `indexOnly` so future `gitnexus analyze` runs refresh the graph without reinstalling skills under `.claude/skills/` or rewriting `AGENTS.md` and `CLAUDE.md`.

Invoke a skill by file path and ask the agent to follow it. For example: `Read .agents/skills/java-code-review/SKILL.md and use it to review the current Java diff.` For the project-specific skill, identify the target application repository.

In Codex CLI or the IDE extension, run `/skills` and select a skill, or mention it with `$java-code-review` or `$astavet-cod-validation`, for example. If the new skills do not appear, restart Codex. File-path invocation also remains available. Structural checks do not establish runtime invocation. Java skills resolve rule references from their canonical file location when the checkout moves.

## Manage the repository

From the AstaVet root, use `git status` and `git diff` to inspect changes, including `.agents/`. The former nested Git metadata is backed up outside the workspace. Commit or publish only when explicitly authorized.
