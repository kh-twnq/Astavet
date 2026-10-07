package com.astavet.shop.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record AdjustStockRequest(
    @NotNull UUID operationId,
    @NotNull @Min(-1000000) @Max(1000000) Integer delta,
    @NotBlank @Size(max = 300) String reason,
    @NotNull @Min(0) Long expectedVersion) {}
