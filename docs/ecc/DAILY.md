# Daily work with AI

Describe the result you want. The agent handles code exploration, skill/rule
selection, implementation, verification and a short report.

```text
Understand → Implement → Verify + review
```

## One task, one prompt

```text
Implement [desired behavior].
Keep [API/business constraints] unchanged.
Done means [observable acceptance criteria].
Handle it end to end and report the change, checks and remaining issues.
```

For example:

```text
Fix cancellation so it restores inventory exactly once, including concurrent
requests. Preserve the current API and order transitions. Add a regression
test, implement, run relevant checks and review the diff.
```

You do not need to invoke each skill, run graph commands or operate separate
agents. The agent follows AGENTS.md, selects the relevant guidance and reports
any verification it could not perform.

## Skill use

| Task | Guidance selected as needed |
| --- | --- |
| Routine Java change | `java-coding-standards` |
| Persistence/service work | Add `jpa-patterns` |
| Feature/bug/test behavior | Add `springboot-tdd` |
| Auth, CSRF, authorization | `springboot-security` |
| SQL migration | `database-migrations`, `postgres-patterns` |
| New Spring architecture/caching/async | Add `springboot-patterns` |
| Release or major upgrade | Add `springboot-verification` |
| Harness configuration | `agent-harness-construction` |

Skills are instruction workflows loaded by the agent, not shell commands the
developer must run one by one. Default routing stays compact; conditional
`skillOptions` explain when deeper guidance is needed. Use one source: the pinned
repo skill or its `ecc:` plugin alternative.

If you want to require a specific workflow, say it explicitly:

```text
Use springboot-tdd for this task: show the regression test failing before fixing it.
```

## Three useful request styles

- **Explore:** “Explain this flow and identify the affected files; do not edit yet.”
- **Implement:** “Handle this task end to end, test it and review the diff.”
- **Review:** “Review this change for correctness, security and missing tests.”

Use deeper planning for architecture changes, migrations, concurrency or unclear
requirements. Routine work needs a short plan, one implement/verify loop and a
compact result. One agent is the default; parallel agents are optional for
authorized, independently separable work.

## Checks

The agent runs the appropriate existing command:

```bash
bash scripts/verify.sh backend
bash scripts/verify.sh frontend
bash scripts/verify.sh ecc
```

Use `all` for changes affecting both application modules. PostgreSQL-sensitive
changes need a disposable test database and integration verification. Missing
database, coverage or security scan evidence must be reported rather than
silently treated as passed.
