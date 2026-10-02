# Java security

## Language/API requirements

Access modifiers are not authentication or authorization. Serialization, parsing and file/network APIs must be evaluated using their concrete contracts; Java does not make untrusted object graphs safe by default.

## Upstream recommendations

Oracle's Java SE security guidance covers confidentiality, validation, injection, resource exhaustion, mutability and unsafe deserialization. Spring Security supplies distinct authentication, authorization and exploit-protection mechanisms; select its Servlet or reactive guidance as appropriate.

## User-selected conventions

Trace untrusted input to sensitive operations. Validate structure, bounds and business meaning; use parameterized database queries and context-appropriate encoding rather than ad hoc escaping. Separate identity verification from permission/object-ownership checks. Protect secrets and sensitive data in code, logs, errors and diagnostic artifacts; show redacted evidence. Examine deserialization type exposure, parser limits, file paths, URL fetching, redirects and resource consumption based on real reachability. Restrict resource access at the effective boundary and account for path traversal/SSRF when applicable.

Do not disable CSRF, TLS verification or authorization as routine fixes. A requested exception needs an explicit threat-model rationale, narrow scope and suitable checks. Do not label speculative reachability as a confirmed vulnerability.

## Conditional framework rules

For Spring Security, inspect effective filter chains, matchers, method security, defaults and any overrides. CSRF protection is enabled by default for unsafe HTTP methods in Servlet applications; suitability depends on how credentials are sent. Preserve security when diagnosing failed requests/tests. Use reactive security contracts for WebFlux, and avoid assuming CORS provides authorization. Custom security configuration can alter defaults, so verify observed behavior.

## Discover in the repository

Trust boundaries, identity provider/session/token model, role/object authorization policy, security/version/configuration, parser/client settings, secrets mechanism, log policy and security tests.

Sources: [Security source records](SOURCES.md#java-security).
