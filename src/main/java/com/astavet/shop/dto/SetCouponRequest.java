package com.astavet.shop.dto;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotNull;
public record SetCouponRequest(@NotNull @Pattern(regexp = "(?:[A-Za-z0-9-]{3,30})?") String code) {}
