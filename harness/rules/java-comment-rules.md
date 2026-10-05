# Rules — Comment Standards (NO COMMENTS)

Authoritative comment policy for **all** code in the workspace — every language, not only Java.
This rule supersedes the brief Phase 2 note in `java.md` and the previous 15-rule "explain WHY"
policy that lived here.

## Core Directive (retained harness policy)

> **When writing or modifying code, do NOT add comments — in any language, ever.**

Code must be self-explanatory through naming and structure. If a piece of logic feels like it needs
a comment to be understood, that is a signal to **rename, extract, or restructure** it — not to
annotate it. Explanation that genuinely cannot live in the code belongs in the Jira ticket, the MR
description, an ADR, or technical documentation — **never** in a source comment.

This applies to `.java`, `.ts`, `.tsx`, `.js`, `.jsx`, `.py`, `.c`, `.cpp`, `.h`, YAML, and every
other source file in every repo.

## The Rules

### 1. Do NOT write comments
No line comments, no block comments, no trailing comments, no explanatory prose inside code —
regardless of whether the comment would explain WHAT, WHY, a business rule, a workaround, an edge
case, or intent. All of it stays out of the code.

### 2. Do NOT write docstrings / Javadoc for explanation
No Javadoc, JSDoc/TSDoc, Python docstrings, or equivalent added to explain implementation or
document a contract. Express contracts through types, signatures, and names.

### 3. Never leave commented-out code
Delete unused code. Git history is the record of old implementations.

### 4. Never leave TODO / FIXME / XXX / HACK notes
Track outstanding work in the issue tracker (a `PROJ-` ticket), not in the source.

### 5. Self-documenting code is mandatory, not optional
- Meaningful class, method, and variable names.
- Extract complex logic into well-named methods instead of narrating it.
- Small, single-responsibility units whose intent is legible from the code alone.

### 6. Do NOT remove other people's existing comments in bulk
This directive governs **code you write or modify**. Do not add new comments. When editing a region
that already contains someone else's comments, you may drop comments that your change makes wrong or
obsolete, but do not sweep unrelated existing comments out of files as a drive-by change — that is
not the task and inflates the diff.

## The only permitted exceptions

Add these **only** because a tool, license, or the language requires them — never as explanation:

- **Required license / copyright headers** (where the repo already uses them).
- **`@Override`** and other functional annotations (these are annotations, not comments).
- **Machine-read directives that are load-bearing**, e.g. `// eslint-disable-next-line <rule>`,
  `# noqa: <code>`, `# type: ignore`, `@SuppressWarnings(...)`, a shebang line. These change tool
  behavior; they are not prose. Keep them minimal and specific (name the exact rule/code), and only
  when there is no code-level way to avoid the warning.

Anything that is *explanation dressed as a directive* is still a comment — not allowed.

## Application scope
- **All source files, all repos, all languages.** Java, TypeScript/React (frontends + IDE
  extensions), Python (libraries, services, server extensions), C/C++ (native code/templates), config.
- Enforced at write-time by `harness/rules/java.md` Phase 2 and by the workspace guard-hook reminders.

## Relationship to other rules
- **`java.md` Phase 2** — the write-time gate; references this file as the full policy.
- **`code-review` standards lens** — flags any newly added comment/docstring as a finding.
- The former 15-rule "explain WHY / Javadoc for contracts" policy is **retired** by this directive;
  see git history if the old text is needed.
