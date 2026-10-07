package com.astavet.shop.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Digits;
import java.math.BigDecimal;
public record SaveProductRequest(@NotBlank @Pattern(regexp = "[a-z0-9]+(?:-[a-z0-9]+)*") @Size(max = 100) String slug,
        @NotBlank @Size(max = 160) String name, @NotBlank @Size(max = 2000) String description,
        @NotNull @DecimalMin("0.01") @DecimalMax("1000000.00") @Digits(integer = 7, fraction = 2) BigDecimal price,
        boolean active, @NotBlank @Pattern(regexp = "/assets/[a-zA-Z0-9/_-]+\\.(?:png|jpg|jpeg|webp|svg)") @Size(max = 300) String imagePath,
        @NotNull @Min(0) Long expectedVersion) {}
