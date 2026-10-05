# Executor and advisor architecture

[Model routing](../rules/model-routing.md) is the single source for role routing.
This document preserves the source harness's distinction between executor,
deep reviewer and scarce advisor, using native Codex roles.

## Responsibilities

- **Executor:** the active session and selected model. Reads code, scopes work,
  designs the solution, obtains plan/base approval, implements and verifies.
  It coordinates permitted workers and owns the user-facing recommendation.
- **Deep reviewer:** `.codex/agents/deep-reviewer.toml`, read-only with high
  reasoning effort. Reviews concurrency, architecture, security and broad
  cross-repo changes; provide all applicable deep lenses in one packet.
- **Drafter:** `.codex/agents/drafter.toml`, read-only. Produces bulk summaries,
  changelog drafts and checklist evidence without taking engineering decisions.
- **Engineering advisor:** `.codex/agents/engineering-advisor.toml`, read-only.
  Manually invoked for a focused decision after a deep pass, never a routine
  automatic reviewer. It supplies advice; the executor verifies and decides.

## Advisor eligibility

First perform the deep review. Consider the advisor only when at least one
condition holds: ≥2 viable approaches remain unresolved; two distinct attempts
failed; rollback cost is high; or a final recommendation is needed for high-risk
work. Cross-repo, auth, contract or concurrency scope alone routes to deep review,
not automatically to the advisor. Honor active-client delegation restrictions.

## Evidence packet

Give the advisor the requested outcome/acceptance criteria, relevant repos and
`path:line` symbols, current/base refs, observed versus expected behavior,
investigation and attempts already performed, test/graph evidence, the current
hypothesis, unresolved alternatives, relevant rules and the focused question.
Do not forward a raw task without prior investigation. Confirm structural
claims by code reads or rg/graph evidence before acting on recommendations.

## Verification and limits

Inspect `.codex/agents/*.toml` and actual client task metadata to establish the
resolved role, reasoning setting and model. Omitted model means inheritance;
never infer a Claude tier or ask a model to certify its own runtime identity.
The executor runs validation after implementing advice. Read-only reviewers
must not post comments, change tickets or trigger external actions.

## Maintenance

Keep routing in the linked table, role instructions in native TOML and the
workflow in skills. If the advisor is disabled or unavailable, retain the deep
pass and report unresolved decisions. Removing a role requires updating its
capability inventory, references and relevant checks; changing repo config
must preserve the user's selected model and unrelated settings.
