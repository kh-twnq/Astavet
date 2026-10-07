package com.astavet.shop.dto;

import com.astavet.shop.domain.Customer;

public record CustomerResponse(
    String name,
    String email,
    String phone,
    String address,
    String city,
    String postcode,
    String state) {
  public static CustomerResponse from(Customer customer) {
    return new CustomerResponse(
        customer.name(),
        customer.email(),
        customer.phone(),
        customer.address(),
        customer.city(),
        customer.postcode(),
        customer.state());
  }
}
