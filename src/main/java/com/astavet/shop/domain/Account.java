package com.astavet.shop.domain;

import java.time.Instant;
import java.util.UUID;

public record Account(UUID id, String email, String name, String passwordHash, Instant createdAt) {}
