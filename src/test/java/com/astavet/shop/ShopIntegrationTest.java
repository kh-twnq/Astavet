package com.astavet.shop;

import com.astavet.shop.domain.Checkout;
import com.astavet.shop.domain.Customer;
import com.astavet.shop.domain.Order;
import com.astavet.shop.domain.OrderStatus;
import com.astavet.shop.domain.Quote;
import com.astavet.shop.domain.ShopException;
import com.astavet.shop.repository.entity.ProductEntity;
import com.astavet.shop.service.CartService;
import com.astavet.shop.service.OrderService;
import com.astavet.shop.service.ProductService;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.json.JsonMapper;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

@SpringBootTest
@AutoConfigureMockMvc
class ShopIntegrationTest {
    private static final UUID PRODUCT = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final Customer CUSTOMER = new Customer("Alex Green", "alex@example.com", "+61 400 123 456",
            "10 Sample Street", "Brisbane", "4000", "QLD");
    @Autowired CartService carts;
    @Autowired OrderService orders;
    @Autowired ProductService products;
    @Autowired EntityManager entityManager;
    @Autowired TransactionTemplate transactions;
    @Autowired MockMvc mvc;
    private final JsonMapper json = JsonMapper.builder().build();

    @BeforeEach
    void resetDatabase() {
        transactions.executeWithoutResult(transaction -> {
            entityManager.createQuery("delete from OrderEventEntity").executeUpdate();
            entityManager.createQuery("delete from OrderLineEntity").executeUpdate();
            entityManager.createQuery("delete from OrderEntity").executeUpdate();
            entityManager.createNativeQuery("delete from cart_lines").executeUpdate();
            entityManager.createQuery("delete from CartEntity").executeUpdate();
            entityManager.createQuery("delete from ProductEntity p where p.id <> :id").setParameter("id", PRODUCT).executeUpdate();
            entityManager.createQuery("update ProductEntity p set p.stock = 100, p.price = 49.00, p.active = true").executeUpdate();
        });
    }
    @Test
    void browserFlowUsesServerTotalsAndProtectsOrderOwnership() throws Exception {
        MockHttpSession session = new MockHttpSession();
        mvc.perform(get("/")).andExpect(status().isOk());
        mvc.perform(get("/api/v1/products")).andExpect(status().isOk()).andExpect(jsonPath("$[0].name").value("AstaVet 130g"));
        String bag = mvc.perform(put("/api/v1/cart/lines").session(session).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of("productId", PRODUCT, "quantity", 2))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.subtotal").value(98.0))
                .andExpect(jsonPath("$.total").value(105.95)).andReturn().getResponse().getContentAsString();
        String fingerprint = json.readTree(bag).get("fingerprint").asText();
        String payload = payload(fingerprint, UUID.randomUUID());
        String result = mvc.perform(post("/api/v1/orders").session(session).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(payload)).andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentMethod").value("COD")).andExpect(jsonPath("$.status").value("PLACED"))
                .andReturn().getResponse().getContentAsString();
        String orderId = json.readTree(result).get("id").asText();
        mvc.perform(post("/api/v1/orders").session(session).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(orderId));
        mvc.perform(get("/api/v1/cart").session(session)).andExpect(jsonPath("$.lines").isEmpty());
        mvc.perform(get("/api/v1/orders/" + orderId).session(session)).andExpect(status().isOk());
        mvc.perform(get("/api/v1/orders/" + orderId).session(new MockHttpSession())).andExpect(status().isNotFound());
        assertThat(products.find(PRODUCT).stock()).isEqualTo(98);
    }
    @Test
    void rejectsCsrfAdminAccessAndInvalidOrClientOwnedFields() throws Exception {
        mvc.perform(put("/api/v1/cart/lines").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("productId", PRODUCT, "quantity", 1))))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/admin/orders")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/admin/orders").with(user("guest").roles("USER"))).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/admin/orders?page=-1").with(user("admin").roles("ADMIN"))).andExpect(status().isBadRequest());
        mvc.perform(put("/api/v1/cart/lines").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("productId", PRODUCT, "quantity", -1))))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/orders").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"total\":0,\"paymentMethod\":\"CARD\"}")).andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/orders").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(payload("a".repeat(64), UUID.randomUUID()).replace("4000", "INVALID")))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/api/v1/cart/lines").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("productId", PRODUCT))))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/orders").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(payload("a".repeat(64), UUID.randomUUID()).replace("\"name\":", "\"total\":0,\"name\":")))
                .andExpect(status().isBadRequest());
    }
    @Test
    void unsupportedRequestMethodPreservesClientErrorAndAllowedMethods() throws Exception {
        mvc.perform(post("/api/v1/cart").with(csrf()))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(result -> assertThat(result.getResponse().getHeader("Allow")).contains("GET"))
                .andExpect(jsonPath("$.message").value("This request method is not supported."));
    }
    @Test
    void unsupportedContentTypePreservesClientErrorAndAcceptedTypes() throws Exception {
        mvc.perform(post("/api/v1/orders").with(csrf()).contentType(MediaType.TEXT_PLAIN).content("{}"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(result -> assertThat(result.getResponse().getHeader("Accept")).contains("application/json"))
                .andExpect(jsonPath("$.message").value("This content type is not supported."));
    }
    @Test
    void rejectsChangedCartAndPricesWithoutReservingStock() {
        UUID cartId = UUID.randomUUID();
        Quote quote = carts.setQuantity(cartId, PRODUCT, 1);
        transactions.executeWithoutResult(transaction -> entityManager.createQuery("update ProductEntity p set p.price = 50.00").executeUpdate());
        assertThatThrownBy(() -> orders.place(cartId, checkout(quote))).isInstanceOf(ShopException.class).hasMessageContaining("price has changed");
        assertThat(products.find(PRODUCT).stock()).isEqualTo(100);
        assertThat(carts.view(cartId).lines()).hasSize(1);
        assertThat(orderCount()).isZero();
    }
    @Test
    void sameKeyWithDifferentCustomerIsAConflict() {
        UUID cartId = UUID.randomUUID();
        Checkout request = checkout(carts.setQuantity(cartId, PRODUCT, 1));
        orders.place(cartId, request);
        Checkout changed = new Checkout(request.idempotencyKey(), request.quoteFingerprint(),
                new Customer("Someone Else", CUSTOMER.email(), CUSTOMER.phone(), CUSTOMER.address(), CUSTOMER.city(), CUSTOMER.postcode(), CUSTOMER.state()));
        assertThatThrownBy(() -> orders.place(cartId, changed)).isInstanceOf(ShopException.class).hasMessageContaining("different request");
        assertThat(orderCount()).isEqualTo(1);
        assertThat(products.find(PRODUCT).stock()).isEqualTo(99);
    }
    @Test
    void rollsBackAllInventoryWhenOneProductIsUnavailable() {
        UUID secondId = UUID.randomUUID();
        transactions.executeWithoutResult(transaction -> {
            ProductEntity second = new ProductEntity(); second.id = secondId; second.slug = "second";
            second.name = "Second product"; second.description = "Description"; second.currency = "AUD";
            second.price = new BigDecimal("15.00"); second.stock = 1; second.active = true; entityManager.persist(second);
        });
        UUID cartId = UUID.randomUUID();
        carts.setQuantity(cartId, PRODUCT, 1);
        Quote quote = carts.setQuantity(cartId, secondId, 1);
        transactions.executeWithoutResult(transaction -> entityManager.createQuery("update ProductEntity p set p.stock = 0 where p.id = :id").setParameter("id", secondId).executeUpdate());
        assertThatThrownBy(() -> orders.place(cartId, checkout(quote))).isInstanceOf(ShopException.class);
        assertThat(products.find(PRODUCT).stock()).isEqualTo(100);
        assertThat(carts.view(cartId).lines()).hasSize(2);
        assertThat(orderCount()).isZero();
    }
    @Test
    void concurrentLastUnitCheckoutsDoNotOversell() throws Exception {
        setStock(1);
        UUID firstCart = UUID.randomUUID(); UUID secondCart = UUID.randomUUID();
        Checkout first = checkout(carts.setQuantity(firstCart, PRODUCT, 1));
        Checkout second = checkout(carts.setQuantity(secondCart, PRODUCT, 1));
        List<Object> results = concurrently(List.of(() -> orders.place(firstCart, first), () -> orders.place(secondCart, second)));
        assertThat(results.stream().filter(Order.class::isInstance).count()).isEqualTo(1);
        assertThat(results.stream().filter(ShopException.class::isInstance).count()).isEqualTo(1);
        assertThat(orderCount()).isEqualTo(1);
        assertThat(products.find(PRODUCT).stock()).isZero();
    }
    @Test
    void concurrentSameKeyReturnsOneOrderAndReservesOnce() throws Exception {
        UUID cartId = UUID.randomUUID();
        Checkout checkout = checkout(carts.setQuantity(cartId, PRODUCT, 1));
        List<Object> results = concurrently(List.of(() -> orders.place(cartId, checkout), () -> orders.place(cartId, checkout)));
        assertThat(results).allMatch(Order.class::isInstance);
        assertThat(((Order) results.get(0)).id()).isEqualTo(((Order) results.get(1)).id());
        assertThat(products.find(PRODUCT).stock()).isEqualTo(99);
        assertThat(orderCount()).isEqualTo(1);
    }
    @Test
    void concurrentFirstCartCreationAndUpdatesDoNotLoseDistinctLines() throws Exception {
        UUID secondId = UUID.randomUUID();
        transactions.executeWithoutResult(transaction -> {
            ProductEntity second = new ProductEntity(); second.id = secondId; second.slug = "second";
            second.name = "Second"; second.description = "Description"; second.price = new BigDecimal("10.00");
            second.currency = "AUD"; second.stock = 10; second.active = true; entityManager.persist(second);
        });
        UUID cartId = UUID.randomUUID();
        List<Object> results = concurrently(List.of(() -> carts.setQuantity(cartId, PRODUCT, 1), () -> carts.setQuantity(cartId, secondId, 1)));
        assertThat(results).allMatch(Quote.class::isInstance);
        assertThat(carts.view(cartId).lines()).hasSize(2);
    }
    @Test
    @WithMockUser(roles = "ADMIN")
    void adminTransitionsAndCancellationRestoreExactlyOnce() {
        UUID cartId = UUID.randomUUID();
        Order order = orders.place(cartId, checkout(carts.setQuantity(cartId, PRODUCT, 2)));
        orders.transition(order.id(), OrderStatus.PLACED, OrderStatus.CONFIRMED, "admin");
        assertThatThrownBy(() -> orders.transition(order.id(), OrderStatus.PLACED, OrderStatus.CANCELLED, "admin"))
                .isInstanceOf(ShopException.class).hasMessageContaining("state changed");
        orders.transition(order.id(), OrderStatus.CONFIRMED, OrderStatus.CANCELLED, "admin");
        orders.transition(order.id(), OrderStatus.CONFIRMED, OrderStatus.CANCELLED, "admin");
        assertThat(products.find(PRODUCT).stock()).isEqualTo(100);
        assertThat(orders.findForAdmin(order.id()).status()).isEqualTo(OrderStatus.CANCELLED);
        assertThatThrownBy(() -> orders.transition(order.id(), OrderStatus.CANCELLED, OrderStatus.CONFIRMED, "admin"))
                .isInstanceOf(ShopException.class);
        assertThat(eventCount()).isEqualTo(3);
    }
    @Test
    void concurrentCancellationsRestoreOnlyOnce() throws Exception {
        UUID cartId = UUID.randomUUID();
        Order order = orders.place(cartId, checkout(carts.setQuantity(cartId, PRODUCT, 2)));
        Callable<Object> cancel = () -> {
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                    "admin", "unused", List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
            try { return orders.transition(order.id(), OrderStatus.PLACED, OrderStatus.CANCELLED, "admin"); }
            finally { SecurityContextHolder.clearContext(); }
        };
        assertThat(concurrently(List.of(cancel, cancel))).allMatch(Order.class::isInstance);
        assertThat(products.find(PRODUCT).stock()).isEqualTo(100);
        assertThat(eventCount()).isEqualTo(2);
    }
    @Test
    @WithMockUser(roles = "ADMIN")
    void shippedOrdersCannotBeCancelledAndDeliveredIsTerminal() throws Exception {
        UUID cartId = UUID.randomUUID();
        Order order = orders.place(cartId, checkout(carts.setQuantity(cartId, PRODUCT, 1)));
        mvc.perform(put("/api/v1/admin/orders/" + order.id() + "/status").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"expectedStatus\":\"PLACED\",\"status\":\"CONFIRMED\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CONFIRMED"));
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "admin", "unused", List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
        orders.transition(order.id(), OrderStatus.CONFIRMED, OrderStatus.SHIPPED, "admin");
        assertThatThrownBy(() -> orders.transition(order.id(), OrderStatus.SHIPPED, OrderStatus.CANCELLED, "admin"))
                .isInstanceOf(ShopException.class);
        orders.transition(order.id(), OrderStatus.SHIPPED, OrderStatus.DELIVERED, "admin");
        assertThatThrownBy(() -> orders.transition(order.id(), OrderStatus.DELIVERED, OrderStatus.PLACED, "admin"))
                .isInstanceOf(ShopException.class);
        assertThat(products.find(PRODUCT).stock()).isEqualTo(99);
    }
    @Test
    void adminServiceRejectsUnauthenticatedCalls() {
        assertThatThrownBy(() -> orders.list(0)).isInstanceOf(org.springframework.security.core.AuthenticationException.class);
    }
    @Test
    void adminLoginAndLogoutUseCsrfProtectedSession() throws Exception {
        MockHttpSession session = new MockHttpSession();
        mvc.perform(get("/login.html")).andExpect(status().isOk());
        mvc.perform(post("/login").session(session).with(csrf())
                .param("username", "admin").param("password", "test-only-password"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/admin.html"));
        mvc.perform(get("/api/v1/admin/orders").session(session)).andExpect(status().isOk());
        mvc.perform(post("/logout").session(session)).andExpect(status().isForbidden());
        mvc.perform(post("/logout").session(session).with(csrf())).andExpect(status().is3xxRedirection());
        mvc.perform(get("/api/v1/admin/orders").session(new MockHttpSession())).andExpect(status().isUnauthorized());
    }
    @Test
    void replayKeepsHistoricalPriceAndDoesNotClearNewCartItems() {
        UUID cartId = UUID.randomUUID();
        Checkout request = checkout(carts.setQuantity(cartId, PRODUCT, 1));
        Order original = orders.place(cartId, request);
        transactions.executeWithoutResult(transaction -> entityManager.createQuery("update ProductEntity p set p.price = 60.00").executeUpdate());
        carts.setQuantity(cartId, PRODUCT, 2);
        Order replay = orders.place(cartId, request);
        assertThat(replay.id()).isEqualTo(original.id());
        assertThat(replay.total()).isEqualByComparingTo("56.95");
        assertThat(carts.view(cartId).lines().getFirst().quantity()).isEqualTo(2);
        assertThat(products.find(PRODUCT).stock()).isEqualTo(99);
    }
    private Checkout checkout(Quote quote) { return new Checkout(UUID.randomUUID(), quote.fingerprint(), CUSTOMER); }
    private void setStock(int stock) {
        transactions.executeWithoutResult(transaction -> entityManager.createQuery("update ProductEntity p set p.stock = :stock").setParameter("stock", stock).executeUpdate());
    }
    private long orderCount() {
        return transactions.execute(transaction -> entityManager.createQuery("select count(o) from OrderEntity o", Long.class).getSingleResult());
    }
    private long eventCount() {
        return transactions.execute(transaction -> entityManager.createQuery("select count(o) from OrderEventEntity o", Long.class).getSingleResult());
    }
    private String payload(String fingerprint, UUID key) {
        return json.writeValueAsString(Map.of("idempotencyKey", key, "quoteFingerprint", fingerprint,
                "name", CUSTOMER.name(), "email", CUSTOMER.email(), "phone", CUSTOMER.phone(), "address", CUSTOMER.address(),
                "city", CUSTOMER.city(), "postcode", CUSTOMER.postcode(), "state", CUSTOMER.state()));
    }
    private List<Object> concurrently(List<Callable<Object>> actions) throws Exception {
        CountDownLatch start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(actions.size())) {
            var futures = actions.stream().map(action -> executor.submit(() -> {
                if (!start.await(5, TimeUnit.SECONDS)) throw new IllegalStateException("Concurrent start timed out");
                try { return action.call(); } catch (ShopException exception) { return exception; }
            })).toList();
            start.countDown();
            List<Object> results = new ArrayList<>();
            for (var future : futures) results.add(future.get(20, TimeUnit.SECONDS));
            return results;
        }
    }
}
