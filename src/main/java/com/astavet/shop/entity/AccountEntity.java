package com.astavet.shop.entity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import java.time.Instant;
import java.util.UUID;
@Entity
@Table(name = "accounts")
public class AccountEntity {
    @Id public UUID id;
    @Column(nullable = false, unique = true, length = 254) public String email;
    @Column(nullable = false, length = 100) public String name;
    @Column(nullable = false, length = 100) public String passwordHash;
    @Column(nullable = false) public Instant createdAt;
}
