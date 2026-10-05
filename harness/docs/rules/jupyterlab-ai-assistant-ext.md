> Component template ported from the source harness. Use only when the Repository
> Map confirms this component; replace placeholders and verify tooling from code.

# Rules — an IDE extension (AI assistant)

> Generic placeholder. This file described one specific IDE (JupyterLab) extension; the concrete
> detail has been removed during genericization. Replace the notes below with your own extension's
> specifics, or delete this file if your workspace has no such component.

## ⚠️ Check the source-of-truth doc first
An extension repo may keep its authoritative coding rules in an `AGENTS.md` (with other agent instruction files
as **symlinks** to it). **Edit the real file, never a symlink**, and read it before changing the
extension — it holds the detailed Do/Don't rules this file only summarizes.

## What this kind of component is
An in-IDE AI assistant: a frontend UI panel (TS/React) plus a server extension (e.g. Python) that
proxies requests to the AI/codegen service. It is the IDE-side client of the same AI service the web
frontend uses.

## Conventions to record
- The two halves (server extension package + frontend npm package) and how they bridge (request API).
- Build/dev commands using the IDE's pinned toolchain (e.g. `jlpm build`/`watch`, `labextension
  develop`, plus `pytest` for the server half). Rebuild the frontend after every change.

## Coding style
- **No comments/docstrings** beyond required licenses and functional directives;
  see [comment policy](../../rules/java-comment-rules.md).
- **No new frontend unit tests unless explicitly requested.** Retain the team
  exception in [testing.md](../../rules/testing.md) for UI implementation,
  solution-planning estimates and completion-audit scoring. Run existing suites
  and relevant type/lint/build checks. Python server-extension work and dedicated
  Playwright/Galata UI-suite expectations remain outside that exception.

## Pitfalls (generic)
- Rebuild after TS changes; re-run `labextension develop` after reinstalling the server package.
- Keep server-side and frontend logic separate — don't duplicate business logic across the two.
- Talks to the AI/codegen service's versioned API — endpoint/DTO changes need matching edits here.
- The IDE's package manager needs a **modern Node** — an old Node fails confusingly.
