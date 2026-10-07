package com.astavet.shop.repository.impl;
import com.astavet.shop.domain.Account;
import com.astavet.shop.domain.ShopException;
import com.astavet.shop.repository.AccountRepository;
import com.astavet.shop.repository.entity.AccountEntity;
import com.astavet.shop.repository.jpa.JpaAccountRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
@Repository
public class AccountRepositoryImpl implements AccountRepository {
    private final JpaAccountRepository jpa;
    public AccountRepositoryImpl(JpaAccountRepository jpa) { this.jpa = jpa; }
    @Override public Optional<Account> findByEmail(String email) { return jpa.findByEmail(email).map(this::map); }
    @Override public Account save(Account a) {
        AccountEntity e = new AccountEntity(); e.id = a.id(); e.email = a.email(); e.name = a.name();
        e.passwordHash = a.passwordHash(); e.createdAt = a.createdAt();
        return map(jpa.saveAndFlush(e));
    }
    @Override public void lock(UUID id) { jpa.lock(id).orElseThrow(() -> new ShopException(404, "Account not found.")); }
    private Account map(AccountEntity e) { return new Account(e.id, e.email, e.name, e.passwordHash, e.createdAt); }
}
