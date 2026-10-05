> Component template ported from the source harness. Use only when the Repository
> Map confirms this component; replace placeholders and verify tooling from code.

# Rules — the admin/IMS frontend (a second backend consumer)

> Generic placeholder. This file described one specific admin frontend; the concrete detail has been
> removed during genericization. Replace the notes below with your own second frontend's specifics,
> or delete this file if your workspace has only one frontend.

## What this kind of component is
A **second** web frontend that also consumes the backend — typically an admin / internal-management UI
(usage & cost analysis, billing accounts, resource limits, system administration). Often the same
stack as the main user frontend (React/TypeScript + a component library), by convention.

## Why it matters for cross-repo work
A second frontend consumer is easy to forget. If it is **not** a member of your GitNexus contract
group, a clean `route_map` / `api_impact` / contract-registry result **does not prove** a backend
route or DTO change is safe — **grep this repo by hand as well**. This is the single most common way an
admin-UI regression slips through.

## Conventions to record
- Which admin/usage screens it owns that the main frontend does not.
- Its env globals (backend base URL, AI/codegen base URL, CMS URL) — note that constant names may
  differ from the main frontend, so don't copy env wiring between the two blindly.
- Any **extra deploy targets** beyond the usual env branches (e.g. a separate tenant line) — confirm
  the branch base per fix, and whether the fix must land on the extra line too.
- Package manager, build/dev/test commands.

**Frontend test exception applies** (see [testing.md](../../rules/testing.md)): don't author new unit
tests for changes here unless explicitly asked — but still run the existing suite and type-check.
