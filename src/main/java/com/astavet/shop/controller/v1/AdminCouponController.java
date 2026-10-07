package com.astavet.shop.controller.v1;
import com.astavet.shop.dto.SaveCouponRequest;
import com.astavet.shop.dto.CouponResponse;
import com.astavet.shop.service.CouponService;
import jakarta.validation.Valid;
import java.util.UUID;
import java.util.List;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
@RestController @RequestMapping("/api/v1/admin/coupons")
public class AdminCouponController {
    private final CouponService coupons;
    public AdminCouponController(CouponService coupons) { this.coupons = coupons; }
    @GetMapping public List<CouponResponse> list(@RequestParam(defaultValue = "0") int page) { return coupons.list(page).stream().map(CouponResponse::from).toList(); }
    @PostMapping public CouponResponse create(@Valid @RequestBody SaveCouponRequest r) { return save(null, r); }
    @PutMapping("/{id}") public CouponResponse update(@PathVariable UUID id, @Valid @RequestBody SaveCouponRequest r) { return save(id, r); }
    private CouponResponse save(UUID id, SaveCouponRequest r) { return CouponResponse.from(coupons.save(id, r.code(), r.amount(), r.minimumSubtotal(), r.expiresAt(), r.maxUses(), r.active(), r.expectedVersion())); }
}
