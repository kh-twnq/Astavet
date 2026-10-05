---
name: security-review
description: "Audit changed input, authentication, authorization, secrets, dependencies and web output for security defects."
---

# Security Audit Skill

Read [the Codex adaptation contract](../../../harness/references/codex-adaptation.md) before using this workflow.

Security checklist for Java applications based on OWASP Top 10 and secure coding practices. Load
only the reference section relevant to what the diff actually touches — don't read all three for a
one-file logging change.

> **Deep security review:** follow [model routing](../../../harness/docs/rules/model-routing.md). GitNexus taint/PDG results require a built layer and are leads only; absence of findings never replaces the checklist.

## When to Use
- Security code review
- Before production releases
- User asks about "security", "vulnerability", "OWASP"
- Reviewing authentication/authorization code
- Checking for injection vulnerabilities

## Step 0 — deterministic secret scan (always first)

Before any LLM review, run the bundled script against each touched repo:

```sh
python3 harness/scripts/harness.py --repo <repo-dir> scan
```

It scans changed working files, staged content and untracked files for high-confidence credential
shapes (AWS/GitLab/GitHub/Anthropic/Stripe/Slack tokens, private keys, JWTs) and `.env` files.
For deep review, combine security with other deep lenses in one native `deep-reviewer` when available and permitted; otherwise run the complete checklist inline.
Nonzero exit = BLOCKER (never commit); inspect reported paths/categories without printing credential values. The script is the floor, not the
ceiling — a clean scan does not skip the checklist below (it can't see logic flaws, weak crypto,
or secrets echoed to logs).

---

## OWASP Top 10 Quick Reference

| # | Risk | Java Mitigation |
|---|------|-----------------|
| A01 | Broken Access Control | Role-based checks, deny by default |
| A02 | Cryptographic Failures | Use strong algorithms, no hardcoded secrets |
| A03 | Injection | Parameterized queries, input validation |
| A04 | Insecure Design | Threat modeling, secure defaults |
| A05 | Security Misconfiguration | Disable debug, secure headers |
| A06 | Vulnerable Components | Dependency scanning, updates |
| A07 | Authentication Failures | Strong passwords, MFA, session management |
| A08 | Data Integrity Failures | Verify signatures, secure deserialization |
| A09 | Logging Failures | Log security events, no sensitive data |
| A10 | SSRF | Validate URLs, allowlist domains |

## Routing — which reference applies

| If the diff touches… | Read |
|-----------------------|------|
| Request/DTO validation, JPQL/native/JDBC queries, deserialization of untrusted data | [references/input-validation-and-injection.md](references/input-validation-and-injection.md) — Input Validation, SQL Injection Prevention, Secure Deserialization |
| Rendered/returned output, forms, response headers | [references/web-output-protections.md](references/web-output-protections.md) — XSS Prevention, CSRF Protection, Security Headers |
| Login/session/authorization code, secrets/config, logging statements, dependency versions | [references/auth-secrets-and-deps.md](references/auth-secrets-and-deps.md) — Authentication & Authorization, Secrets Management, Logging Security Events, Dependency Security |

---

## Security Checklist

### Code Review

- [ ] Input validated with allowlist patterns
- [ ] SQL queries use parameters (no concatenation)
- [ ] Output encoded for context (HTML, JS, URL)
- [ ] Authorization checked at service layer
- [ ] No hardcoded secrets
- [ ] Passwords hashed with BCrypt/Argon2
- [ ] Sensitive data not logged
- [ ] CSRF protection enabled (for browser apps)

### Configuration

- [ ] HTTPS enforced
- [ ] Security headers configured
- [ ] Debug/dev features disabled in production
- [ ] Default credentials changed
- [ ] Error messages don't leak internal details

### Dependencies

- [ ] No known vulnerabilities (OWASP check)
- [ ] Dependencies up to date
- [ ] Unnecessary dependencies removed

---

## Related Skills

- `code-review` - General code review
- `spring-stack-patterns` - Secure logging (logging section)
