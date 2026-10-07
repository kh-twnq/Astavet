package com.astavet.shop.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.astavet.shop.domain.Checkout;
import com.astavet.shop.domain.Customer;
import com.astavet.shop.domain.Order;
import com.astavet.shop.domain.OrderStatus;
import com.astavet.shop.domain.Quote;
import com.astavet.shop.entity.ProductEntity;
import com.astavet.shop.exception.ShopException;
import com.astavet.shop.service.CartService;
import com.astavet.shop.service.OrderService;
import com.astavet.shop.service.ProductService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@AutoConfigureMockMvc
class ShopIntegrationTest {
  private static final UUID PRODUCT = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final Customer CUSTOMER =
      new Customer(
          "Alex Green",
          "alex@example.com",
          "+61 400 123 456",
          "10 Sample Street",
          "Brisbane",
          "4000",
          "QLD");
  @Autowired CartService carts;
  @Autowired OrderService orders;
  @Autowired ProductService products;
  @Autowired EntityManager entityManager;
  @Autowired EntityManagerFactory entityManagerFactory;
  @Autowired JdbcTemplate jdbc;
  @Autowired TransactionTemplate transactions;
  @Autowired MockMvc mvc;
  @Autowired com.astavet.shop.service.AccountService accounts;
  @Autowired com.astavet.shop.service.CatalogueAdminService catalogue;
  @Autowired com.astavet.shop.service.CouponService coupons;
  @Autowired com.astavet.shop.service.CommunityService community;
  private final JsonMapper json = JsonMapper.builder().build();

  @BeforeEach
  void resetDatabase() {
    transactions.executeWithoutResult(
        transaction -> {
          entityManager.createQuery("delete from ReviewEntity").executeUpdate();
          entityManager.createQuery("delete from WishlistEntity").executeUpdate();
          entityManager.createQuery("delete from StockAdjustmentEntity").executeUpdate();
          entityManager.createQuery("delete from OrderEventEntity").executeUpdate();
          entityManager.createQuery("delete from OrderLineEntity").executeUpdate();
          entityManager.createQuery("delete from OrderEntity").executeUpdate();
          entityManager.createNativeQuery("delete from cart_lines").executeUpdate();
          entityManager.createQuery("delete from CartEntity").executeUpdate();
          entityManager.createQuery("delete from CouponEntity").executeUpdate();
          entityManager.createQuery("delete from AccountEntity").executeUpdate();
          entityManager
              .createQuery(
                  "update ProductEntity p set p.name = 'AstaVet 130g', p.slug = 'astaxanthin-200g',"
                      + " p.imagePath = '/assets/astavet-130g.png' where p.id = :id")
              .setParameter("id", PRODUCT)
              .executeUpdate();
          entityManager
              .createQuery("delete from ProductEntity p where p.id <> :id")
              .setParameter("id", PRODUCT)
              .executeUpdate();
          entityManager
              .createQuery(
                  "update ProductEntity p set p.stock = 100, p.price = 49.00, p.active = true")
              .executeUpdate();
        });
  }

  @Test
  void browserFlowUsesServerTotalsAndProtectsOrderOwnership() throws Exception {
    MockHttpSession session = new MockHttpSession();
    mvc.perform(get("/api/v1/products"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].name").value("AstaVet 130g"));
    String bag =
        mvc.perform(
                put("/api/v1/cart/lines")
                    .session(session)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json.writeValueAsString(Map.of("productId", PRODUCT, "quantity", 2))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.subtotal").value(98.0))
            .andExpect(jsonPath("$.total").value(105.95))
            .andReturn()
            .getResponse()
            .getContentAsString();
    String fingerprint = json.readTree(bag).get("fingerprint").asText();
    String payload = payload(fingerprint, UUID.randomUUID());
    String result =
        mvc.perform(
                post("/api/v1/orders")
                    .session(session)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payload))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.paymentMethod").value("COD"))
            .andExpect(jsonPath("$.status").value("PLACED"))
            .andReturn()
            .getResponse()
            .getContentAsString();
    String orderId = json.readTree(result).get("id").asText();
    mvc.perform(
            post("/api/v1/orders")
                .session(session)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(orderId));
    mvc.perform(get("/api/v1/cart").session(session)).andExpect(jsonPath("$.lines").isEmpty());
    mvc.perform(get("/api/v1/orders/" + orderId).session(session)).andExpect(status().isOk());
    mvc.perform(get("/api/v1/orders/" + orderId).session(new MockHttpSession()))
        .andExpect(status().isNotFound());
    assertThat(products.find(PRODUCT).stock()).isEqualTo(98);
  }

  @Test
  void rejectsCsrfAdminAccessAndInvalidOrClientOwnedFields() throws Exception {
    mvc.perform(
            put("/api/v1/cart/lines")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("productId", PRODUCT, "quantity", 1))))
        .andExpect(status().isForbidden());
    mvc.perform(get("/api/v1/admin/orders")).andExpect(status().isUnauthorized());
    mvc.perform(get("/api/v1/admin/orders").with(user("guest").roles("USER")))
        .andExpect(status().isForbidden());
    mvc.perform(get("/api/v1/admin/orders?page=-1").with(user("admin").roles("ADMIN")))
        .andExpect(status().isBadRequest());
    mvc.perform(
            put("/api/v1/cart/lines")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("productId", PRODUCT, "quantity", -1))))
        .andExpect(status().isBadRequest());
    mvc.perform(
            post("/api/v1/orders")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"total\":0,\"paymentMethod\":\"CARD\"}"))
        .andExpect(status().isBadRequest());
    mvc.perform(
            post("/api/v1/orders")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload("a".repeat(64), UUID.randomUUID()).replace("4000", "INVALID")))
        .andExpect(status().isBadRequest());
    mvc.perform(
            put("/api/v1/cart/lines")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("productId", PRODUCT))))
        .andExpect(status().isBadRequest());
    mvc.perform(
            post("/api/v1/orders")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    payload("a".repeat(64), UUID.randomUUID())
                        .replace("\"name\":", "\"total\":0,\"name\":")))
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
        .andExpect(
            result ->
                assertThat(result.getResponse().getHeader("Accept")).contains("application/json"))
        .andExpect(jsonPath("$.message").value("This content type is not supported."));
  }

  @Test
  void rejectsChangedCartAndPricesWithoutReservingStock() {
    UUID cartId = UUID.randomUUID();
    Quote quote = carts.setQuantity(cartId, PRODUCT, 1);
    transactions.executeWithoutResult(
        transaction ->
            entityManager
                .createQuery("update ProductEntity p set p.price = 50.00")
                .executeUpdate());
    assertThatThrownBy(() -> orders.place(cartId, checkout(quote)))
        .isInstanceOf(ShopException.class)
        .hasMessageContaining("price has changed");
    assertThat(products.find(PRODUCT).stock()).isEqualTo(100);
    assertThat(carts.view(cartId).lines()).hasSize(1);
    assertThat(orderCount()).isZero();
  }

  @Test
  void sameKeyWithDifferentCustomerIsAConflict() {
    UUID cartId = UUID.randomUUID();
    Checkout request = checkout(carts.setQuantity(cartId, PRODUCT, 1));
    orders.place(cartId, request);
    Checkout changed =
        new Checkout(
            request.idempotencyKey(),
            request.quoteFingerprint(),
            new Customer(
                "Someone Else",
                CUSTOMER.email(),
                CUSTOMER.phone(),
                CUSTOMER.address(),
                CUSTOMER.city(),
                CUSTOMER.postcode(),
                CUSTOMER.state()));
    assertThatThrownBy(() -> orders.place(cartId, changed))
        .isInstanceOf(ShopException.class)
        .hasMessageContaining("different request");
    assertThat(orderCount()).isEqualTo(1);
    assertThat(products.find(PRODUCT).stock()).isEqualTo(99);
  }

  @Test
  void rollsBackAllInventoryWhenOneProductIsUnavailable() {
    UUID secondId = UUID.randomUUID();
    transactions.executeWithoutResult(
        transaction -> {
          ProductEntity second = new ProductEntity();
          second.id = secondId;
          second.slug = "second";
          second.name = "Second product";
          second.description = "Description";
          second.currency = "AUD";
          second.price = new BigDecimal("15.00");
          second.stock = 1;
          second.active = true;
          entityManager.persist(second);
        });
    UUID cartId = UUID.randomUUID();
    carts.setQuantity(cartId, PRODUCT, 1);
    Quote quote = carts.setQuantity(cartId, secondId, 1);
    transactions.executeWithoutResult(
        transaction ->
            entityManager
                .createQuery("update ProductEntity p set p.stock = 0 where p.id = :id")
                .setParameter("id", secondId)
                .executeUpdate());
    assertThatThrownBy(() -> orders.place(cartId, checkout(quote)))
        .isInstanceOf(ShopException.class);
    assertThat(products.find(PRODUCT).stock()).isEqualTo(100);
    assertThat(carts.view(cartId).lines()).hasSize(2);
    assertThat(orderCount()).isZero();
  }

  @Test
  void concurrentLastUnitCheckoutsDoNotOversell() throws Exception {
    setStock(1);
    UUID firstCart = UUID.randomUUID();
    UUID secondCart = UUID.randomUUID();
    Checkout first = checkout(carts.setQuantity(firstCart, PRODUCT, 1));
    Checkout second = checkout(carts.setQuantity(secondCart, PRODUCT, 1));
    List<Object> results =
        concurrently(
            List.of(() -> orders.place(firstCart, first), () -> orders.place(secondCart, second)));
    assertThat(results.stream().filter(Order.class::isInstance).count()).isEqualTo(1);
    assertThat(results.stream().filter(ShopException.class::isInstance).count()).isEqualTo(1);
    assertThat(orderCount()).isEqualTo(1);
    assertThat(products.find(PRODUCT).stock()).isZero();
  }

  @Test
  void concurrentSameKeyReturnsOneOrderAndReservesOnce() throws Exception {
    UUID cartId = UUID.randomUUID();
    Checkout checkout = checkout(carts.setQuantity(cartId, PRODUCT, 1));
    List<Object> results =
        concurrently(
            List.of(() -> orders.place(cartId, checkout), () -> orders.place(cartId, checkout)));
    assertThat(results).allMatch(Order.class::isInstance);
    assertThat(((Order) results.get(0)).id()).isEqualTo(((Order) results.get(1)).id());
    assertThat(products.find(PRODUCT).stock()).isEqualTo(99);
    assertThat(orderCount()).isEqualTo(1);
  }

  @Test
  void concurrentFirstCartCreationAndUpdatesDoNotLoseDistinctLines() throws Exception {
    UUID secondId = UUID.randomUUID();
    transactions.executeWithoutResult(
        transaction -> {
          ProductEntity second = new ProductEntity();
          second.id = secondId;
          second.slug = "second";
          second.name = "Second";
          second.description = "Description";
          second.price = new BigDecimal("10.00");
          second.currency = "AUD";
          second.stock = 10;
          second.active = true;
          entityManager.persist(second);
        });
    UUID cartId = UUID.randomUUID();
    List<Object> results =
        concurrently(
            List.of(
                () -> carts.setQuantity(cartId, PRODUCT, 1),
                () -> carts.setQuantity(cartId, secondId, 1)));
    assertThat(results).allMatch(Quote.class::isInstance);
    assertThat(carts.view(cartId).lines()).hasSize(2);
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void adminTransitionsAndCancellationRestoreExactlyOnce() {
    UUID cartId = UUID.randomUUID();
    Order order = orders.place(cartId, checkout(carts.setQuantity(cartId, PRODUCT, 2)));
    orders.transition(order.id(), OrderStatus.PLACED, OrderStatus.CONFIRMED, "admin");
    assertThatThrownBy(
            () -> orders.transition(order.id(), OrderStatus.PLACED, OrderStatus.CANCELLED, "admin"))
        .isInstanceOf(ShopException.class)
        .hasMessageContaining("state changed");
    orders.transition(order.id(), OrderStatus.CONFIRMED, OrderStatus.CANCELLED, "admin");
    orders.transition(order.id(), OrderStatus.CONFIRMED, OrderStatus.CANCELLED, "admin");
    assertThat(products.find(PRODUCT).stock()).isEqualTo(100);
    assertThat(orders.findForAdmin(order.id()).status()).isEqualTo(OrderStatus.CANCELLED);
    assertThatThrownBy(
            () ->
                orders.transition(
                    order.id(), OrderStatus.CANCELLED, OrderStatus.CONFIRMED, "admin"))
        .isInstanceOf(ShopException.class);
    assertThat(eventCount()).isEqualTo(3);
  }

  @Test
  void concurrentCancellationsRestoreOnlyOnce() throws Exception {
    UUID cartId = UUID.randomUUID();
    Order order = orders.place(cartId, checkout(carts.setQuantity(cartId, PRODUCT, 2)));
    Callable<Object> cancel =
        () -> {
          SecurityContextHolder.getContext()
              .setAuthentication(
                  new UsernamePasswordAuthenticationToken(
                      "admin", "unused", List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
          try {
            return orders.transition(
                order.id(), OrderStatus.PLACED, OrderStatus.CANCELLED, "admin");
          } finally {
            SecurityContextHolder.clearContext();
          }
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
    mvc.perform(
            put("/api/v1/admin/orders/" + order.id() + "/status")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"expectedStatus\":\"PLACED\",\"status\":\"CONFIRMED\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("CONFIRMED"));
    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken(
                "admin", "unused", List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
    orders.transition(order.id(), OrderStatus.CONFIRMED, OrderStatus.SHIPPED, "admin");
    assertThatThrownBy(
            () ->
                orders.transition(order.id(), OrderStatus.SHIPPED, OrderStatus.CANCELLED, "admin"))
        .isInstanceOf(ShopException.class);
    orders.transition(order.id(), OrderStatus.SHIPPED, OrderStatus.DELIVERED, "admin");
    assertThatThrownBy(
            () -> orders.transition(order.id(), OrderStatus.DELIVERED, OrderStatus.PLACED, "admin"))
        .isInstanceOf(ShopException.class);
    assertThat(products.find(PRODUCT).stock()).isEqualTo(99);
  }

  @Test
  void adminServiceRejectsUnauthenticatedCalls() {
    assertThatThrownBy(() -> orders.list(0))
        .isInstanceOf(org.springframework.security.core.AuthenticationException.class);
  }

  @Test
  void adminLoginAndLogoutUseCsrfProtectedSession() throws Exception {
    MockHttpSession session = new MockHttpSession();
    mvc.perform(get("/api/v1/csrf")).andExpect(status().isOk());
    mvc.perform(
            post("/login")
                .session(session)
                .with(csrf())
                .param("username", "admin")
                .param("password", "test-only-password"))
        .andExpect(status().isNoContent());
    mvc.perform(get("/api/v1/admin/orders").session(session)).andExpect(status().isOk());
    mvc.perform(post("/logout").session(session)).andExpect(status().isForbidden());
    mvc.perform(post("/logout").session(session).with(csrf())).andExpect(status().isNoContent());
    mvc.perform(get("/api/v1/admin/orders").session(new MockHttpSession()))
        .andExpect(status().isUnauthorized());
    mvc.perform(
            post("/login").with(csrf()).param("username", "admin").param("password", "incorrect"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void replayKeepsHistoricalPriceAndDoesNotClearNewCartItems() {
    UUID cartId = UUID.randomUUID();
    Checkout request = checkout(carts.setQuantity(cartId, PRODUCT, 1));
    Order original = orders.place(cartId, request);
    transactions.executeWithoutResult(
        transaction ->
            entityManager
                .createQuery("update ProductEntity p set p.price = 60.00")
                .executeUpdate());
    carts.setQuantity(cartId, PRODUCT, 2);
    Order replay = orders.place(cartId, request);
    assertThat(replay.id()).isEqualTo(original.id());
    assertThat(replay.total()).isEqualByComparingTo("56.95");
    assertThat(carts.view(cartId).lines().getFirst().quantity()).isEqualTo(2);
    assertThat(products.find(PRODUCT).stock()).isEqualTo(99);
  }

  @Test
  void registrationHashesPasswordsRejectsRoleInjectionAndDuplicateEmails() throws Exception {
    var account = accounts.register(" Customer@Example.test ", "Customer", "long-test-password");
    assertThat(account.email()).isEqualTo("customer@example.test");
    assertThat(account.passwordHash()).startsWith("{bcrypt}").doesNotContain("long-test-password");
    assertThatThrownBy(
            () -> accounts.register("CUSTOMER@example.test", "Other", "long-test-password"))
        .isInstanceOf(ShopException.class);
    mvc.perform(
            post("/api/v1/accounts")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    json.writeValueAsString(
                        Map.of(
                            "email",
                            "other@example.test",
                            "name",
                            "Other",
                            "password",
                            "long-test-password",
                            "role",
                            "ADMIN"))))
        .andExpect(status().isBadRequest());
    mvc.perform(
            post("/api/v1/accounts")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    json.writeValueAsString(
                        Map.of(
                            "email",
                            "other@example.test",
                            "name",
                            "Other",
                            "password",
                            "😀".repeat(20)))))
        .andExpect(status().isBadRequest());
    mvc.perform(get("/api/v1/account/orders")).andExpect(status().isUnauthorized());
    mvc.perform(post("/api/v1/accounts").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isForbidden());
  }

  @Test
  void accountOrdersSurviveNewSessionsAndRejectDifferentAccountReadsAndReplay() throws Exception {
    accounts.register("a@example.test", "Account A", "long-test-password");
    accounts.register("b@example.test", "Account B", "long-test-password");
    MockHttpSession session = new MockHttpSession();
    mvc.perform(
            post("/login")
                .session(session)
                .with(csrf())
                .param("username", "a@example.test")
                .param("password", "long-test-password"))
        .andExpect(status().isNoContent());
    mvc.perform(get("/api/v1/session").session(session))
        .andExpect(jsonPath("$.role").value("CUSTOMER"))
        .andExpect(jsonPath("$.account.passwordHash").doesNotExist());
    String cart =
        mvc.perform(
                put("/api/v1/cart/lines")
                    .session(session)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json.writeValueAsString(Map.of("productId", PRODUCT, "quantity", 1))))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    String body = payload(json.readTree(cart).get("fingerprint").asText(), UUID.randomUUID());
    String placed =
        mvc.perform(
                post("/api/v1/orders")
                    .session(session)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    String id = json.readTree(placed).get("id").asText();
    mvc.perform(
            get("/api/v1/orders/" + id)
                .with(user("a@example.test").roles("CUSTOMER"))
                .session(new MockHttpSession()))
        .andExpect(status().isOk());
    mvc.perform(get("/api/v1/account/orders").with(user("a@example.test").roles("CUSTOMER")))
        .andExpect(jsonPath("$[0].id").value(id));
    mvc.perform(
            post("/login")
                .session(session)
                .with(csrf())
                .param("username", "b@example.test")
                .param("password", "long-test-password"))
        .andExpect(status().isNoContent());
    mvc.perform(get("/api/v1/orders/" + id).session(session)).andExpect(status().isNotFound());
    mvc.perform(
            post("/api/v1/orders")
                .session(session)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isNotFound());
    mvc.perform(get("/api/v1/account/orders").session(session)).andExpect(jsonPath("$").isEmpty());
    assertThat(orderCount()).isEqualTo(1);
    assertThat(products.find(PRODUCT).stock()).isEqualTo(99);
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void productAdminUsesVersionsSoftArchivalAndServerStock() throws Exception {
    String create =
        json.writeValueAsString(
            Map.of(
                "slug",
                "new-product",
                "name",
                "New product",
                "description",
                "New description",
                "price",
                12.5,
                "active",
                true,
                "imagePath",
                "/assets/product-placeholder.svg",
                "expectedVersion",
                0));
    String result =
        mvc.perform(
                post("/api/v1/admin/products")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(create))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.stock").value(0))
            .andReturn()
            .getResponse()
            .getContentAsString();
    UUID id = UUID.fromString(json.readTree(result).get("id").asText());
    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken(
                "admin", "unused", List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
    var firstProduct = products.find(id);
    catalogue.adjust(id, UUID.randomUUID(), 5, "Receive shipment", firstProduct.version(), "admin");
    assertThatThrownBy(
            () ->
                catalogue.save(
                    id,
                    firstProduct.slug(),
                    firstProduct.name(),
                    firstProduct.description(),
                    firstProduct.price(),
                    false,
                    firstProduct.imagePath(),
                    firstProduct.version()))
        .isInstanceOf(ShopException.class)
        .hasMessageContaining("changed");
    var product = products.find(id);
    var hidden =
        catalogue.save(
            id,
            product.slug(),
            product.name(),
            product.description(),
            product.price(),
            false,
            product.imagePath(),
            product.version());
    assertThat(hidden.active()).isFalse();
    assertThat(products.list()).noneMatch(p -> p.id().equals(id));
    assertThatThrownBy(() -> carts.setQuantity(UUID.randomUUID(), id, 1))
        .isInstanceOf(ShopException.class);
    mvc.perform(
            post("/api/v1/admin/products")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    create.replace(
                        "/assets/product-placeholder.svg", "https://bad.example/image.svg")))
        .andExpect(status().isBadRequest());
  }

  @Test
  void customerCannotManageProductsCouponsStockOrModeration() throws Exception {
    accounts.register("customer@example.test", "Customer", "long-test-password");
    for (String path :
        List.of("/api/v1/admin/products", "/api/v1/admin/coupons", "/api/v1/admin/reviews")) {
      mvc.perform(get(path).with(user("customer@example.test").roles("CUSTOMER")))
          .andExpect(status().isForbidden());
    }
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void stockAdjustmentReplayAndStaleUpdatesCannotDoubleCount() {
    long version = products.find(PRODUCT).version();
    UUID operation = UUID.randomUUID();
    var first = catalogue.adjust(PRODUCT, operation, 5, "Receive", version, "admin");
    assertThat(catalogue.adjust(PRODUCT, operation, 5, "Receive", version, "admin"))
        .isEqualTo(first);
    assertThat(products.find(PRODUCT).stock()).isEqualTo(105);
    assertThat(catalogue.adjustments(PRODUCT)).hasSize(1);
    assertThatThrownBy(() -> catalogue.adjust(PRODUCT, operation, 6, "Receive", version, "admin"))
        .isInstanceOf(ShopException.class);
    assertThatThrownBy(
            () -> catalogue.adjust(PRODUCT, UUID.randomUUID(), -1, "Correction", version, "admin"))
        .isInstanceOf(ShopException.class);
    long current = products.find(PRODUCT).version();
    assertThatThrownBy(
            () ->
                catalogue.adjust(PRODUCT, UUID.randomUUID(), -106, "Correction", current, "admin"))
        .isInstanceOf(ShopException.class);
  }

  @Test
  void concurrentStockAdjustmentRetriesApplyOnlyOnce() throws Exception {
    long version = products.find(PRODUCT).version();
    UUID key = UUID.randomUUID();
    Callable<Object> adjustment =
        () ->
            as(
                "admin",
                "ADMIN",
                () -> catalogue.adjust(PRODUCT, key, 5, "Receive", version, "admin"));
    assertThat(concurrently(List.of(adjustment, adjustment)))
        .allMatch(com.astavet.shop.domain.StockAdjustment.class::isInstance);
    assertThat(products.find(PRODUCT).stock()).isEqualTo(105);
    assertThat(as("admin", "ADMIN", () -> catalogue.adjustments(PRODUCT))).hasSize(1);
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void couponsDiscountServerTotalsAndReplayDoesNotRedeemTwice() {
    coupons.save(
        null,
        "SAVE10",
        new BigDecimal("10.00"),
        BigDecimal.ZERO,
        java.time.Instant.now().plusSeconds(3600),
        1,
        true,
        0);
    UUID cartId = UUID.randomUUID();
    carts.setQuantity(cartId, PRODUCT, 1);
    Quote quote = carts.setCoupon(cartId, "save10");
    assertThat(quote.discount()).isEqualByComparingTo("10.00");
    assertThat(quote.total()).isEqualByComparingTo("46.95");
    Checkout request = checkout(quote);
    Order order = orders.place(cartId, request);
    assertThat(order.discount()).isEqualByComparingTo("10.00");
    assertThat(order.total()).isEqualByComparingTo("46.95");
    assertThat(orders.place(cartId, request).id()).isEqualTo(order.id());
    assertThat(coupons.list(0).getFirst().uses()).isEqualTo(1);
    assertThat(carts.view(cartId).couponCode()).isNull();
    orders.transition(order.id(), OrderStatus.PLACED, OrderStatus.CANCELLED, "admin");
    assertThat(coupons.list(0).getFirst().uses()).isEqualTo(1);
    assertThat(products.find(PRODUCT).stock()).isEqualTo(100);
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void changedCouponRequiresFreshQuoteAndInvalidCouponCanBeRemoved() {
    var coupon =
        coupons.save(
            null,
            "SAVE10",
            new BigDecimal("10.00"),
            BigDecimal.ZERO,
            java.time.Instant.now().plusSeconds(3600),
            10,
            true,
            0);
    UUID cartId = UUID.randomUUID();
    carts.setQuantity(cartId, PRODUCT, 1);
    Quote quote = carts.setCoupon(cartId, coupon.code());
    coupons.save(
        coupon.id(),
        coupon.code(),
        new BigDecimal("11.00"),
        BigDecimal.ZERO,
        coupon.expiresAt(),
        10,
        true,
        coupon.version());
    assertThatThrownBy(() -> orders.place(cartId, checkout(quote)))
        .isInstanceOf(ShopException.class)
        .hasMessageContaining("changed");
    assertThat(products.find(PRODUCT).stock()).isEqualTo(100);
    coupon = coupons.list(0).getFirst();
    coupons.save(
        coupon.id(),
        coupon.code(),
        coupon.amount(),
        BigDecimal.ZERO,
        coupon.expiresAt(),
        10,
        false,
        coupon.version());
    assertThat(carts.view(cartId).couponMessage()).isNotBlank();
    assertThatThrownBy(() -> orders.place(cartId, checkout(carts.view(cartId))))
        .isInstanceOf(ShopException.class);
    assertThat(carts.setCoupon(cartId, "").couponCode()).isNull();
    assertThat(carts.view(cartId).total()).isEqualByComparingTo("56.95");
  }

  @Test
  void concurrentOrdersCompeteForLastCouponUse() throws Exception {
    as(
        "admin",
        "ADMIN",
        () ->
            coupons.save(
                null,
                "LAST",
                new BigDecimal("10.00"),
                BigDecimal.ZERO,
                java.time.Instant.now().plusSeconds(3600),
                1,
                true,
                0));
    UUID a = UUID.randomUUID();
    UUID b = UUID.randomUUID();
    carts.setQuantity(a, PRODUCT, 1);
    carts.setQuantity(b, PRODUCT, 1);
    Checkout first = checkout(carts.setCoupon(a, "LAST"));
    Checkout second = checkout(carts.setCoupon(b, "LAST"));
    List<Object> result =
        concurrently(List.of(() -> orders.place(a, first), () -> orders.place(b, second)));
    assertThat(result.stream().filter(Order.class::isInstance)).hasSize(1);
    assertThat(result.stream().filter(ShopException.class::isInstance)).hasSize(1);
    assertThat(products.find(PRODUCT).stock()).isEqualTo(99);
    assertThat(as("admin", "ADMIN", () -> coupons.list(0).getFirst().uses())).isEqualTo(1);
  }

  @Test
  void wishlistIsPersistentAccountScopedAndIdempotent() {
    accounts.register("a@example.test", "A", "long-test-password");
    accounts.register("b@example.test", "B", "long-test-password");
    assertThat(as("a@example.test", "CUSTOMER", () -> community.setWishlist(PRODUCT, true)))
        .hasSize(1);
    assertThat(as("a@example.test", "CUSTOMER", () -> community.setWishlist(PRODUCT, true)))
        .hasSize(1);
    assertThat(as("b@example.test", "CUSTOMER", () -> community.wishlist())).isEmpty();
    assertThat(as("a@example.test", "CUSTOMER", () -> community.wishlist())).hasSize(1);
    assertThat(as("a@example.test", "CUSTOMER", () -> community.setWishlist(PRODUCT, false)))
        .isEmpty();
  }

  @Test
  void reviewsRequireDeliveredAccountPurchaseAndFreshModerationRevision() {
    accounts.register("a@example.test", "Account A", "long-test-password");
    assertThatThrownBy(
            () ->
                as(
                    "a@example.test",
                    "CUSTOMER",
                    () -> community.submit(PRODUCT, 5, "A valid review body")))
        .isInstanceOf(ShopException.class);
    UUID cart = UUID.randomUUID();
    Quote quote = carts.setQuantity(cart, PRODUCT, 1);
    Order order = as("a@example.test", "CUSTOMER", () -> orders.place(cart, checkout(quote)));
    as(
        "admin",
        "ADMIN",
        () -> orders.transition(order.id(), OrderStatus.PLACED, OrderStatus.CONFIRMED, "admin"));
    as(
        "admin",
        "ADMIN",
        () -> orders.transition(order.id(), OrderStatus.CONFIRMED, OrderStatus.SHIPPED, "admin"));
    as(
        "admin",
        "ADMIN",
        () -> orders.transition(order.id(), OrderStatus.SHIPPED, OrderStatus.DELIVERED, "admin"));
    var original =
        as(
            "a@example.test",
            "CUSTOMER",
            () -> community.submit(PRODUCT, 5, "Original review body"));
    assertThat(community.publicReviews(PRODUCT, 0)).isEmpty();
    var edited =
        as("a@example.test", "CUSTOMER", () -> community.submit(PRODUCT, 4, "New review content"));
    assertThatThrownBy(
            () ->
                as(
                    "admin",
                    "ADMIN",
                    () ->
                        community.moderate(
                            original.id(),
                            original.status(),
                            com.astavet.shop.domain.ReviewStatus.APPROVED,
                            original.version())))
        .isInstanceOf(ShopException.class)
        .hasMessageContaining("changed");
    var approved =
        as(
            "admin",
            "ADMIN",
            () ->
                community.moderate(
                    edited.id(),
                    edited.status(),
                    com.astavet.shop.domain.ReviewStatus.APPROVED,
                    edited.version()));
    assertThat(community.publicReviews(PRODUCT, 0))
        .hasSize(1)
        .first()
        .extracting(com.astavet.shop.domain.Review::body)
        .isEqualTo("New review content");
    as("a@example.test", "CUSTOMER", () -> community.submit(PRODUCT, 5, "Edited after approval"));
    assertThat(community.publicReviews(PRODUCT, 0)).isEmpty();
    accounts.register("b@example.test", "B", "long-test-password");
    assertThatThrownBy(
            () ->
                as(
                    "b@example.test",
                    "CUSTOMER",
                    () -> community.submit(PRODUCT, 5, "Forged review body")))
        .isInstanceOf(ShopException.class);
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void discountNeverExceedsMerchandiseAndFreeShippingUsesGrossSubtotal() {
    coupons.save(
        null,
        "ALL",
        new BigDecimal("1000.00"),
        BigDecimal.ZERO,
        java.time.Instant.now().plusSeconds(3600),
        10,
        true,
        0);
    UUID cart = UUID.randomUUID();
    carts.setQuantity(cart, PRODUCT, 1);
    Quote single = carts.setCoupon(cart, "ALL");
    assertThat(single.discount()).isEqualByComparingTo("49.00");
    assertThat(single.shipping()).isEqualByComparingTo("7.95");
    assertThat(single.total()).isEqualByComparingTo("7.95");
    Quote bulk = carts.setQuantity(cart, PRODUCT, 3);
    assertThat(bulk.subtotal()).isEqualByComparingTo("147.00");
    assertThat(bulk.shipping()).isEqualByComparingTo("0.00");
    assertThat(bulk.total()).isEqualByComparingTo("0.00");
    Order placed = orders.place(cart, checkout(bulk));
    assertThat(placed.total()).isEqualByComparingTo("0.00");
    assertThat(placed.discount()).isEqualByComparingTo("147.00");
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void couponMinimumExpiryAndInventoryFailuresDoNotConsumeUses() {
    var coupon =
        coupons.save(
            null,
            "MINIMUM",
            new BigDecimal("10.00"),
            new BigDecimal("100.00"),
            java.time.Instant.now().plusSeconds(3600),
            10,
            true,
            0);
    UUID cart = UUID.randomUUID();
    carts.setQuantity(cart, PRODUCT, 1);
    assertThatThrownBy(() -> carts.setCoupon(cart, coupon.code()))
        .isInstanceOf(ShopException.class);
    assertThat(carts.view(cart).couponCode()).isNull();
    carts.setQuantity(cart, PRODUCT, 3);
    Quote quoted = carts.setCoupon(cart, coupon.code());
    setStock(0);
    assertThatThrownBy(() -> orders.place(cart, checkout(quoted)))
        .isInstanceOf(ShopException.class);
    assertThat(coupons.list(0).getFirst().uses()).isZero();
    assertThat(orderCount()).isZero();
    assertThat(carts.view(cart).lines()).hasSize(1);
    transactions.executeWithoutResult(
        tx ->
            entityManager
                .createQuery("update CouponEntity c set c.expiresAt = :time")
                .setParameter("time", java.time.Instant.now().minusSeconds(1))
                .executeUpdate());
    assertThat(carts.view(cart).couponMessage()).isNotBlank();
    assertThat(carts.setCoupon(cart, "").couponCode()).isNull();
  }

  @Test
  void accountWishlistAndCheckoutUseConsistentLocks() throws Exception {
    accounts.register("a@example.test", "A", "long-test-password");
    UUID cart = UUID.randomUUID();
    Checkout request = checkout(carts.setQuantity(cart, PRODUCT, 1));
    List<Object> results =
        concurrently(
            List.of(
                () -> as("a@example.test", "CUSTOMER", () -> orders.place(cart, request)),
                () ->
                    as("a@example.test", "CUSTOMER", () -> community.setWishlist(PRODUCT, true))));
    assertThat(results.stream().filter(Order.class::isInstance)).hasSize(1);
    assertThat(results.stream().filter(List.class::isInstance)).hasSize(1);
    assertThat(products.find(PRODUCT).stock()).isEqualTo(99);
    assertThat(as("a@example.test", "CUSTOMER", () -> community.wishlist())).hasSize(1);
  }

  @Test
  void orderPagesFetchAllLinesInTwoQueriesAndPreserveAccountIsolation() {
    UUID account = accounts.register("page@example.test", "Page", "long-test-password").id();
    UUID other = accounts.register("other@example.test", "Other", "long-test-password").id();
    UUID secondProduct = seedProducts(1).getFirst();
    UUID cart = UUID.randomUUID();
    jdbc.update("insert into carts(id) values (?)", cart);
    List<UUID> expected = new ArrayList<>();
    for (int i = 0; i < 26; i++) {
      UUID id = new UUID(0, 1000 + i);
      expected.add(id);
      seedOrder(id, cart, account, secondProduct);
    }
    seedOrder(new UUID(0, 999), cart, other, secondProduct);
    List<Order> first = measured(2, () -> as("admin", "ADMIN", () -> orders.list(0)));
    List<UUID> all = new ArrayList<>();
    all.add(new UUID(0, 999));
    all.addAll(expected);
    assertThat(first).extracting(Order::id).containsExactlyElementsOf(all.subList(0, 25));
    assertThat(first).allSatisfy(order -> assertThat(order.lines()).hasSize(2));
    assertThat(measured(2, () -> as("admin", "ADMIN", () -> orders.list(1))))
        .extracting(Order::id)
        .containsExactlyElementsOf(all.subList(25, 27));
    assertThat(measured(1, () -> as("admin", "ADMIN", () -> orders.list(2)))).isEmpty();
    List<Order> own =
        measured(3, () -> as("page@example.test", "CUSTOMER", () -> orders.listMine(0)));
    assertThat(own).extracting(Order::id).containsExactlyElementsOf(expected.subList(0, 25));
    assertThat(own).allSatisfy(order -> assertThat(order.lines()).hasSize(2));
    assertThat(measured(3, () -> as("page@example.test", "CUSTOMER", () -> orders.listMine(1))))
        .extracting(Order::id)
        .containsExactly(expected.getLast());
  }

  @Test
  void wishlistBatchReadHasConstantQueryCountAndKeepsSavedOrder() {
    UUID account = accounts.register("batch@example.test", "Batch", "long-test-password").id();
    accounts.register("empty@example.test", "Empty", "long-test-password");
    List<UUID> ids = seedProducts(25);
    for (int i = 0; i < ids.size(); i++) {
      jdbc.update(
          "insert into wishlists(id,account_id,product_id,created_at) values(?,?,?,?)",
          UUID.randomUUID(),
          account,
          ids.get(i),
          java.sql.Timestamp.from(Instant.parse("2026-01-01T00:00:00Z").plusSeconds(i)));
    }
    List<UUID> reversed = ids.reversed();
    assertThat(measured(3, () -> as("batch@example.test", "CUSTOMER", () -> community.wishlist())))
        .extracting(com.astavet.shop.domain.Product::id)
        .containsExactlyElementsOf(reversed);
    assertThat(measured(2, () -> as("empty@example.test", "CUSTOMER", () -> community.wishlist())))
        .isEmpty();
    assertThat(products.findAll(List.of(ids.getLast(), PRODUCT, ids.getFirst(), ids.getLast())))
        .extracting(com.astavet.shop.domain.Product::id)
        .containsExactly(ids.getLast(), PRODUCT, ids.getFirst(), ids.getLast());
    assertThatThrownBy(() -> products.findAll(List.of(UUID.randomUUID())))
        .isInstanceOf(ShopException.class)
        .hasMessage("Product not found.");
  }

  @Test
  void publicCatalogueIsBoundedSearchableAndDetailsReachLaterPages() throws Exception {
    List<UUID> ids = seedProducts(26);
    jdbc.update("update products set active=false where id=?", ids.get(24));
    mvc.perform(get("/api/v1/products"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(25));
    mvc.perform(get("/api/v1/products").param("page", "1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].id").value(ids.getLast().toString()));
    mvc.perform(get("/api/v1/products").param("q", "ZZ fixture"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(ids.getFirst().toString()))
        .andExpect(jsonPath("$.length()").value(25));
    jdbc.update("update products set name='ZZ 100%_literal' where id=?", ids.getLast());
    mvc.perform(get("/api/v1/products").param("q", "%_"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].id").value(ids.getLast().toString()));
    mvc.perform(get("/api/v1/products/by-slug/fixture-25"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(ids.getLast().toString()));
    mvc.perform(get("/api/v1/products/by-slug/fixture-24")).andExpect(status().isNotFound());
    mvc.perform(get("/api/v1/products").param("page", "-1")).andExpect(status().isBadRequest());
    mvc.perform(get("/api/v1/products").param("page", "100001")).andExpect(status().isBadRequest());
    mvc.perform(get("/api/v1/products").param("q", "x".repeat(101)))
        .andExpect(status().isBadRequest());
    mvc.perform(get("/api/v1/products/lookup").param("ids", ids.getLast() + "," + ids.get(24)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1));
    mvc.perform(
            get("/api/v1/products/lookup")
                .param(
                    "ids", String.join(",", java.util.Collections.nCopies(51, PRODUCT.toString()))))
        .andExpect(status().isBadRequest());
    mvc.perform(get("/api/v1/products/lookup")).andExpect(status().isBadRequest());
    mvc.perform(get("/api/v1/products/lookup").param("ids", "invalid"))
        .andExpect(status().isBadRequest());
  }

  private List<UUID> seedProducts(int count) {
    List<UUID> ids = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      UUID id = new UUID(0, 100 + i);
      ids.add(id);
      jdbc.update(
          "insert into products(id,slug,name,description,price,currency,stock,active,image_path)"
              + " values (?,?,?,'Fixture',10,'AUD',100,true,'/assets/product-placeholder.svg')",
          id,
          "fixture-" + i,
          "ZZ fixture");
    }
    return ids;
  }

  private void seedOrder(UUID id, UUID cart, UUID account, UUID secondProduct) {
    jdbc.update(
        "insert into customer_orders"
            + " (id,cart_id,idempotency_key,request_hash,customer_name,email,phone,address,city,postcode,state,subtotal,shipping,gross_total,total,currency,payment_method,status,created_at,updated_at,account_id)"
            + " values"
            + " (?,?,?,?,'Fixture','fixture@example.test','12345678','Test','Test','2000','NSW',20,0,20,20,'AUD','COD','PLACED',?,?,?)",
        id,
        cart,
        UUID.randomUUID(),
        "a".repeat(64),
        java.sql.Timestamp.from(Instant.parse("2026-01-01T00:00:00Z")),
        java.sql.Timestamp.from(Instant.parse("2026-01-01T00:00:00Z")),
        account);
    for (UUID productId : List.of(PRODUCT, secondProduct)) {
      jdbc.update(
          "insert into order_lines(id,order_id,product_id,name,unit_price,quantity)"
              + " values(?,?,?,'Snapshot',10,1)",
          UUID.randomUUID(),
          id,
          productId);
    }
  }

  private <T> T measured(long queries, java.util.function.Supplier<T> action) {
    var statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
    boolean enabled = statistics.isStatisticsEnabled();
    statistics.setStatisticsEnabled(true);
    statistics.clear();
    try {
      T value = action.get();
      assertThat(statistics.getPrepareStatementCount()).isEqualTo(queries);
      assertThat(statistics.getCollectionFetchCount()).isZero();
      return value;
    } finally {
      statistics.setStatisticsEnabled(enabled);
    }
  }

  private <T> T as(String name, String role, java.util.function.Supplier<T> action) {
    var previous = SecurityContextHolder.getContext().getAuthentication();
    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken(
                name, "unused", List.of(new SimpleGrantedAuthority("ROLE_" + role))));
    try {
      return action.get();
    } finally {
      SecurityContextHolder.getContext().setAuthentication(previous);
    }
  }

  private Checkout checkout(Quote quote) {
    return new Checkout(UUID.randomUUID(), quote.fingerprint(), CUSTOMER);
  }

  private void setStock(int stock) {
    transactions.executeWithoutResult(
        transaction ->
            entityManager
                .createQuery("update ProductEntity p set p.stock = :stock")
                .setParameter("stock", stock)
                .executeUpdate());
  }

  private long orderCount() {
    return transactions.execute(
        transaction ->
            entityManager
                .createQuery("select count(o) from OrderEntity o", Long.class)
                .getSingleResult());
  }

  private long eventCount() {
    return transactions.execute(
        transaction ->
            entityManager
                .createQuery("select count(o) from OrderEventEntity o", Long.class)
                .getSingleResult());
  }

  private String payload(String fingerprint, UUID key) {
    return json.writeValueAsString(
        Map.of(
            "idempotencyKey",
            key,
            "quoteFingerprint",
            fingerprint,
            "name",
            CUSTOMER.name(),
            "email",
            CUSTOMER.email(),
            "phone",
            CUSTOMER.phone(),
            "address",
            CUSTOMER.address(),
            "city",
            CUSTOMER.city(),
            "postcode",
            CUSTOMER.postcode(),
            "state",
            CUSTOMER.state()));
  }

  private List<Object> concurrently(List<Callable<Object>> actions) throws Exception {
    CountDownLatch start = new CountDownLatch(1);
    try (var executor = Executors.newFixedThreadPool(actions.size())) {
      var futures =
          actions.stream()
              .map(
                  action ->
                      executor.submit(
                          () -> {
                            if (!start.await(5, TimeUnit.SECONDS)) {
                              throw new IllegalStateException("Concurrent start timed out");
                            }
                            try {
                              return action.call();
                            } catch (ShopException exception) {
                              return exception;
                            }
                          }))
              .toList();
      start.countDown();
      List<Object> results = new ArrayList<>();
      for (var future : futures) {
        results.add(future.get(20, TimeUnit.SECONDS));
      }
      return results;
    }
  }
}
