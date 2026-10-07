# Verification — separate frontend, 2026-10-06

Backend: `astavet`, branch `feature/astavet-mvp`, user-created base commit `1583d3a`. Frontend: sibling `astavet-frontend`, branch `feature/astavet-frontend`, initialized from unborn `main`. All changes made during this frontend separation remain uncommitted. No publication or remote CI execution occurred.

## Executed checks

| Check | Actual result |
| --- | --- |
| `./gradlew clean test postgresTest bootJar` | Passed, executable Spring Boot jar produced |
| H2 domain/integration suite | 20 tests, zero failures/errors/skips |
| PostgreSQL integration suite | 20 tests, zero failures/errors/skips; both Flyway migrations applied and schema validated |
| Frontend `npm ci`, `npm test`, `npm run build` | Passed; two critical client regressions, production bundle produced |
| `npm audit --audit-level=high` | Zero vulnerabilities reported |
| Playwright with Chromium/Brave | Two tests passed against real backend/PostgreSQL: customer COD/recovery/admin processing/cancellation and mobile layout |
| Harness doctor, Python3.11 | Passed for both repositories, including TOML validation |
| Installed parity | Passed for both repositories:116 artifacts,23 skills,5 agents,3 workflows |
| Secret scan | No blockers; warnings refer only to instructional harness examples and disposable backend CI database credentials |
| Independent deep code/security review | Passed after checkout recovery corrections |

The customer browser test deliberately lets the backend commit an order, then drops its response. It verifies the exact request and idempotency key are reused, inventory decrements once, and confirmation survives a subsequent catalogue refresh failure. A different browser session signs in as admin, confirms and cancels the order; stock returns to its initial value. Mobile testing verifies a390px viewport has no horizontal overflow and navigation opens. Runtime screenshots and test artifacts remain ignored.

The backend suites cover server totals, quote changes/tampering, request validation, CSRF/authentication/ownership, exact replay, concurrent cart creation, same-key retries, last-unit competition, rollback, expected-state transitions and concurrent cancellation. Authentication now returns204 success/401 failure to the independent JavaScript client, with CSRF and session fixation protection retained.

## Contract and impact checks

GitNexus is unavailable: no index or configured connector exists. Manual reads/searches traced every retained route and DTO to consumers in `astavet-frontend/src/services/api.js`, `pages/Checkout.jsx`, `pages/Orders.jsx` and `main.jsx`. Old embedded HTML/JS and their backend build task were removed. Both profiles register the backend/frontend workspace and `shop-api-v1` contract. Vite and production Nginx proxy `/api`, `/login`, `/logout` under one browser origin; backend CORS was not broadened.

Backend task `ASTAVET-FRONTEND` has intake/analyze/plan/implement evidence in the existing graph. Its verifier checks the staged backend snapshot. Frontend ledger initialization fails because `main` has no committed base. Full workspace verification therefore cannot pass; cross-repository graph readiness is explicitly unavailable. No ledger state was fabricated or manually edited, and no commit was created to bypass the user's restriction.

## Unverified or provisional

Docker/Nginx containers, TLS ingress and remote CI have not been executed here. Prices, initial stock and shipping remain provisional demo values. Browser tests use isolated local preview data and leave cancelled test-order audit records. Production needs configured database/admin secrets and its deployment services. The frontend is a separate build/project while browser API requests intentionally share its reverse-proxy origin for session/CSRF correctness.

## Feature extension — 2026-10-07

Task `ASTAVET-EXTEND` adds catalogue/stock administration, registered accounts and order history, persisted wishlist, verified-delivery moderated reviews and fixed-AUD coupons. Both repository bases now exist as user-created commits; changes in this task remain uncommitted.

- `./gradlew test postgresTest bootJar`: passed; 34 tests on H2 and the same 34 on isolated PostgreSQL, zero failures/errors. V3 applies and schema validates. Legacy preview order totals before/after V3 were compared byte-for-byte and preserved.
- Frontend `npm test`: two client tests passed; `npm run build`: passed. Three Playwright tests pass against real PostgreSQL/backend, including product creation/hiding, exact stock retry after dropped response, registration/login with delayed refresh, wishlist persistence, discounted COD purchase, delivery, review moderation and order recovery in a new browser.
- Critical backend regressions cover account ownership across same-session account switches, stale product/review edits, stock retry/concurrency, coupon last-use competition and rollback, minimum/expiry/discount limits, verified purchase checks, CSRF/roles and consistent account/product locking across wishlist and checkout.
- Independent review findings were fixed: account-owned confirmations/replays require account ID even when cart matches; moderation includes review version; login and wishlist responses respect auth changes; explicit Java imports and stock response DTOs preserve repository boundaries.
- GitNexus remains unavailable. Manual impact traced migration/entity/repository/domain/service/controller DTO changes through React API consumers. Both repositories register the same API contract and task in the installed harness. Snapshot verification, workspace integration, graph review/security and secret scans are recorded locally after staging. No ship/release is attempted.

This section supersedes the earlier unborn-frontend graph limitation. Remote CI, deployment/TLS, email/SMS, carrier APIs, password recovery and image uploads remain unverified or unimplemented. Test services are shut down after verification.
