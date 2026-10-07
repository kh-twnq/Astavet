package com.astavet.shop.repository.entity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name = "wishlists")
public class WishlistEntity {
    @Id public UUID id;
    @Column(nullable = false) public UUID accountId;
    @Column(nullable = false) public UUID productId;
    @Column(nullable = false) public Instant createdAt;
}
