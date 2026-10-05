> Component template ported from the source harness. Use only when the Repository
> Map confirms this component; replace placeholders and verify tooling from code.

# Rules — the web frontend (user-facing app)

> Generic placeholder. This file described one specific web frontend; the concrete detail has been
> removed during genericization. Replace the notes below with your own frontend's specifics, or
> delete this file if your workspace has no such component.

## What this kind of component is
The user-facing web application (dashboard, editors, admin, chat). Typically a React/TypeScript SPA
(UmiJS/Next/CRA-style), a component library (e.g. Ant Design), Jest + Testing Library, ESLint +
Prettier + Husky. It calls one or more backend services over REST, and often WebSocket/STOMP and
SSE for live/streamed data.

## Conventions to record per frontend
- Folder structure (`pages/`, `services/` HTTP clients, feature folders, `components/`, `config/`).
- Env-injected globals (backend base URLs, feature flags) and where they're defined.
- Build/dev/test commands and the package manager to use (`yarn` vs `npm` — pick one and stick to it,
  since `postinstall`/Husky wiring can break otherwise).
- Deploy target.

## Coding style (frontend — project-wide policy)
- **No comments/docstrings** beyond required licenses and functional directives;
  see [comment policy](../../rules/java-comment-rules.md).
- **No new frontend unit tests unless explicitly requested.** Retain the team
  exception in [testing.md](../../rules/testing.md) for UI implementation,
  solution-planning estimates and completion-audit scoring. Run existing suites
  and relevant type/lint/build checks. Python server-extension work and dedicated
  Playwright/Galata UI-suite expectations remain outside that exception.

## Pitfalls (generic)
- Use the repo's chosen package manager consistently.
- No generated API client — when a backend DTO or route changes, update the HTTP client by hand.
- Don't assume a browser/e2e suite exists unless the repo actually has one.
- Never suppress `onChange` on a controlled input to guard IME — it blocks composition-based (e.g.
  Vietnamese) typing. Fix char-duplication via a stable input DOM, not `onChange` gating.
