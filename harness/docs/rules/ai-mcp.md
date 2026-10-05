> Component template ported from the source harness. Use only when the Repository
> Map confirms this component; replace placeholders and verify tooling from code.

# Rules — the AI / codegen service

> Generic placeholder. This file described one specific AI/codegen service; the concrete detail has
> been removed during genericization. Replace the notes below with your own service's specifics, or
> delete this file if your workspace has no such component.

Shared Java conventions (strict layering, naming, Gradle wrapper, `controller/v1/`) live in
`workspace.md` and apply here.

## What this kind of component is
A service that fronts one or more external AI providers (Claude / OpenAI / Azure / local) behind a
stable API — sessions, quotas, analytics, streaming generation. Typically Spring Boot + Java + Gradle,
OAuth2 JWT resource server, PostgreSQL + JPA, Caffeine or Redis cache.

## Conventions to record per service
- Entry point, server port, and the API base clients call (e.g. `/api/v1`).
- Provider abstraction + per-provider implementations.
- 3-layer repository pattern (business `repository/` → `repository/impl/` mapper+JPA delegate →
  `repository/jpa/`); never expose JPA entities outside the repository layer.
- Build/test commands, including any split of integration tests into a separate Gradle task that the
  plain `test` task excludes (run both when touching repositories/controllers).
- Env-var-driven config (DB URL/creds, cache type, JWT issuer/JWK, provider keys), and where secrets
  come from.
- Deploy target and the paired migration repo that runs first.

## Security & pitfalls (generic)
- OAuth2 JWT resource server — keep issuer/JWK consistent with the other services.
- Match the repo's configured JDK when building from a terminal.
- Consumers call the versioned API — endpoint/DTO changes need matching client edits.
