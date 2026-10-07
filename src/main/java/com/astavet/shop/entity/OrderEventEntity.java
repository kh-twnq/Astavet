package com.astavet.shop.entity;

import com.astavet.shop.domain.OrderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "order_events")
public class OrderEventEntity {
  @Id public UUID id;

  @Column(nullable = false)
  public UUID orderId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  public OrderStatus status;

  @Column(nullable = false, length = 100)
  public String actor;

  @Column(nullable = false)
  public Instant occurredAt;
}
