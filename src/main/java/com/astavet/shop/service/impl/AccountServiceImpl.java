package com.astavet.shop.service.impl;
import com.astavet.shop.domain.Account;
import com.astavet.shop.exception.ShopException;
import com.astavet.shop.repository.AccountRepository;
import com.astavet.shop.service.AccountService;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
@Transactional(readOnly = true)
public class AccountServiceImpl implements AccountService {
    private final AccountRepository repository;
    private final String adminUsername;
    private final BCryptPasswordEncoder passwords = new BCryptPasswordEncoder(12);
    public AccountServiceImpl(AccountRepository repository, @Value("${shop.admin.username}") String adminUsername) {
        this.repository = repository; this.adminUsername = adminUsername;
    }
    @Override @Transactional public Account register(String email, String name, String password) {
        String normalized = normalize(email);
        if (password.length() < 12 || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ShopException(400, "Use at least 12 characters and at most 72 UTF-8 bytes for your password.");
        }
        if (normalized.equalsIgnoreCase(adminUsername) || repository.findByEmail(normalized).isPresent()) {
            throw new ShopException(409, "This email is unavailable.");
        }
        try { return repository.save(new Account(UUID.randomUUID(), normalized, name.trim(), "{bcrypt}" + passwords.encode(password), Instant.now())); }
        catch (DataIntegrityViolationException exception) { throw new ShopException(409, "This email is unavailable."); }
    }
    @Override public Optional<Account> findByEmail(String email) { return repository.findByEmail(normalize(email)); }
    @Override @PreAuthorize("hasRole('CUSTOMER')") public Account current() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        return findByEmail(authentication.getName()).orElseThrow(() -> new ShopException(401, "Please sign in again."));
    }
    @Override public UUID currentId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"))) return null;
        return current().id();
    }
    @Override @Transactional(propagation = org.springframework.transaction.annotation.Propagation.MANDATORY) public UUID lockCurrent() {
        UUID id = currentId();
        if (id != null) repository.lock(id);
        return id;
    }
    private String normalize(String email) { return email.trim().toLowerCase(Locale.ROOT); }
}
