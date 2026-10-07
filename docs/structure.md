# Backend project structure

Java source root: `src/main/java/com/astavet/shop/`. The application remains one Spring Boot module; the React client is a separate repository at `../astavet-frontend`.

```text
com.astavet.shop/
├── ShopApplication.java
├── controller/
│   ├── v1/             HTTP endpoints and validated DTO binding
│   └── support/        Guest session identity helper
├── service/
│   └── impl/           Business rules, ownership and transactions
├── repository/
│   ├── impl/           Persistence adapters and entity/domain mapping
│   └── jpa/            Spring Data queries and locking metadata
├── dto/                Request and response contracts
├── domain/             Business models, states and calculations
├── entity/             JPA table and relationship mappings
├── config/             Security, auditing and request tracing
└── exception/          ShopException and HTTP exception advice
```

Controllers depend on service contracts and DTOs. Services use domain models and repository contracts. Repository implementations map between domain models and entities; JPA repositories operate on entities. The top-level `entity/` folder does not allow entities in service/controller/HTTP signatures. Handwritten mappers remain internal to repository implementations.

`src/main/resources/db/migration/` owns Flyway migrations. `application.yml` owns shared configuration; test profiles remain under `src/test/resources/`. Schema validation and framework versions are unchanged.

Tests under `src/test/java/com/astavet/shop/` mirror their scope:

- `domain/OrderStatusTest.java`: fulfillment state rules.
- `service/QuoteServiceTest.java`: pricing and quote calculations.
- `integration/ShopIntegrationTest.java`: real Spring, HTTP/security, JPA, query counts and concurrency.

Run the same `./gradlew check bootJar` and dedicated-database `postgresTest` tasks; package relocation does not introduce a new test lifecycle. See [verification](verification.md) for executed evidence and [frontend structure](../../astavet-frontend/README.md) for the separate client.
