package com.astavet.shop.domain;

public record Customer(
    String name,
    String email,
    String phone,
    String address,
    String city,
    String postcode,
    String state) {}
