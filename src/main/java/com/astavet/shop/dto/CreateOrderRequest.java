package com.astavet.shop.dto;

import com.astavet.shop.domain.Checkout;
import com.astavet.shop.domain.Customer;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateOrderRequest(
    @NotNull UUID idempotencyKey,
    @NotBlank @Pattern(regexp = "[a-f0-9]{64}") String quoteFingerprint,
    @NotBlank @Size(max = 100) String name,
    @NotBlank @Email @Size(max = 254) String email,
    @NotBlank @Pattern(regexp = "[+0-9() .-]{6,30}") String phone,
    @NotBlank @Size(max = 300) String address,
    @NotBlank @Size(max = 100) String city,
    @NotBlank @Pattern(regexp = "[0-9]{4}") String postcode,
    @NotBlank @Pattern(regexp = "NSW|VIC|QLD|WA|SA|TAS|ACT|NT") String state) {
  public Checkout toModel() {
    return new Checkout(
        idempotencyKey,
        quoteFingerprint,
        new Customer(
            name.trim(), email.trim(), phone.trim(), address.trim(), city.trim(), postcode, state));
  }
}
