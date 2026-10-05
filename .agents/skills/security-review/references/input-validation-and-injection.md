# Input Validation & Injection Prevention

Reference for [security-review](../SKILL.md). Untrusted-input handling: validate at the
boundary, use parameterized queries, and never deserialize untrusted data with native Java
serialization.

## Input Validation (All Frameworks)

### Bean Validation (JSR 380)

Works in Spring, Quarkus, Jakarta EE, and standalone.

```java
// ✅ GOOD: Validate at boundary
public class CreateUserRequest {

    @NotNull(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be 3-50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, underscore")
    private String username;

    @NotNull
    @Email(message = "Invalid email format")
    private String email;

    @NotNull
    @Size(min = 8, max = 100)
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
             message = "Password must contain uppercase, lowercase, and number")
    private String password;

    @Min(value = 0, message = "Age cannot be negative")
    @Max(value = 150, message = "Invalid age")
    private Integer age;
}

// Controller/Resource - trigger validation
public Response createUser(@Valid CreateUserRequest request) {
    // request is already validated
}
```

### Custom Validators

```java
// Custom annotation
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SafeHtmlValidator.class)
public @interface SafeHtml {
    String message() default "Contains unsafe HTML";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

// Validator implementation
public class SafeHtmlValidator implements ConstraintValidator<SafeHtml, String> {

    private static final Pattern DANGEROUS_PATTERN = Pattern.compile(
        "<script|javascript:|on\\w+\\s*=", Pattern.CASE_INSENSITIVE
    );

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true;
        return !DANGEROUS_PATTERN.matcher(value).find();
    }
}
```

### Allowlist vs Blocklist

```java
// ❌ BAD: Blocklist (attackers find bypasses)
if (input.contains("<script>")) {
    throw new ValidationException("Invalid input");
}

// ✅ GOOD: Allowlist (only permit known-good)
private static final Pattern SAFE_NAME = Pattern.compile("^[a-zA-Z\\s'-]{1,100}$");

if (!SAFE_NAME.matcher(input).matches()) {
    throw new ValidationException("Invalid name format");
}
```


## SQL Injection Prevention

### JPA/Hibernate (All Frameworks)

```java
// ✅ GOOD: Parameterized queries
@Query("SELECT u FROM User u WHERE u.email = :email")
Optional<User> findByEmail(@Param("email") String email);

// ✅ GOOD: Criteria API
CriteriaBuilder cb = entityManager.getCriteriaBuilder();
CriteriaQuery<User> query = cb.createQuery(User.class);
Root<User> user = query.from(User.class);
query.where(cb.equal(user.get("email"), email));  // Safe

// ✅ GOOD: Named parameters
TypedQuery<User> query = entityManager.createQuery(
    "SELECT u FROM User u WHERE u.status = :status", User.class);
query.setParameter("status", status);  // Safe

// ❌ BAD: String concatenation
String jpql = "SELECT u FROM User u WHERE u.email = '" + email + "'";  // VULNERABLE!
```

### Native Queries

```java
// ✅ GOOD: Parameterized native query
@Query(value = "SELECT * FROM users WHERE email = ?1", nativeQuery = true)
User findByEmailNative(String email);

// ❌ BAD: Concatenated native query
String sql = "SELECT * FROM users WHERE email = '" + email + "'";  // VULNERABLE!
```

### JDBC (Plain Java)

```java
// ✅ GOOD: PreparedStatement
String sql = "SELECT * FROM users WHERE email = ? AND status = ?";
try (PreparedStatement stmt = connection.prepareStatement(sql)) {
    stmt.setString(1, email);
    stmt.setString(2, status);
    ResultSet rs = stmt.executeQuery();
}

// ❌ BAD: Statement with concatenation
String sql = "SELECT * FROM users WHERE email = '" + email + "'";  // VULNERABLE!
Statement stmt = connection.createStatement();
stmt.executeQuery(sql);
```

## SSRF (Server-Side Request Forgery)

Relevant any time a service builds an outbound HTTP call (WebClient/RestTemplate) from a URL or host
that originated in a request — a webhook target, a callback URL, an import-from-URL feature. First
check whether any outbound call in the diff actually takes a caller-supplied URL/host at all — most
outbound integrations call a fixed, hardcoded provider endpoint and this doesn't apply — but flag it
immediately if a new feature introduces a caller-influenced destination:

```java
// ❌ BAD: fetch a user-supplied URL directly
webClient.get().uri(request.getCallbackUrl()).retrieve()...

// Partial illustration: host and address checks alone are not a complete SSRF defense
if (!allowedHosts.contains(URI.create(request.getCallbackUrl()).getHost())) {
    throw new ValidationException("Callback host not permitted");
}
InetAddress resolved = InetAddress.getByName(URI.create(request.getCallbackUrl()).getHost());
if (resolved.isLoopbackAddress() || resolved.isLinkLocalAddress() || resolved.isSiteLocalAddress()) {
    throw new ValidationException("Callback resolves to a non-routable/internal address");
}
```

For a caller-influenced destination, enforce the allowed scheme/host/port and
validate every resolved IPv4/IPv6 address against the permitted destination set.
Java's three address helpers above do not cover every restricted/reserved range,
and a separate DNS lookup followed by a new client lookup can race. Inspect the
actual connection/resolution and network-egress controls; the partial snippet
is not proof of DNS-rebinding protection. Disable automatic redirects or apply
the full destination policy to every hop. See [OWASP SSRF prevention](https://cheatsheetseries.owasp.org/cheatsheets/Server_Side_Request_Forgery_Prevention_Cheat_Sheet.html).

## Secure Deserialization

Avoid Java `ObjectInputStream` on untrusted payloads. For JSON requests, bind to
an explicit DTO with the application's configured mapper and validated fields;
do not enable unrestricted class-based default typing.

```java
ObjectMapper mapper = new ObjectMapper();
mapper.deactivateDefaultTyping();
UserRequest request = mapper.readValue(json, UserRequest.class);
```

Disabling `FAIL_ON_UNKNOWN_PROPERTIES` controls how extra JSON fields are handled;
it is not a deserialization security control. Choose strictness for the API's
compatibility contract rather than claiming it prevents gadget attacks.

### Jackson configuration review

Check for `activateDefaultTyping`/`enableDefaultTyping`, permissive subtype
validators and `@JsonTypeInfo` using class names on externally supplied payloads.
Default typing being off does not neutralize a DTO that itself enables unsafe
polymorphism. If polymorphism is required, use a bounded set of expected subtypes
and a restrictive validator/logical type mapping, with tests rejecting unexpected
types. Configure the existing framework mapper deliberately instead of replacing
it and losing registered modules/serialization settings.

Source: [Jackson polymorphic deserialization security guidance](https://github.com/FasterXML/jackson-docs/wiki/JacksonPolymorphicDeserialization),
[OWASP deserialization guidance](https://cheatsheetseries.owasp.org/cheatsheets/Deserialization_Cheat_Sheet.html).
