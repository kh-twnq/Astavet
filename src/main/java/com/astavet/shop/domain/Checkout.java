package com.astavet.shop.domain;

import java.util.UUID;

public record Checkout(UUID idempotencyKey, String quoteFingerprint, Customer customer) {}
