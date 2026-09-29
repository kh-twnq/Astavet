package com.astavet.dto.response.auth;

public record SessionResponse(boolean authenticated, String email) {
}
