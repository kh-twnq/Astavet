# Java rules and skills

Reusable guidance for Java projects, including projects without Spring or a database. Canonical references live at `rules/` and canonical skills at `skills/java-*/`, relative to the collection repository root. These reference files are not automatically loaded by Codex. Read this routing document and only the groups relevant to the task.

## Authority and scope

Each group separates **Language/API requirements**, **Upstream recommendations**, **User-selected conventions**, **Conditional framework rules**, and **Discover in the repository**. Requirements describe applicable contracts; recommendations are contextual; conventions are this user's choices. An empty category explicitly has no additional rule. Project instructions and established conventions take precedence over optional style defaults. They cannot change actual language/API behavior: report such incompatibilities. Do not silently restructure an established architecture to fit a personal preference.

At invocation, read applicable global and repository instructions, inspect the working diff, and establish the target JDK and actual framework/build/test versions. Distinguish declared versions from observed runtime or resolved versions; mark unknowns. Select matching primary documentation when behavior is uncertain. Do not assume Spring, JPA, Maven, Gradle, or a database. Preserve unrelated changes; no subagents unless explicitly requested. Never install tools or upgrade dependencies merely to apply these references.

## Select references

| File | Read for |
| --- | --- |
| [java-core.md](java-core.md) | Contracts, data types, exceptions, resources |
| [java-style.md](java-style.md) | Naming, formatter, imports, Javadoc |
| [java-architecture.md](java-architecture.md) | Package/module boundaries, layered preference |
| [spring-components-and-api.md](spring-components-and-api.md) | Spring DI, lifecycle, MVC HTTP boundaries |
| [persistence-and-transactions.md](persistence-and-transactions.md) | Storage, transaction, query and locking behavior |
| [java-security.md](java-security.md) | Trust boundaries, identity, sensitive data |
| [java-testing.md](java-testing.md) | Test selection, isolation and result reporting |
| [java-build-and-dependencies.md](java-build-and-dependencies.md) | Build commands, compatibility, dependencies |
| [concurrency-and-performance.md](concurrency-and-performance.md) | Shared state, async lifecycle, measurements |

Exact URLs, consulted section titles, check dates, version scopes and retrieval limitations are in [SOURCES.md](SOURCES.md). Versions there record documentation observations, not project requirements. Content is original guidance; examples were consulted for context, with no source code or skill text copied or adapted.

## Skill routing and examples

Use a general review for a requested diff; use a specialized skill for an explicitly focused task. Relevant concerns may be checked inside a general review without automatically launching other workflows.

| Skill | Example invocation |
| --- | --- |
| java-code-review | `Read <collection>/skills/java-code-review/SKILL.md and follow it to: Review the current Java diff for actionable regressions.` |
| java-feature-implementation | `Read <collection>/skills/java-feature-implementation/SKILL.md and follow it to: Implement the requested account lookup in the existing Java module.` |
| java-test-validation | `Read <collection>/skills/java-test-validation/SKILL.md and follow it to: Select and run checks for the current Java change; assess missing coverage.` |
| java-build-diagnostics | `Read <collection>/skills/java-build-diagnostics/SKILL.md and follow it to: Diagnose this Java compilation failure using the provided log.` |
| java-architecture-review | `Read <collection>/skills/java-architecture-review/SKILL.md and follow it to: Review boundaries and dependency direction in the billing module.` |
| java-persistence-review | `Read <collection>/skills/java-persistence-review/SKILL.md and follow it to: Review transaction and query behavior in the save workflow.` |
| java-security-review | `Read <collection>/skills/java-security-review/SKILL.md and follow it to: Review authorization and input handling for this endpoint.` |
| java-concurrency-diagnostics | `Read <collection>/skills/java-concurrency-diagnostics/SKILL.md and follow it to: Investigate why this background job sometimes hangs.` |

## Storage and invocation

This collection is tracked by the AstaVet application repository and contains `rules/` and `skills/`. Manage the Java skills and rules only here. Codex discovers its skills directly from `.agents/skills/`. Apply each skill only within its declared scope.

Each skill resolves its canonical `SKILL.md` path, ascends from `skills/<skill-name>/` to the collection root, and reads `rules/README.md` there. Relative paths do not depend on the workspace name or process working directory. A consuming repository's `AGENTS.md` should direct Java work to this collection's applicable references; the files are not automatically loaded.

In the current consuming workspace, the canonical collection is under `.agents/`. In Codex CLI or the IDE extension, select a Java skill with `/skills` or mention it with `$java-code-review`, for example. If the new skills do not appear, restart Codex. File-path invocation also works: `Read .agents/skills/java-code-review/SKILL.md and use it to review the current Java diff.` In the examples above, replace `<collection>` with the checkout path. Validate frontmatter, relative references and applicable repository instructions; structural checks do not establish runtime behavior.
