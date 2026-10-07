package com.astavet.shop.service.impl;
import com.astavet.shop.exception.ShopException;
import com.astavet.shop.domain.Quote;
import com.astavet.shop.domain.Coupon;
import com.astavet.shop.domain.Digests;
import com.astavet.shop.repository.CouponRepository;
import com.astavet.shop.service.CouponService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
@Service
@Transactional(readOnly = true)
public class CouponServiceImpl implements CouponService {
    private final CouponRepository repository;
    public CouponServiceImpl(CouponRepository repository) { this.repository = repository; }
    @Override public Quote apply(Quote base, String code, boolean checkout) {
        if (code == null) return base;
        Optional<Coupon> found = checkout ? repository.lock(code) : repository.find(code);
        boolean available = found.isPresent() && found.get().available(base.subtotal(), Instant.now()) && !base.lines().isEmpty();
        if (!available && checkout) throw new ShopException(409, "Coupon unavailable, expired or below its minimum. Remove it or use another code.");
        BigDecimal discount = available ? found.get().amount().min(base.subtotal()) : new BigDecimal("0.00");
        String fingerprint = Digests.sha256(base.fingerprint() + Digests.field(code) + discount.toPlainString() + available);
        return new Quote(base.lines(), base.subtotal(), base.shipping(), base.total().subtract(discount), base.currency(), fingerprint,
                code, discount, available ? null : "Coupon unavailable. Remove it or use another code.");
    }
    @Override @Transactional(propagation = Propagation.MANDATORY) public void redeem(String code) {
        if (code != null) repository.increment(code);
    }
    @Override @PreAuthorize("hasRole('ADMIN')") public List<Coupon> list(int page) {
        if (page < 0 || page > 100000) throw new ShopException(400, "Invalid page number.");
        return repository.list(page);
    }
    @Override @Transactional @PreAuthorize("hasRole('ADMIN')") public Coupon save(UUID id, String code, BigDecimal amount, BigDecimal minimumSubtotal,
            Instant expiresAt, int maxUses, boolean active, long expectedVersion) {
        if (active && !expiresAt.isAfter(Instant.now())) throw new ShopException(400, "Active coupons must expire in the future.");
        Coupon previous = id == null ? null : repository.lock(code).orElseThrow(() -> new ShopException(404, "Coupon not found."));
        if (previous != null && (!previous.id().equals(id) || previous.version() != expectedVersion)) throw new ShopException(409, "Coupon changed. Refresh before saving.");
        if (previous != null && maxUses < previous.uses()) throw new ShopException(409, "The usage limit cannot be below existing redemptions.");
        try {
            return repository.save(new Coupon(id == null ? UUID.randomUUID() : id, code, amount, minimumSubtotal, expiresAt,
                    maxUses, previous == null ? 0 : previous.uses(), active, previous == null ? 0 : previous.version()));
        } catch (DataIntegrityViolationException exception) { throw new ShopException(409, "Coupon code is already in use."); }
    }
}
