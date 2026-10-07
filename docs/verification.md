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

GitNexus is unavailable: no index or configured connector exists. Manual reads/searches traced every retained route and DTO to frontend consumers. After the authorized structure change, their locations are `astavet-frontend/src/services/api.js`, `pages/checkout/CheckoutPage.jsx`, `pages/order/ConfirmationPage.jsx`, `pages/admin/AdminOrders.jsx` and `app/App.jsx`. Old embedded HTML/JS and their backend build task were removed. Both profiles register the backend/frontend workspace and `shop-api-v1` contract. Vite and production Nginx proxy `/api`, `/login`, `/logout` under one browser origin; backend CORS was not broadened.

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

## Query and catalogue fixes — 2026-10-07

Task `ASTAVET-QUERY-FIX` resolves the three findings from the Java guidance review. Orders page 25 root IDs first, then fetch complete lines without paginating a collection join; results retain created-at/UUID ordering. Wishlist products load in one batch while retaining saved order and missing-product errors. Existing write transactions and inventory lock ordering are unchanged.

Public catalogue responses remain JSON arrays, now bounded to 25 active products with page/name-search parameters and stable name/UUID ordering. Slug lookup keeps deep links reachable beyond page one. A 50-ID lookup supplies active cart image metadata; archived products remain hidden. The React catalogue and header search use server pages, discard obsolete responses and expose retry controls. Successful store refreshes reload product details.

- Backend `./gradlew check bootJar postgresTest`: 37 tests passed on H2 and the same 37 passed on disposable PostgreSQL 15.18; zero failures/errors/skips. Real Flyway migrations and schema validation ran on both engines.
- Query-count regressions: full admin order pages use two prepared statements and zero lazy collection fetches (previously 27 for 25 orders); account order pages use three including account resolution. A 25-item wishlist uses three statements including account resolution and wishlist IDs. Empty pages/lists avoid unnecessary fetches.
- Regressions also verify complete order children, tied sort values, page boundaries, account isolation, wishlist ordering, literal search wildcards, archived visibility and malformed/missing request inputs.
- Frontend production build and both existing Node client tests pass. Four real-browser tests pass on the disposable PostgreSQL backend, including the new 26-product pagination/search/deep-link/reload/cart-image and malformed-client-path regression alongside the existing COD/recovery/admin/account/review/mobile flows.
- Independent deep review passed after correcting malformed client-path handling, missing lookup parameters and stale detail retry behavior. Both harness doctors (Python 3.11), installed parity checks, secret scans and diff whitespace checks pass.

Framework/build/persistence versions are unchanged and no migration is needed. Verification processes and temporary PostgreSQL data are removed afterwards. Remote CI, production deployment/TLS and performance under production data volume remain unverified. Changes remain uncommitted.

## Project structure — 2026-10-07

Task `ASTAVET-STRUCTURE` implements the user's explicit request to reorganize the project. See [the current package map](structure.md) and the [frontend folder map](../../astavet-frontend/README.md).

JPA mappings move from `repository/entity/` to `entity/`; ShopException and API advice move to `exception/`; the guest identity helper moves to `controller/support/`. Java tests move into `domain/`, `service/`, `integration/`. Project instructions now describe the approved paths while retaining entity isolation within persistence callers.

React bootstrap stays in `main.jsx`, and application/session/route orchestration moves to `app/App.jsx`. Account, order and administration screens each get a file under their page group. Shared administrator list loading is in `hooks/useAdminList.js`; paging, stock forms and catalogue search are reusable components. Imports are explicit, with no barrel layers, routing dependencies or new abstractions.

- Compared all 100 Java production/test class bodies with an exact pre-change backup: only package/import/blank-line edits. Application configuration and Flyway migrations are byte-identical. Independent review confirmed extracted React functions retain their bodies, with component-name/export adjustments and an equivalent catalogue screen extraction.
- `./gradlew clean check bootJar` and `./gradlew check bootJar postgresTest` pass: 37 H2 tests and 37 PostgreSQL tests, zero failures/errors/skips. Relocated tests remain discoverable; Spring components, entity scanning, HTTP advice and schema validation work.
- `npm run build`, both existing Node client regressions and all four real-browser tests pass against a disposable PostgreSQL backend. These cover administration, customer accounts, reviews, checkout/recovery, catalogue paging/deep links/cart images and mobile layout.
- Independent architecture/correctness review passes. No stale source package imports, unresolved frontend imports or entity imports in services/controllers/DTOs/domain/config. Python 3.11 doctor and installed parity pass in both repositories. Secret scans have no blockers; advisory matches in moved AccountLogin code are the `fields.password` expression and password autocomplete strings, not credentials.

Pre-change source trees, instruction files and staged patches are saved locally outside both repositories in `java-backend-upstreams/backups/2026-10-07-structure/`. Existing query fixes are retained. Versions, public routes/DTOs, CSS selectors, session/storage keys, transactions and database schema are unchanged. No commit, push or deployment is performed. Temporary verification services/data are removed; remote CI and production deployment remain unverified.
