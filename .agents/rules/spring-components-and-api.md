# Spring components and HTTP APIs

## Language/API requirements

No Spring rule applies to a project without Spring. Where used, dependency injection, bean scopes and controller annotations follow the installed framework's contracts.

## Upstream recommendations

Spring recommends constructor injection for required dependencies. Singleton scope provides a shared bean per container/definition, not automatic thread safety. Scope, initialization and destruction determine dependency/resource lifetime.

## User-selected conventions

Keep HTTP contracts explicit. Use request/response DTOs when they protect the boundary or clarify contracts; do not require a redundant DTO for every value. Validate untrusted input on the server and enforce business invariants at their appropriate boundary. Keep exception mapping, status codes and response shapes consistent with the existing API, and avoid exposing internal exception details. In adopted layered backends, follow [java-architecture.md](java-architecture.md).

## Conditional framework rules

- For Spring components, use constructors for mandatory collaborators; discover optional injection and configuration conventions. Avoid mutable request-specific fields in shared beans. Check lifecycle callbacks, scope mismatches and cleanup for owned resources.
- For **Spring MVC**, inspect binding, Bean Validation activation and exception handling for the actual version. `@Valid` requests nested validation; it is not itself a not-null constraint. Verify both invalid bodies and applicable method constraints. Use configured `@ExceptionHandler`/advice consistently.
- **WebFlux is a separate reactive stack**. Do not transfer Servlet filters, MVC test slices or blocking controller assumptions to it. Consult reactive documentation matching the installed version when it is present.

## Discover in the repository

Spring presence/version, MVC versus WebFlux, scopes, injection patterns, validation namespace/provider, DTO conventions, HTTP/error contracts, lifecycle configuration and controller tests.

Sources: [Spring component/API source records](SOURCES.md#spring-components-and-api).
