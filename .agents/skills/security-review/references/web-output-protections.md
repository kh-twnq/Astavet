# Web Output Protections — XSS, CSRF, Security Headers

Reference for [security-review](../SKILL.md). Browser-facing output/response concerns:
encoding on the way out, CSRF tokens for session-based apps, and the response headers that
harden a browser client against the app's own responses.

## XSS Prevention

### Output Encoding

```java
// ✅ GOOD: Use templating engine's auto-escaping

// Thymeleaf - auto-escapes by default
<p th:text="${userInput}">...</p>  // Safe

// To display HTML (dangerous, use carefully):
<p th:utext="${trustedHtml}">...</p>  // Only for trusted content!

// ✅ GOOD: Manual encoding when needed
import org.owasp.encoder.Encode;

String safe = Encode.forHtml(userInput);
String safeJs = Encode.forJavaScript(userInput);
String safeUrl = Encode.forUriComponent(userInput);
```

**Maven dependency for OWASP Encoder:**
```xml
<dependency>
    <groupId>org.owasp.encoder</groupId>
    <artifactId>encoder</artifactId>
    <version>1.2.3</version>
</dependency>
```

### Content Security Policy

```java
// Add CSP header to prevent inline scripts

// Spring Boot
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.headers(headers -> headers
            .contentSecurityPolicy(csp -> csp
                .policyDirectives("default-src 'self'; script-src 'self'; style-src 'self'")
            )
        );
        return http.build();
    }
}

// Servlet Filter (works everywhere)
@WebFilter("/*")
public class SecurityHeadersFilter implements Filter {
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) res;
        response.setHeader("Content-Security-Policy", "default-src 'self'");
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("X-XSS-Protection", "1; mode=block");
        chain.doFilter(req, res);
    }
}
```

## CSRF Protection

### Spring Security

Keep CSRF protection enabled for browser requests using automatically sent
credentials such as session/authentication cookies. JWT/stateless labels alone
do not establish immunity to CSRF. A session-based chain can keep the default:

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(Customizer.withDefaults());
    return http.build();
}
```

For a cookie-backed SPA, configure the token repository and request handling for
its Spring Security version; allow JavaScript to read the CSRF cookie only when
that integration needs it. Do not disable CSRF and then configure its repository
in the same example. A distinct API chain can disable CSRF only after verifying
its authentication does not rely on credentials a browser sends automatically.
See [Spring CSRF guidance](https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html).

### Quarkus

Use the `quarkus-rest-csrf` extension with the supported configuration namespace,
not `quarkus.http.csrf.*`:

```properties
quarkus.rest-csrf.enabled=true
quarkus.rest-csrf.cookie-name=XSRF-TOKEN
```

Confirm configuration and the client token integration for the installed version
in [Quarkus CSRF prevention](https://quarkus.io/guides/security-csrf-prevention/).

## Security Headers

### Recommended Headers

| Header | Value | Purpose |
|--------|-------|---------|
| `Content-Security-Policy` | `default-src 'self'` | Prevent XSS |
| `X-Content-Type-Options` | `nosniff` | Prevent MIME sniffing |
| `X-Frame-Options` | `DENY` | Prevent clickjacking |
| `Strict-Transport-Security` | `max-age=31536000` | Force HTTPS |
| `X-XSS-Protection` | `1; mode=block` | Legacy XSS filter |

### Spring Boot Configuration

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.headers(headers -> headers
        .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'"))
        .frameOptions(frame -> frame.deny())
        .httpStrictTransportSecurity(hsts -> hsts.maxAgeInSeconds(31536000))
        .contentTypeOptions(Customizer.withDefaults())
    );
    return http.build();
}
```
