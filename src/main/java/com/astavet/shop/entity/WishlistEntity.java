package com.astavet.shop.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "wishlists")
public class WishlistEntity {
  @Id public UUID id;

  @Column(nullable = false)
  public UUID accountId;

  @Column(nullable = false)
  public UUID productId;

  @Column(nullable = false)
  public Instant createdAt;
}
