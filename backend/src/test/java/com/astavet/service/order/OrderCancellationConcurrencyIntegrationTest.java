package com.astavet.service.order;

import static org.assertj.core.api.Assertions.assertThat;

import com.astavet.entity.order.CustomerOrder;
import com.astavet.entity.order.OrderItem;
import com.astavet.entity.order.OrderStatus;
import com.astavet.entity.product.Product;
import com.astavet.entity.product.ProductStatus;
import com.astavet.entity.product.ProductVariant;
import com.astavet.exception.ApiException;
import com.astavet.repository.order.OrderRepository;
import com.astavet.repository.product.ProductRepository;
import com.astavet.repository.product.ProductVariantRepository;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.support.TransactionTemplate;

@EnabledIfEnvironmentVariable(named = "ASTAVET_TEST_DATABASE_URL", matches = ".+")
@SpringBootTest(properties = { "store.admin.email=", "store.admin.password=" })
class OrderCancellationConcurrencyIntegrationTest {

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> System.getenv("ASTAVET_TEST_DATABASE_URL"));
        registry.add("spring.datasource.username",
                () -> System.getenv().getOrDefault("ASTAVET_TEST_DATABASE_USERNAME", System.getProperty("user.name")));
        registry.add("spring.datasource.password",
                () -> System.getenv().getOrDefault("ASTAVET_TEST_DATABASE_PASSWORD", ""));
    }

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantRepository variantRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @BeforeEach
    void cleanDatabase() {
        transactionTemplate.executeWithoutResult(status -> {
            orderRepository.deleteAll();
            productRepository.deleteAll();
        });
    }

    @Test
    void concurrentCancellationRestoresStockExactlyOnce() throws Exception {
        Fixture fixture = transactionTemplate.execute(status -> createOrderWithReservedStock());
        Authentication admin = UsernamePasswordAuthenticationToken.authenticated(
                "admin@example.com", "", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Future<String> first = executor.submit(() -> cancel(fixture.orderId(), admin, ready, start));
            Future<String> second = executor.submit(() -> cancel(fixture.orderId(), admin, ready, start));
            ready.await();
            start.countDown();

            assertThat(List.of(first.get(), second.get()))
                    .containsExactlyInAnyOrder("CANCELLED", "INVALID_STATUS_TRANSITION");
        }

        transactionTemplate.executeWithoutResult(status -> {
            CustomerOrder order = orderRepository.findOneById(fixture.orderId()).orElseThrow();
            ProductVariant variant = variantRepository.findById(fixture.variantId()).orElseThrow();
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
            assertThat(variant.getStockQuantity()).isEqualTo(10);
        });
    }

    private String cancel(UUID orderId, Authentication admin, CountDownLatch ready, CountDownLatch start)
            throws InterruptedException {
        ready.countDown();
        start.await();
        try {
            return orderService.updateStatus(orderId, OrderStatus.CANCELLED, admin).status().name();
        } catch (ApiException exception) {
            return exception.getCode();
        }
    }

    private Fixture createOrderWithReservedStock() {
        Product product = new Product("concurrency-test", "Concurrency test", null, null, ProductStatus.ACTIVE);
        ProductVariant variant = new ProductVariant(product, "Default", "CONCURRENCY-SKU", 100_000, 10, true);
        product.addVariant(variant);
        productRepository.saveAndFlush(product);

        variant.reserve(2);
        CustomerOrder order = new CustomerOrder("AST-CONCURRENCY", "Khách hàng", "0901234567", "Địa chỉ",
                null, 200_000, 30_000, "concurrency-key");
        order.addItem(new OrderItem(order, variant, 2));
        orderRepository.saveAndFlush(order);
        return new Fixture(order.getId(), variant.getId());
    }

    private record Fixture(UUID orderId, UUID variantId) {
    }
}
