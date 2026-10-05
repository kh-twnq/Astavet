> Component template ported from the source harness. Use only when the Repository
> Map confirms this component; replace placeholders and verify tooling from code.

# Rules — a provider library (PyPI package)

> Generic placeholder. This file described one specific provider library; the concrete detail has
> been removed during genericization. Replace the notes below with your own library's specifics, or
> delete this file if your workspace has no such component.

## What this kind of component is
A provider-specific library layered on top of a generic shared runtime (see
[qapp-common.md](qapp-common.md)) — it implements the concrete provider/device/handler classes for one
backend family and pins the shared library as a dependency.

## Conventions to record
- Module map (factories, provider/device pairs, handlers, backend components). Provider and device
  classes usually form **parallel hierarchies** — adding a backend means adding both plus the factory
  wiring.
- Test command; note if the test directory has **no committed test modules** (a green run then proves
  nothing) — add real cases alongside behavior changes, since this is a published library and a
  regression ships straight to every downstream that installs it.

## Publish flow & pitfalls
- **Confirm which branch publishes** — it may **not** be `develop`. A repo can break the workspace's
  usual pattern (e.g. publish only from a dedicated release branch), and a branch with no CI rule at
  all ships nothing, so work parked there looks merged but produces no artifact.
- Multiple release lineages can coexist — check which lineage a published version came from before
  trusting its metadata (e.g. which shared-library version it pins).
- The pinned dependency on the shared library **does not update until the pin is bumped**. Publish in
  dependency order: shared library → bump the pin here → this library.
