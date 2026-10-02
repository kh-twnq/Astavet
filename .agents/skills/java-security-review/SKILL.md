---
name: java-security-review
description: "Review security-sensitive Java behavior and configuration, including trust boundaries, authorization, parsing, and sensitive data. Use for a focused security assessment, not every change that happens to use Spring Security."
---

# java-security-review

## Invocation prerequisites

Read applicable repository instructions (including nested instructions for affected files) and inspect existing changes before proceeding. Identify the actual target JDK, framework, build and test versions from targeted build files, wrappers, toolchains and existing reports. Distinguish declared, resolved and observed versions; mark unknowns and verify only what the task needs. Use matching primary documentation when behavior is uncertain. Do not assume Spring, JPA, Maven, Gradle or a database is present. Preserve unrelated changes; use no subagents unless explicitly requested. Do not install tools or upgrade dependencies automatically.

Resolve the actual path of this `SKILL.md`, following any discovery symlink. This collection stores skills at `<collection>/skills/<skill-name>/SKILL.md`; ascend from the skill directory through `skills` to obtain the collection root. Read [the shared rules README](../../rules/README.md) at `<collection>/rules/README.md` for authority/routing, then only the rule files selected below and affected source, callers, configuration or tests. Resolve relative references against this canonical skill file, not the discovery path or process working directory. References are not automatically loaded. If the collection is unavailable, report the missing path and proceed with verified project guidance while marking that limitation.

## Required inputs and targeted discovery

Requested endpoints/components, trust and identity model, security policy and review scope; discover missing policy and label assumptions.

Read `java-security.md`; add Spring, persistence or core groups only for concrete sensitive paths, and `java-testing.md` before checks.

## Workflow and verification

1. Trace reachable untrusted inputs to sensitive sinks and effective authentication/authorization enforcement, including object ownership.
2. Inspect actual framework versions/defaults/overrides, credential delivery, parsers, queries, file/network access and logging with targeted searches.
3. Separate confirmed vulnerabilities from hardening recommendations and unknown reachability. Never expose secret values in evidence.
4. Verify with existing safe local checks for allowed/denied cases; do not weaken CSRF, TLS or authorization to pass a check or probe live systems beyond scope.

## Output and completion

Report findings by default; fixes require requested scope. Give redacted file/line evidence, reachable scenario, impact, confidence, recommendation and check limitations. Complete when the requested boundaries are assessed; never claim a clean review proves absence of vulnerabilities. Report verified facts, assumptions, recommendations and unexecuted checks separately.
