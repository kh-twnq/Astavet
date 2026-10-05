> Component template ported from the source harness. Use only when the Repository
> Map confirms this component; replace placeholders and verify tooling from code.

# Rules — the shared library (PyPI package)

> Generic placeholder. This file described one specific shared Python library; the concrete detail
> has been removed during genericization. Replace the notes below with your own library's specifics,
> or delete this file if your workspace has no such component.

## What this kind of component is
A shared runtime library published to a package index (e.g. PyPI) and consumed by many downstream
components at runtime. It typically provides cross-cutting building blocks: async task execution,
subprocess/multi-language bridges, factories, job/registry management, standard result/event models,
serialization helpers, logging/config utilities.

## Conventions to record
- Module map (what each package area provides).
- Any **cross-process / cross-language contract** it defines (e.g. a subprocess request/response
  protocol). Do not change such a contract without coordinating every consumer that depends on it.
- Build/test commands (`python -m build`, `python -m pytest tests/`).
- Key third-party dependencies.

## Publish flow & safety
- Know **which branch publishes** to the package index and whether it auto-publishes on push — if so,
  treat that branch as protected even if the host doesn't enforce it, and **bump the version in
  `pyproject.toml`** before merging anything that changes public API.
- Because this is consumed by many downstreams at runtime, a breaking change to a core helper ripples
  everywhere — publish in dependency order and verify consumers.
