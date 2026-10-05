package com.astavet.shop.config;

import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.http.HttpStatus;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    public UserDetailsService users(@Value("${shop.admin.username}") String username,
            @Value("${shop.admin.password-hash}") String hash, Environment environment) {
        boolean testing = Arrays.stream(environment.getActiveProfiles()).anyMatch(p -> p.equals("test") || p.equals("postgres-test"));
        if (username.isBlank() || username.length() > 100 || (!testing && !hash.matches("\\{bcrypt\\}\\$2[aby]\\$(1[0-9]|2[0-9]|3[01])\\$[./A-Za-z0-9]{53}"))) {
            throw new IllegalArgumentException("Configure a valid administrator username and bcrypt password hash.");
        }
        return new InMemoryUserDetailsManager(User.withUsername(username).password(hash).roles("ADMIN").build());
    }
    @Bean
    public SecurityFilterChain security(HttpSecurity http) throws Exception {
        http.csrf(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/products", "/api/v1/csrf",
                                "/api/v1/cart", "/api/v1/cart/lines", "/api/v1/orders", "/api/v1/orders/*", "/error").permitAll()
                        .anyRequest().denyAll())
                .formLogin(login -> login.loginPage("/login").loginProcessingUrl("/login")
                        .successHandler((request, response, authentication) -> response.setStatus(204))
                        .failureHandler((request, response, exception) -> response.setStatus(401)).permitAll())
                .logout(logout -> logout.logoutSuccessHandler((request, response, authentication) -> response.setStatus(204)))
                .exceptionHandling(errors -> errors.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .headers(headers -> headers.contentSecurityPolicy(csp -> csp.policyDirectives(
                        "default-src 'self'; script-src 'self'; style-src 'self'; img-src 'self'; object-src 'none'; base-uri 'self'; frame-ancestors 'none'; form-action 'self'")));
        return http.build();
    }
}
