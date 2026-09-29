package com.astavet.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "store")
public record StoreProperties(String frontendOrigin, long shippingFee, Admin admin) {
    public record Admin(String email, String password) {
    }
}

