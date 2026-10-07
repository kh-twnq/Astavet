# AstaVet COD shop

Java 21 / Spring Boot 4.1.1 commerce MVP, with PostgreSQL, JPA, Flyway and a separately built React JavaScript frontend in `../astavet-frontend`. No online-payment integration is needed.

Customer flow: product catalogue/detail → server-backed bag → Australian delivery checkout → order confirmation. The initial catalogue has one product; products, cart lines and order lines support multiple products without a frontend or backend rewrite. The frontend routes `/admin/login` and `/admin/orders` provide administration. Admins can view customer details and line items, paginate orders, confirm, ship, mark delivered and cancel before shipment.

The design and initial product are based on [the supplied AstaVet reference](https://www.astavet.com/products/astaxanthin-200g). Its title is **AstaVet 130g**, although its URL says 200g. The seeded **AUD 49.00 price and 100 units are provisional demo data**, since the reference is sold out and does not expose a usable price. Shipping is provisionally AUD 7.95, free from AUD 100.00. The frontend uses reference packaging photography. Confirm catalogue, fulfilment terms, taxes, branding rights and imagery before opening the shop to customers. No email notifications are sent.

## Project structure

Use conventional layer packages: `controller/`, `service/`, `repository/`, `dto/`, `domain/`, `entity/`, `config/`, `exception/`. Service/repository interfaces retain their `impl/` implementations; HTTP endpoints remain in `controller/v1/`, and Spring Data repositories in `repository/jpa/`. Entities are persistence-only types even though they now have a top-level package.

See [the package map and dependency boundaries](docs/structure.md). Tests are grouped into `domain/`, `service/`, `integration/`. Frontend screens live in the separate [React repository](../astavet-frontend/README.md).

## Run

Requirements: Java 21 and PostgreSQL. The independent frontend requires Node 22.12+. The Gradle wrapper downloads Gradle/dependencies on its first run.

```sh
./gradlew check bootJar
```

Set the following variables in your environment; no production credentials are supplied:

| Variable | Purpose |
| --- | --- |
| `DATABASE_URL` | JDBC URL; defaults to `jdbc:postgresql://localhost:5432/astavet` |
| `DATABASE_USERNAME` | Database role; defaults to `astavet` |
| `DATABASE_PASSWORD` | Required database password |
| `ADMIN_USERNAME` | Administrator login; defaults to `admin`, maximum 100 characters |
| `ADMIN_PASSWORD_HASH` | Required Spring encoded bcrypt hash, beginning with `{bcrypt}` |
| `SESSION_COOKIE_SECURE` | Defaults to `true`; set `false` only for local HTTP |

Generate an administrator bcrypt hash using an approved password manager/tool or `htpasswd -nBC 12 admin`, which prompts for a password. Remove the `admin:` prefix and prepend `{bcrypt}` to its `$2y$…` hash. Pass the resulting value literally through an environment variable or secret manager, preserving dollar signs.

With a database configured:

```sh
./gradlew bootRun
```

For an isolated local Docker stack, export `DATABASE_PASSWORD` and `ADMIN_PASSWORD_HASH`, build the jar, then run:

```sh
docker compose up --build
```

The backend serves APIs at `http://localhost:8080`. In `../astavet-frontend`, run `API_PROXY_TARGET=http://127.0.0.1:8080 npm run dev`, then open `http://127.0.0.1:5173`; administration is at `/admin/login`. Compose binds the app to localhost and defaults secure cookies to false for local HTTP. The database volume preserves data across restarts. Hosted operation requires HTTPS, secure cookies, database backups and ingress request/body limits plus rate limiting on `/login` and `/api/v1/orders`. These deployment services are outside this repository; the shop has no in-process rate limiter.

## Server invariants

- Controllers accept validated request DTOs and return explicit response DTOs. Services use immutable domain records; JPA entities stay inside `repository/`. Service methods own transaction boundaries. No raw JDBC is used.
- Prices, currency, shipping and totals come from the database/server. JSON fields such as `price`, `total`, `paymentMethod` or arbitrary order states are rejected. Quotes fingerprint line identifiers, quantities, product names, unit prices and shipping. Checkout must match a fresh locked quote, so changed prices/content require customer review.
- The cart row is locked before checkout; products are locked in UUID order. Inventory reservation, immutable order snapshots, audit events and cart clearing commit together. No stock is reserved by adding to a bag. Database constraints prohibit negative stock and duplicate order keys.
- Checkout keys are scoped to the guest cart and persisted with a request hash. Simultaneous retries return one order and reserve once. Changed data with a used key gets HTTP 409. The browser saves the exact pending submission before sending it and retries it after an uncertain response, even if the cart was already cleared. A successful retry does not clear items added after the original checkout.
- Order transitions: `PLACED → CONFIRMED → SHIPPED → DELIVERED`. Cancellation is allowed from `PLACED` or `CONFIRMED`. The admin supplies its expected state; stale requests get HTTP 409. Repeating an already applied state change is a no-op. Order locking makes cancellation restore inventory and record its audit event exactly once.
- Guest order reads require the session's cart ownership; other sessions receive 404. Admin APIs and services require `ADMIN`. Login/logout and all writes keep Spring Security CSRF protection. The frontend escapes browser output and its production reverse proxy sets a restrictive CSP.
- Guest ownership lives in the HTTP session. This MVP runs as one instance, or requires sticky sessions. A session expiry/restart removes guest access to old confirmations. Registered customers recover account-owned orders after signing in; ownership follows account ID, never delivery email or a shared cart.
- Transaction failures roll back; transient database failures return a sanitized 503. Retry checkout with the saved key and request. Logs contain order IDs/state, not customer details or checkout payloads.

## API

| Method | Route | Contract |
| --- | --- | --- |
| POST | `/login` | Form username/password and CSRF; 204 success, 401 invalid credentials |
| POST | `/logout` | CSRF required; 204 success |
| GET | `/api/v1/products?page=0&q=` | Active catalogue, 25 per page ordered by name/UUID; literal case-insensitive name search (100 characters maximum) |
| GET | `/api/v1/products/by-slug/{slug}` | Active product detail, independent of catalogue page |
| GET | `/api/v1/products/lookup?ids={comma-separated-UUIDs}` | Active product metadata for 1–50 IDs (cart images); archived products omitted |
| GET | `/api/v1/csrf` | Masked session token and header name |
| GET | `/api/v1/cart` | Session bag, current totals and fingerprint |
| PUT | `/api/v1/cart/lines` | `{productId, quantity}`; quantity 0 removes a line |
| POST | `/api/v1/orders` | `{idempotencyKey, quoteFingerprint, name, email, phone, address, city, postcode, state}`; COD/Australia fixed by server |
| GET | `/api/v1/orders/{id}` | Session-owned confirmation |
| GET | `/api/v1/admin/orders?page=0` | 25 orders per page, newest first |
| GET | `/api/v1/admin/orders/{id}` | Admin order detail |
| PUT | `/api/v1/admin/orders/{id}/status` | `{expectedStatus, status}` |

A successful order submission or replay returns HTTP 200 with the same order ID. Validation failures return 400, conflicts 409, missing/inaccessible resources 404, and transient database failures 503.

Admins create/edit/hide products through `/admin/products`; stock starts at zero and changes through audited adjustments with an operation UUID and expected product version. Image paths select existing frontend assets; uploads are not implemented. Historical order names/prices remain snapshots. Do not edit an applied migration.

## Verification

```sh
./gradlew check bootJar
python3 harness/scripts/harness.py doctor
python3 harness/scripts/check_parity.py --installed
python3 harness/scripts/harness.py scan
```

`test` runs domain and full Spring/JPA/MockMvc integration tests against H2 in PostgreSQL mode, applying all real migrations and validating the schema. Critical regressions cover pricing, authentication, CSRF, ownership, idempotency, concurrent first-cart creation, last-unit competition, rollback, state transitions and concurrent cancellation. Frontend client and browser regressions run independently in `../astavet-frontend`.

For the same integration suite on PostgreSQL, set `TEST_DATABASE_URL`, `TEST_DATABASE_USERNAME` and `TEST_DATABASE_PASSWORD` to a **dedicated disposable test database**, then run `./gradlew postgresTest`. Tests delete application rows between cases. The CI workflow runs H2 and PostgreSQL checks; it has not been executed remotely during implementation.

See [verification evidence](docs/verification.md) for the actual checks and environment limitations from this implementation. The installed harness remains authoritative. Do not claim ledger/graph readiness without a committed base and snapshot-bound verification.

## Accounts, catalogue and community

- `POST /api/v1/accounts` registers an account; `/login` accepts its email and password. Passwords require 12 characters and at most 72 UTF-8 bytes and use bcrypt. `GET /api/v1/session` reports role and safe account details. `GET /api/v1/account/orders` returns account-owned history.
- `GET/POST /api/v1/admin/products`, product updates and stock adjustments support a growing catalogue. Product versions reject stale edits; inventory changes record actor, reason and resulting stock. Exact adjustment retries apply once.
- `GET/PUT /api/v1/account/wishlist` persists up to 100 saved products per account.
- Product review reads expose approved reviews only. Account review writes require a delivered purchase; edits return to pending moderation. Admin review decisions validate status and version to prevent approval of a changed body.
- Admin coupon APIs configure fixed AUD discounts, minimum merchandise subtotal, expiry, usage limit and enabled state. Cart coupon application/removal quotes server totals. Shipping eligibility uses the subtotal before discounts; discounts never exceed merchandise subtotal. Invalid coupons remain removable and block checkout.
- Checkout locks cart, account when authenticated, ordered product rows and coupon; stock and coupon redemption commit atomically. An exact order replay does not redeem again. Cancellation restores stock but does not restore coupon usage.

Email/SMS notifications, email verification/password recovery, carrier integrations and image uploads remain future work. No external provider is configured. Payment remains COD only.
