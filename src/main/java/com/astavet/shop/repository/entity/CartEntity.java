package com.astavet.shop.repository.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "carts")
public class CartEntity {
    @Id public UUID id;
    @ElementCollection
    @CollectionTable(name = "cart_lines", joinColumns = @JoinColumn(name = "cart_id"))
    public List<CartLineEntity> lines = new ArrayList<>();
}
