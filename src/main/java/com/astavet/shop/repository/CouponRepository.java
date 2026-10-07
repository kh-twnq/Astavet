package com.astavet.shop.repository;

import com.astavet.shop.domain.Coupon;
import java.util.List;
import java.util.Optional;

public interface CouponRepository {
  Optional<Coupon> find(String code);

  Optional<Coupon> lock(String code);

  List<Coupon> list(int page);

  Coupon save(Coupon coupon);

  void increment(String code);
}
