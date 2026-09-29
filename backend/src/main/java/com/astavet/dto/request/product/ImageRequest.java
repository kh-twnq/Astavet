package com.astavet.dto.request.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ImageRequest(
        @NotBlank @Size(max = 1000)
        @Pattern(regexp = "^(https://|/)[^\\s]+$", message = "Ảnh phải dùng HTTPS hoặc đường dẫn nội bộ") String url,
        @Size(max = 255) String altText) {
}
