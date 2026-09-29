package com.astavet.entity.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ProductVariantTest {

    @Test
    void reservesAvailableStock() {
        Product product = new Product("test", "Test", null, null, ProductStatus.ACTIVE);
        ProductVariant variant = new ProductVariant(product, "Default", "SKU", 100_000, 5, true);

        variant.reserve(2);

        assertThat(variant.getStockQuantity()).isEqualTo(3);
    }

    @Test
    void rejectsInvalidOrInsufficientReservation() {
        Product product = new Product("test", "Test", null, null, ProductStatus.ACTIVE);
        ProductVariant variant = new ProductVariant(product, "Default", "SKU", 100_000, 1, true);

        assertThatThrownBy(() -> variant.reserve(2)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> variant.reserve(0)).isInstanceOf(IllegalArgumentException.class);
        assertThat(variant.getStockQuantity()).isEqualTo(1);
    }

    @Test
    void releasesReservedStock() {
        Product product = new Product("test", "Test", null, null, ProductStatus.ACTIVE);
        ProductVariant variant = new ProductVariant(product, "Default", "SKU", 100_000, 2, true);

        variant.release(3);

        assertThat(variant.getStockQuantity()).isEqualTo(5);
        assertThatThrownBy(() -> variant.release(0)).isInstanceOf(IllegalArgumentException.class);
    }
}
