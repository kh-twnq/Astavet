> Component template ported from the source harness. Use only when the Repository
> Map confirms this component; replace placeholders and verify tooling from code.

# Rules — an external code-validation service

> Generic placeholder. This file described one specific validation service; the concrete detail has
> been removed during genericization. Replace the notes below with your own service's specifics, or
> delete this file if your workspace has no such component.

## What this kind of component is
A standalone service (e.g. FastAPI) that validates user-submitted code before the platform accepts it —
syntax parsing plus a **handler/entry-point contract** check. Often a flat, single-module service
(`main.py` + a message catalog), deployed like the other services.

> **Validation lives HERE, not in the backend.** A syntax or contract-validation bug is fixed in this
> service even though the symptom surfaces in the backend.

## Conventions to record
- API endpoints (a validate endpoint + a health probe) and any request-size cap.
- The **supported-languages gate** and which parser handles each language (stdlib parser for the
  service's own language, tree-sitter grammars for the rest).
- The handler-contract check (which entry points must exist, and the accepted naming variants).

## ⚠️ Language support must land here too
Adding a new language to the platform is **not** done when the backend and templates support it — a
language this service doesn't know is not meaningfully validated, and the gap is easy to miss because
it does not surface as an obvious failure downstream (the backend may swallow a 400 → **silent pass**).
Always add the grammar + supported-languages entry **in the same release** as platform-side support,
and verify end-to-end.

## Tests
`python -m pytest test_main.py`. Every validation-rule change needs a case here — this is a backend
Python service, and use the verification rules in [testing.md](../../rules/testing.md).
