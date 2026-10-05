package com.astavet.shop.domain;

import java.util.UUID;

public record CartLine(UUID productId, int quantity) {}
