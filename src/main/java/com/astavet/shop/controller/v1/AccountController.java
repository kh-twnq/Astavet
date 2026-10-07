package com.astavet.shop.controller.v1;

import com.astavet.shop.dto.AccountResponse;
import com.astavet.shop.dto.OrderResponse;
import com.astavet.shop.dto.RegisterAccountRequest;
import com.astavet.shop.service.AccountService;
import com.astavet.shop.service.OrderService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class AccountController {
  private final AccountService accounts;
  private final OrderService orders;

  public AccountController(AccountService accounts, OrderService orders) {
    this.accounts = accounts;
    this.orders = orders;
  }

  @GetMapping("/session")
  public Map<String, Object> session(Authentication authentication) {
    if (authentication == null) {
      return Map.of("role", "GUEST");
    }
    if (authentication.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
      return Map.of("role", "ADMIN");
    }
    return Map.of("role", "CUSTOMER", "account", AccountResponse.from(accounts.current()));
  }

  @PostMapping("/accounts")
  public AccountResponse register(@Valid @RequestBody RegisterAccountRequest request) {
    return AccountResponse.from(
        accounts.register(request.email(), request.name(), request.password()));
  }

  @GetMapping("/account/orders")
  public List<OrderResponse> orders(@RequestParam(defaultValue = "0") int page) {
    return orders.listMine(page).stream().map(OrderResponse::from).toList();
  }
}
