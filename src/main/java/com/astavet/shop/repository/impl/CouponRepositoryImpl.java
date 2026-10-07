package com.astavet.shop.repository.impl;
import com.astavet.shop.domain.Coupon;
import com.astavet.shop.repository.CouponRepository;
import com.astavet.shop.entity.CouponEntity;
import com.astavet.shop.repository.jpa.JpaCouponRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
@Repository
public class CouponRepositoryImpl implements CouponRepository {
    private final JpaCouponRepository jpa;
    public CouponRepositoryImpl(JpaCouponRepository jpa) { this.jpa = jpa; }
    @Override public Optional<Coupon> find(String code) { return jpa.findByCode(code).map(this::map); }
    @Override public Optional<Coupon> lock(String code) { return jpa.lock(code).map(this::map); }
    @Override public List<Coupon> list(int page) { return jpa.findAllByOrderByCodeAsc(PageRequest.of(page, 25)).stream().map(this::map).toList(); }
    @Override public Coupon save(Coupon c) {
        CouponEntity e = new CouponEntity(); e.id = c.id(); e.code = c.code(); e.amount = c.amount(); e.minimumSubtotal = c.minimumSubtotal();
        e.expiresAt = c.expiresAt(); e.maxUses = c.maxUses(); e.uses = c.uses(); e.active = c.active(); e.version = c.version();
        return map(jpa.saveAndFlush(e));
    }
    @Override public void increment(String code) { jpa.findByCode(code).orElseThrow().uses++; }
    private Coupon map(CouponEntity e) { return new Coupon(e.id, e.code, e.amount, e.minimumSubtotal, e.expiresAt, e.maxUses, e.uses, e.active, e.version); }
}
