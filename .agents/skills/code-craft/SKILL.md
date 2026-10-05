---
name: code-craft
description: "Write or refactor object-oriented code using clean code, SOLID and justified design patterns."
---

# Code Craft

Read [the Codex adaptation contract](../../../harness/references/codex-adaptation.md) before using this workflow.

The single reference for **how to write good Java by hand** — clean code, SOLID, and design
patterns are one capability (design-time craft), not three skills. Load only the section relevant
to the change; don't apply all three to a one-line edit.

## Routing — which section applies
| If the change involves… | Read |
|-------------------------|------|
| Any new/edited class, method, or name; smells, duplication, long methods | [clean-code.md](clean-code.md) — DRY/KISS/YAGNI, naming, function & class design, refactoring |
| Responsibilities, dependency direction, abstraction boundaries, extensibility | [solid.md](solid.md) — Single-Responsibility, Open/Closed, Liskov, Interface-Segregation, Dependency-Inversion |
| An extensible / pluggable component where a known structure fits | [design-patterns.md](design-patterns.md) — Factory, Builder, Strategy, Observer, Decorator, etc. |

## Core stance
- Match the naming, structure, and idioms of the surrounding code **first**.
- Reach for a pattern only when it earns its place — over-engineering is a YAGNI violation.
- Prefer intention-revealing names and small methods over comments (see the project's `harness/rules/`
  no-comment policy for Java).

## References
- Detailed checklists and Java examples live in the three section files above.
- Enforced at review time by [code-review](../code-review/SKILL.md) (use its architecture lens for
  package/module-level concerns).
