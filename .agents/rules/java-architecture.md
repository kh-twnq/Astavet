# Java architecture

## Language/API requirements

Respect package visibility and module exports/reads when JPMS is used. Ordinary package dependency cycles are not universally illegal Java, but can undermine maintainability.

## Upstream recommendations

Spring Boot does not mandate a particular code layout. It recommends avoiding the default package and locating the main application class in a root package for appropriate scanning. ArchUnit can check adopted dependency, layer and cycle rules; it does not choose an architecture for the project.

## User-selected conventions

Keep clear package/module responsibilities and a deliberate dependency direction. Avoid dependency cycles, unjustified abstractions and speculative reuse. Do not require interfaces for every service, force DDD or hexagonal architecture, or impose arbitrary file/class/method size limits.

For **new layered Java web backends**, use separate `controller/`, `service/`, and `repository/` packages, optionally organized within features. Controllers handle HTTP input/output and delegate to services. Services coordinate use cases and business operations. Repositories handle persistence. Controllers must not access repositories directly; dependency direction is controller → service → repository. Preserve appropriate invariants and behavior in domain objects; services need not contain all domain behavior.

This layered layout is the user's preference, **not a Java or Spring mandate**. For an established architecture, report conflicts with this preference and follow the requested scope and project decisions instead of silently restructuring code. Record existing exceptions as context, not permission to create new violations of adopted rules.

## Conditional framework rules

Apply component/entity scanning considerations only to relevant Spring Boot configurations. Apply JPMS, ArchUnit or other architecture tooling only when present or explicitly requested. Petclinic's layout is an example; it is not the prescribed package structure.

## Discover in the repository

Architectural decisions, package/module graph, source sets, public boundaries, domain responsibilities, existing exceptions, framework scanning, architecture tests and the exact refactor/review scope.

Sources: [Architecture source records](SOURCES.md#java-architecture).
