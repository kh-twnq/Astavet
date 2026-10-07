package com.astavet.shop.service.impl;

import com.astavet.shop.domain.Cart;
import com.astavet.shop.domain.CartLine;
import com.astavet.shop.domain.Checkout;
import com.astavet.shop.domain.Customer;
import com.astavet.shop.domain.Digests;
import com.astavet.shop.domain.Order;
import com.astavet.shop.domain.OrderStatus;
import com.astavet.shop.domain.Product;
import com.astavet.shop.domain.Quote;
import com.astavet.shop.domain.ShopException;
import com.astavet.shop.repository.OrderRepository;
import com.astavet.shop.service.CartService;
import com.astavet.shop.service.AccountService;
import com.astavet.shop.service.CouponService;
import com.astavet.shop.service.OrderService;
import com.astavet.shop.service.ProductService;
import com.astavet.shop.service.QuoteService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {
    private static final Logger LOG = LoggerFactory.getLogger(OrderServiceImpl.class);
    private final OrderRepository repository;
    private final CartService carts;
    private final ProductService products;
    private final QuoteService quotes;
    private final AccountService accounts;
    private final CouponService coupons;
    public OrderServiceImpl(OrderRepository repository, CartService carts, ProductService products, QuoteService quotes, AccountService accounts, CouponService coupons) {
        this.repository = repository; this.carts = carts; this.products = products; this.quotes = quotes; this.accounts = accounts; this.coupons = coupons;
    }
    @Override
    @Transactional
    public Order place(UUID cartId, Checkout checkout) {
        Cart cart = carts.lock(cartId);
        String requestHash = requestHash(checkout);
        Optional<Order> previous = repository.findByKey(cartId, checkout.idempotencyKey());
        if (previous.isPresent()) {
            requireOwnership(previous.get(), cartId);
            if (!previous.get().requestHash().equals(requestHash)) {
                throw new ShopException(409, "This checkout key was already used for a different request.");
            }
            return previous.get();
        }
        if (cart.lines().isEmpty()) { throw new ShopException(409, "Your cart is empty."); }
        UUID accountId = accounts.lockCurrent();
        List<Product> lockedProducts = products.lockProducts(cart.lines());
        Quote quote = coupons.apply(quotes.calculate(cart.lines(), lockedProducts), cart.couponCode(), true);
        if (!quote.fingerprint().equals(checkout.quoteFingerprint())) {
            throw new ShopException(409, "Your cart or its price has changed. Review your cart before ordering.");
        }
        products.reserve(cart.lines(), lockedProducts);
        coupons.redeem(cart.couponCode());
        Instant now = Instant.now();
        Order order = repository.save(new Order(UUID.randomUUID(), cartId, checkout.idempotencyKey(), requestHash,
                checkout.customer(), quote.lines(), quote.subtotal(), quote.shipping(), quote.total(),
                quote.currency(), OrderStatus.PLACED, now, now, accountId, quote.couponCode(), quote.discount()));
        carts.clear(cartId);
        LOG.atInfo().addKeyValue("orderId", order.id()).addKeyValue("status", order.status()).log("Order placed");
        return order;
    }
    @Override
    public Order findForCustomer(UUID cartId, UUID orderId) {
        Order order = repository.find(orderId);
        requireOwnership(order, cartId);
        return order;
    }
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public List<Order> list(int page) {
        if (page < 0 || page > 100000) { throw new ShopException(400, "Invalid page number."); }
        return repository.list(page);
    }
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Order findForAdmin(UUID orderId) { return repository.find(orderId); }
    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public Order transition(UUID id, OrderStatus expected, OrderStatus next, String actor) {
        Order order = repository.lock(id);
        if (order.status() == next) { return order; }
        if (order.status() != expected || !order.status().canTransitionTo(next)) {
            throw new ShopException(409, "The order state changed or this transition is not allowed. Refresh the order.");
        }
        if (next == OrderStatus.CANCELLED) {
            products.restore(order.lines().stream().map(line -> new CartLine(line.productId(), line.quantity())).toList());
        }
        Order updated = repository.transition(id, next, actor);
        LOG.atInfo().addKeyValue("orderId", id).addKeyValue("status", next).addKeyValue("actor", actor).log("Order processed");
        return updated;
    }
    @Override @PreAuthorize("hasRole('CUSTOMER')") public List<Order> listMine(int page) {
        if (page < 0 || page > 100000) throw new ShopException(400, "Invalid page number.");
        return repository.listByAccount(accounts.current().id(), page);
    }
    private void requireOwnership(Order order, UUID cartId) {
        boolean owned = order.accountId() == null ? order.cartId().equals(cartId) : order.accountId().equals(accounts.currentId());
        if (!owned) throw new ShopException(404, "Order not found.");
    }
    private String requestHash(Checkout checkout) {
        Customer c = checkout.customer();
        String values = List.of(checkout.quoteFingerprint(), c.name(), c.email(), c.phone(), c.address(),
                c.city(), c.postcode(), c.state()).stream().map(Digests::field).reduce("", String::concat);
        return Digests.sha256(values);
    }
}
