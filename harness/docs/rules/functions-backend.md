> Component template ported from the source harness. Use only when the Repository
> Map confirms this component; replace placeholders and verify tooling from code.

# Rules — the backend service (core platform / BFF)

> Generic placeholder. This file described one specific product backend; the concrete detail has
> been removed during genericization. Replace the notes below with your own backend's specifics, or
> delete this file if your workspace has no such component.

Shared Java conventions (strict layering, naming, Gradle wrapper, `controller/v1/` versioning) live in
`workspace.md` and apply to any Spring Boot backend in the workspace.

## What this kind of component is
The central product backend / Backend-For-Frontend: owns the core domain (CRUD + business flows),
users & auth/SSO, billing/quotas, scheduling, notifications. Typically Spring Boot + Java + Gradle,
Lombok + MapStruct, PostgreSQL + Spring Data JPA, OAuth2 JWT resource server.

## Conventions to record per backend
- Entry point class + JVM defaults (e.g. default timezone).
- Folder structure under the base package (`controller/`, `service/`, `repository/`, `entity/`,
  `dto/`, `mapper/`, `configuration/`, …).
- Build/run commands via the Gradle wrapper (`./gradlew build|bootRun|test`).
- Config/profile layout (`application.yml` + `application-<concern>.yml`), and where secrets come from.
- Deploy target (k8s manifests, Docker image), and which migration repo runs before it deploys.

## Pitfalls (generic)
- Match the repo's configured JDK when building from a terminal.
- Never return JPA entities from services/controllers — go through mappers.
- Schema changes belong in the paired migration repo, not here (don't rely on `ddl-auto`).
