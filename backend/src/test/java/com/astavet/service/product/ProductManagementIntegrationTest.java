package com.astavet.service.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.astavet.dto.request.order.CreateOrderItemRequest;
import com.astavet.dto.request.order.CreateOrderRequest;
import com.astavet.dto.request.product.ImageRequest;
import com.astavet.dto.request.product.UpsertProductRequest;
import com.astavet.dto.request.product.VariantRequest;
import com.astavet.dto.response.product.ProductResponse;
import com.astavet.entity.order.OrderStatus;
import com.astavet.entity.product.ProductStatus;
import com.astavet.exception.ApiException;
import com.astavet.repository.order.OrderRepository;
import com.astavet.repository.product.ProductRepository;
import com.astavet.repository.product.ProductVariantRepository;
import com.astavet.service.order.OrderService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.support.TransactionTemplate;

@EnabledIfEnvironmentVariable(named = "ASTAVET_TEST_DATABASE_URL", matches = ".+")
@SpringBootTest(properties = { "store.admin.email=", "store.admin.password=" })
class ProductManagementIntegrationTest {

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> System.getenv("ASTAVET_TEST_DATABASE_URL"));
        registry.add("spring.datasource.username",
                () -> System.getenv().getOrDefault("ASTAVET_TEST_DATABASE_USERNAME", System.getProperty("user.name")));
        registry.add("spring.datasource.password",
                () -> System.getenv().getOrDefault("ASTAVET_TEST_DATABASE_PASSWORD", ""));
    }

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantRepository variantRepository;

    @Autowired
    private OrderRepository orderRepository;

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
    void replacesImagesAndReconcilesMultipleVariantsWithoutKeepingDeletedEntries() {
        ProductResponse created = productService.create(new UpsertProductRequest(
                "multi-product", "Sản phẩm nhiều biến thể", null, null, ProductStatus.ACTIVE,
                List.of(new ImageRequest("/images/one.jpg", "Một"), new ImageRequest("/images/two.jpg", "Hai")),
                List.of(new VariantRequest(null, null, "Nhỏ", "MULTI-S", 100_000, 10, true),
                        new VariantRequest(null, null, "Lớn", "MULTI-L", 150_000, 5, true))));
        UUID removedVariantId = created.variants().getFirst().id();
        UUID retainedVariantId = created.variants().getLast().id();

        ProductResponse updated = productService.update(created.id(), new UpsertProductRequest(
                created.slug(), created.name(), created.shortDescription(), created.description(), created.status(),
                List.of(new ImageRequest("/images/two.jpg", "Ảnh giữ lại"),
                        new ImageRequest("/images/three.jpg", "Ảnh mới")),
                List.of(new VariantRequest(retainedVariantId, created.variants().getLast().version(),
                        "Lớn cập nhật", "MULTI-L", 160_000, 7, true),
                        new VariantRequest(null, null, "Rất lớn", "MULTI-XL", 200_000, 3, true))));

        assertThat(updated.images()).extracting("url")
                .containsExactly("/images/two.jpg", "/images/three.jpg");
        assertThat(updated.variants()).extracting("name")
                .containsExactlyInAnyOrder("Lớn cập nhật", "Rất lớn");
        assertThat(updated.variants().stream().filter(variant -> variant.id().equals(retainedVariantId))
                .findFirst().orElseThrow().version()).isGreaterThan(created.variants().getLast().version());
        assertThat(variantRepository.existsById(removedVariantId)).isFalse();
    }

    @Test
    void rejectsRemovalOfOrderedVariantAndStillAllowsCancellation() {
        ProductResponse product = createSingleVariantProduct("ordered-product", "ORDERED-SKU");
        UUID variantId = product.variants().getFirst().id();
        UUID orderId = placeOrder(variantId);

        assertThatThrownBy(() -> productService.update(product.id(), new UpsertProductRequest(
                product.slug(), product.name(), null, null, ProductStatus.ACTIVE, List.of(),
                List.of(new VariantRequest(null, null, "Thay thế", "REPLACEMENT-SKU", 100_000, 5, true)))))
                .isInstanceOfSatisfying(ApiException.class,
                        exception -> assertThat(exception.getCode()).isEqualTo("VARIANT_HAS_ORDERS"));

        orderService.updateStatus(orderId, OrderStatus.CANCELLED,
                UsernamePasswordAuthenticationToken.authenticated("admin", "",
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
        assertThat(variantRepository.findById(variantId).orElseThrow().getStockQuantity()).isEqualTo(10);
    }

    @Test
    void rejectsStaleStockAfterCheckoutReservation() {
        ProductResponse product = createSingleVariantProduct("stock-product", "STOCK-SKU");
        UUID variantId = product.variants().getFirst().id();
        placeOrder(variantId);

        assertThatThrownBy(() -> productService.update(product.id(), new UpsertProductRequest(
                product.slug(), "Tên mới", null, null, ProductStatus.ACTIVE, List.of(),
                List.of(new VariantRequest(variantId, product.variants().getFirst().version(),
                        "Nhỏ", "STOCK-SKU", 100_000, 10, true)))))
                .isInstanceOfSatisfying(ApiException.class,
                        exception -> assertThat(exception.getCode()).isEqualTo("STALE_VARIANT"));
        assertThat(variantRepository.findById(variantId).orElseThrow().getStockQuantity()).isEqualTo(8);
    }

    private ProductResponse createSingleVariantProduct(String slug, String sku) {
        return productService.create(new UpsertProductRequest(slug, "Sản phẩm", null, null,
                ProductStatus.ACTIVE, List.of(),
                List.of(new VariantRequest(null, null, "Nhỏ", sku, 100_000, 10, true))));
    }

    private UUID placeOrder(UUID variantId) {
        return orderService.create(new CreateOrderRequest("Khách hàng", "0901234567", "Địa chỉ", null,
                UUID.randomUUID().toString(), List.of(new CreateOrderItemRequest(variantId, 2)))).id();
    }
}
