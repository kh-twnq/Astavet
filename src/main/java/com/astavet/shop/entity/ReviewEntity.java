package com.astavet.shop.entity;

import com.astavet.shop.domain.ReviewStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reviews")
public class ReviewEntity {
  @Version public long version;
  @Id public UUID id;

  @Column(nullable = false)
  public UUID accountId;

  @Column(nullable = false)
  public UUID productId;

  @Column(nullable = false, length = 100)
  public String authorName;

  public int rating;

  @Column(nullable = false, length = 2000)
  public String body;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  public ReviewStatus status;

  @Column(nullable = false)
  public Instant createdAt;

  @Column(nullable = false)
  public Instant updatedAt;
}
