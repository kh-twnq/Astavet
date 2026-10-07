package com.astavet.shop.controller.v1;

import com.astavet.shop.dto.CartResponse;
import com.astavet.shop.dto.SetCouponRequest;
import com.astavet.shop.dto.CreateOrderRequest;
import com.astavet.shop.dto.OrderResponse;
import com.astavet.shop.dto.ProductResponse;
import com.astavet.shop.dto.SetCartQuantityRequest;
import com.astavet.shop.service.CartService;
import com.astavet.shop.service.OrderService;
import com.astavet.shop.service.ProductService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class ShopController {
    private final ProductService products;
    private final CartService carts;
    private final OrderService orders;
    private final GuestIdentity identity;
    public ShopController(ProductService products, CartService carts, OrderService orders, GuestIdentity identity) {
        this.products = products; this.carts = carts; this.orders = orders; this.identity = identity;
    }
    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken token) { return Map.of("token", token.getToken(), "headerName", token.getHeaderName()); }
    @GetMapping("/products")
    public List<ProductResponse> products() { return products.list().stream().map(ProductResponse::from).toList(); }
    @GetMapping("/cart")
    public CartResponse cart(HttpSession session) { return CartResponse.from(carts.view(identity.cartId(session))); }
    @PutMapping("/cart/lines")
    public CartResponse quantity(HttpSession session, @Valid @RequestBody SetCartQuantityRequest request) {
        return CartResponse.from(carts.setQuantity(identity.cartId(session), request.productId(), request.quantity()));
    }
    @PutMapping("/cart/coupon") public CartResponse coupon(HttpSession session, @Valid @RequestBody SetCouponRequest request) {
        return CartResponse.from(carts.setCoupon(identity.cartId(session), request.code()));
    }
    @PostMapping("/orders")
    public OrderResponse place(HttpSession session, @Valid @RequestBody CreateOrderRequest request) {
        return OrderResponse.from(orders.place(identity.cartId(session), request.toModel()));
    }
    @GetMapping("/orders/{id}")
    public OrderResponse order(HttpSession session, @PathVariable UUID id) {
        return OrderResponse.from(orders.findForCustomer(identity.cartId(session), id));
    }
}
