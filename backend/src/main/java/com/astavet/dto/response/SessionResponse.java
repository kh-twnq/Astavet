package com.astavet.dto.response;

public record SessionResponse(boolean authenticated, String email) {
}
