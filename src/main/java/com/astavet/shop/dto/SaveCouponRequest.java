package com.astavet.shop.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.Instant;

public record SaveCouponRequest(
    @NotBlank @Pattern(regexp = "[A-Z0-9-]{3,30}") String code,
    @NotNull @DecimalMin("0.01") @DecimalMax("1000000.00") @Digits(integer = 7, fraction = 2)
        BigDecimal amount,
    @NotNull @DecimalMin("0.00") @DecimalMax("1000000.00") @Digits(integer = 7, fraction = 2)
        BigDecimal minimumSubtotal,
    @NotNull Instant expiresAt,
    @NotNull @Min(1) @Max(1000000) Integer maxUses,
    boolean active,
    @NotNull @Min(0) Long expectedVersion) {}
