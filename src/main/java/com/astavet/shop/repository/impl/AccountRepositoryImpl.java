package com.astavet.shop.repository.impl;

import com.astavet.shop.domain.Account;
import com.astavet.shop.entity.AccountEntity;
import com.astavet.shop.exception.ShopException;
import com.astavet.shop.repository.AccountRepository;
import com.astavet.shop.repository.jpa.JpaAccountRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class AccountRepositoryImpl implements AccountRepository {
  private final JpaAccountRepository jpa;

  public AccountRepositoryImpl(JpaAccountRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  public Optional<Account> findByEmail(String email) {
    return jpa.findByEmail(email).map(this::map);
  }

  @Override
  public Account save(Account account) {
    AccountEntity e = new AccountEntity();
    e.id = account.id();
    e.email = account.email();
    e.name = account.name();
    e.passwordHash = account.passwordHash();
    e.createdAt = account.createdAt();
    return map(jpa.saveAndFlush(e));
  }

  @Override
  public void lock(UUID id) {
    jpa.lock(id).orElseThrow(() -> new ShopException(404, "Account not found."));
  }

  private Account map(AccountEntity entity) {
    return new Account(entity.id, entity.email, entity.name, entity.passwordHash, entity.createdAt);
  }
}
