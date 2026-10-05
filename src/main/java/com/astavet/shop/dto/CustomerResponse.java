package com.astavet.shop.dto;

import com.astavet.shop.domain.Customer;

public record CustomerResponse(String name, String email, String phone, String address,
                               String city, String postcode, String state) {
    public static CustomerResponse from(Customer c) {
        return new CustomerResponse(c.name(), c.email(), c.phone(), c.address(), c.city(), c.postcode(), c.state());
    }
}
