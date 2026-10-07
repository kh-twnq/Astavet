package com.astavet.shop.dto;

import com.astavet.shop.domain.Account;
import java.util.UUID;

public record AccountResponse(UUID id, String email, String name) {
  public static AccountResponse from(Account account) {
    return new AccountResponse(account.id(), account.email(), account.name());
  }
}
