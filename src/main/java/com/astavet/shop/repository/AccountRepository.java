package com.astavet.shop.repository;

import com.astavet.shop.domain.Account;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository {
  Optional<Account> findByEmail(String email);

  Account save(Account account);

  void lock(UUID id);
}
