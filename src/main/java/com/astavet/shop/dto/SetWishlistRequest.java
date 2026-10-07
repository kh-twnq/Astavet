package com.astavet.shop.dto;

import jakarta.validation.constraints.NotNull;

public record SetWishlistRequest(@NotNull Boolean saved) {}
