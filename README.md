# AstaVet COD shop

Java 21 / Spring Boot 4.1.1 commerce MVP, with PostgreSQL, JPA, Flyway and a same-origin, responsive browser interface. No JavaScript build step or online-payment integration is needed.

Customer flow: product catalogue/detail → server-backed bag → Australian delivery checkout → order confirmation. The initial catalogue has one product; products, cart lines and order lines support multiple products without a frontend or backend rewrite. `/admin.html` redirects unauthenticated visitors to the admin sign-in page. Admins can view customer details and line items, paginate orders, confirm, ship, mark delivered and cancel before shipment.

The design and initial product are based on [the supplied AstaVet reference](https://www.astavet.com/products/astaxanthin-200g). Its title is **AstaVet 130g**, although its URL says 200g. The seeded **AUD 49.00 price and 100 units are provisional demo data**, since the reference is sold out and does not expose a usable price. Shipping is provisionally AUD 7.95, free from AUD 100.00. The jar is a custom illustration, not official packaging photography. Confirm catalogue, fulfilment terms, taxes, branding rights and imagery before opening the shop to customers. No email notifications are sent.

## Run

Requirements: Java 21, Node 16+ for the two frontend regressions, and PostgreSQL. The Gradle wrapper downloads Gradle/dependencies on its first run.

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

Open `http://localhost:8080`; administration is at `/admin.html`. Compose binds the app to localhost and defaults secure cookies to false for local HTTP. The database volume preserves data across restarts. Hosted operation requires HTTPS, secure cookies, database backups and ingress request/body limits plus rate limiting on `/login` and `/api/v1/orders`. These deployment services are outside this repository; the shop has no in-process rate limiter.

## Server invariants

- Controllers accept validated request DTOs and return explicit response DTOs. Services use immutable domain records; JPA entities stay inside `repository/`. Service methods own transaction boundaries. No raw JDBC is used.
- Prices, currency, shipping and totals come from the database/server. JSON fields such as `price`, `total`, `paymentMethod` or arbitrary order states are rejected. Quotes fingerprint line identifiers, quantities, product names, unit prices and shipping. Checkout must match a fresh locked quote, so changed prices/content require customer review.
- The cart row is locked before checkout; products are locked in UUID order. Inventory reservation, immutable order snapshots, audit events and cart clearing commit together. No stock is reserved by adding to a bag. Database constraints prohibit negative stock and duplicate order keys.
- Checkout keys are scoped to the guest cart and persisted with a request hash. Simultaneous retries return one order and reserve once. Changed data with a used key gets HTTP 409. The browser saves the exact pending submission before sending it and retries it after an uncertain response, even if the cart was already cleared. A successful retry does not clear items added after the original checkout.
- Order transitions: `PLACED → CONFIRMED → SHIPPED → DELIVERED`. Cancellation is allowed from `PLACED` or `CONFIRMED`. The admin supplies its expected state; stale requests get HTTP 409. Repeating an already applied state change is a no-op. Order locking makes cancellation restore inventory and record its audit event exactly once.
- Guest order reads require the session's cart ownership; other sessions receive 404. Admin APIs and services require `ADMIN`. Login/logout and all writes keep Spring Security CSRF protection. Browser output is escaped and a restrictive CSP is set.
- Guest ownership lives in the HTTP session. This MVP runs as one instance, or requires sticky sessions. A session expiry/restart removes customer access to its old confirmation; admins retain database access. Durable shared sessions or authenticated customer recovery can be added later without changing order ownership rules.
- Transaction failures roll back; transient database failures return a sanitized 503. Retry checkout with the saved key and request. Logs contain order IDs/state, not customer details or checkout payloads.

## API

| Method | Route | Contract |
| --- | --- | --- |
| GET | `/api/v1/products` | Active catalogue |
| GET | `/api/v1/csrf` | Masked session token and header name |
| GET | `/api/v1/cart` | Session bag, current totals and fingerprint |
| PUT | `/api/v1/cart/lines` | `{productId, quantity}`; quantity 0 removes a line |
| POST | `/api/v1/orders` | `{idempotencyKey, quoteFingerprint, name, email, phone, address, city, postcode, state}`; COD/Australia fixed by server |
| GET | `/api/v1/orders/{id}` | Session-owned confirmation |
| GET | `/api/v1/admin/orders?page=0` | 25 orders per page, newest first |
| GET | `/api/v1/admin/orders/{id}` | Admin order detail |
| PUT | `/api/v1/admin/orders/{id}/status` | `{expectedStatus, status}` |

A successful order submission or replay returns HTTP 200 with the same order ID. Validation failures return 400, conflicts 409, missing/inaccessible resources 404, and transient database failures 503.

Add products through a new reviewed Flyway migration, supplying a unique UUID/slug, name, description, AUD price, stock and active flag. Historical order names/prices remain snapshots. Do not edit an applied migration. Product editing and catalogue image uploads are outside this MVP.

## Verification

```sh
./gradlew check bootJar
python3 harness/scripts/harness.py doctor
python3 harness/scripts/check_parity.py --installed
python3 harness/scripts/harness.py scan
```

`test` runs domain and full Spring/JPA/MockMvc integration tests against H2 in PostgreSQL mode, applying both real migrations and validating the schema. Critical regressions cover pricing, authentication, CSRF, ownership, idempotency, concurrent first-cart creation, last-unit competition, rollback, state transitions and concurrent cancellation. `frontendTest` runs two dependency-free Node regressions for CSRF-session initialization and exact pending-checkout replay. `check` includes both.

For the same integration suite on PostgreSQL, set `TEST_DATABASE_URL`, `TEST_DATABASE_USERNAME` and `TEST_DATABASE_PASSWORD` to a **dedicated disposable test database**, then run `./gradlew postgresTest`. Tests delete application rows between cases. The CI workflow runs H2 and PostgreSQL checks; it has not been executed remotely during implementation.

See [verification evidence](docs/verification.md) for the actual checks and environment limitations from this implementation. The installed harness remains authoritative. Do not claim ledger/graph readiness without a committed base and snapshot-bound verification.
