package com.astavet.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateOrderRequest(
        @NotBlank @Size(max = 150) String customerName,
        @NotBlank @Pattern(regexp = "^(\\+?84|0)[0-9]{9,10}$", message = "Số điện thoại không hợp lệ") String phone,
        @NotBlank @Size(max = 500) String address,
        @Size(max = 1000) String note,
        @NotBlank @Size(max = 100) String idempotencyKey,
        @NotEmpty @Size(max = 30) List<@Valid CreateOrderItemRequest> items) {
}
