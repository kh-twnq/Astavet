> Component template ported from the source harness. Use only when the Repository
> Map confirms this component; replace placeholders and verify tooling from code.

# Rules — the template catalog (asset-sync repo)

> Generic placeholder. This file described one specific template catalog; the concrete detail has
> been removed during genericization. Replace the notes below with your own catalog's specifics, or
> delete this file if your workspace has no such component.

## What this kind of component is
A catalog of function/project templates (often multi-language) that is **synced to object storage**
rather than built into a running app. **No application lives here** — it is pure source artifacts.

## Conventions to record
- Directory → template-family mapping and handler language per family.
- **Deployment**: which directory syncs where (e.g. `aws s3 sync --delete <dir>/ <bucket>` on push),
  and the branch → bucket/env mapping. Note that **only the synced directory ships** — files outside
  it never deploy.
- Any **multi-language bridge contract** the handlers must satisfy (entry-point signatures for each
  language); do not change handler signatures without checking that contract in the shared library.
- The minimal diff between two same-family templates (the few points that actually vary).

## Safety notes
- The auto-synced branch ships to its bucket immediately — never merge broken templates there; treat
  the production-syncing branch as protected.
- Any file holding live tokens (e.g. an `.mcp.json`) must stay git-ignored — never commit or echo it.
