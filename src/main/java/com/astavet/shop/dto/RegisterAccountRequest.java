package com.astavet.shop.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
public record RegisterAccountRequest(@NotBlank @Email @Size(max = 254) String email,
        @NotBlank @Size(max = 100) String name, @NotBlank @Size(min = 12, max = 72) String password) {
    @Override public String toString() { return "RegisterAccountRequest[redacted]"; }
}
