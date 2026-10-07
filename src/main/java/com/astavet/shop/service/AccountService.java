package com.astavet.shop.service;

import com.astavet.shop.domain.Account;
import java.util.Optional;
import java.util.UUID;

public interface AccountService {
  Account register(String email, String name, String password);

  Optional<Account> findByEmail(String email);

  Account current();

  UUID currentId();

  UUID lockCurrent();
}
