> Component template ported from the source harness. Use only when the Repository
> Map confirms this component; replace placeholders and verify tooling from code.

# Rules — an IDE extension (storage bridge)

> Generic placeholder. This file described one specific IDE (JupyterLab) storage-bridge extension;
> the concrete detail has been removed during genericization. Replace the notes below with your own
> extension's specifics, or delete this file if your workspace has no such component.

## What this kind of component is
An IDE extension that bridges the IDE's workspace files to the backend and object storage — a server
extension (e.g. Python) that overrides the IDE's file/contents manager and exposes REST handlers, plus
a frontend plugin (TS/React) for a file browser with versioning and save-to-storage.

## Conventions to record
- The two halves and their roles; the central contents-manager class and its cache (file hashes,
  decoded token, active token, version id) — clear caches on auth errors / token change to prevent
  session bleed.
- The REST handler routes it registers.
- **Auth**: how it reads the session token (e.g. from a cookie), mirrors it to an env var, and revokes
  on session close (`sendBeacon` on page hide).
- Build/dev/test commands (IDE toolchain + `pytest` for the server half).
- Release model — many extensions **release by version tag**, not by merging an env branch; long-lived
  env branches may be stale, so base off the active default branch.

## Safety notes
- Language/extension enforcement lives in the contents-manager `save()` — do not bypass it.
- If directory creation is intentionally forbidden, keep it forbidden.
- Don't reference aspirational README sections whose module isn't actually present.
