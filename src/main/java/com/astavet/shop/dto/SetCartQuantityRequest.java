package com.astavet.shop.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record SetCartQuantityRequest(
    @NotNull UUID productId, @NotNull @Min(0) @Max(99) Integer quantity) {}
