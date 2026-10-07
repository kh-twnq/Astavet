package com.astavet.shop.repository.impl;

import com.astavet.shop.domain.Coupon;
import com.astavet.shop.entity.CouponEntity;
import com.astavet.shop.repository.CouponRepository;
import com.astavet.shop.repository.jpa.JpaCouponRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
public class CouponRepositoryImpl implements CouponRepository {
  private final JpaCouponRepository jpa;

  public CouponRepositoryImpl(JpaCouponRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  public Optional<Coupon> find(String code) {
    return jpa.findByCode(code).map(this::map);
  }

  @Override
  public Optional<Coupon> lock(String code) {
    return jpa.lock(code).map(this::map);
  }

  @Override
  public List<Coupon> list(int page) {
    return jpa.findAllByOrderByCodeAsc(PageRequest.of(page, 25)).stream().map(this::map).toList();
  }

  @Override
  public Coupon save(Coupon coupon) {
    CouponEntity e = new CouponEntity();
    e.id = coupon.id();
    e.code = coupon.code();
    e.amount = coupon.amount();
    e.minimumSubtotal = coupon.minimumSubtotal();
    e.expiresAt = coupon.expiresAt();
    e.maxUses = coupon.maxUses();
    e.uses = coupon.uses();
    e.active = coupon.active();
    e.version = coupon.version();
    return map(jpa.saveAndFlush(e));
  }

  @Override
  public void increment(String code) {
    jpa.findByCode(code).orElseThrow().uses++;
  }

  private Coupon map(CouponEntity entity) {
    return new Coupon(
        entity.id,
        entity.code,
        entity.amount,
        entity.minimumSubtotal,
        entity.expiresAt,
        entity.maxUses,
        entity.uses,
        entity.active,
        entity.version);
  }
}
