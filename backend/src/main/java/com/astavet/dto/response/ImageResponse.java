package com.astavet.dto.response;

import java.util.UUID;

public record ImageResponse(UUID id, String url, String altText, int sortOrder) {
}
