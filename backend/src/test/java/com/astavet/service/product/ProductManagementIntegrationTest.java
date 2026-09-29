package com.astavet.service.product;

import static org.assertj.core.api.Assertions.assertThat;

import com.astavet.dto.request.product.ImageRequest;
import com.astavet.dto.request.product.UpsertProductRequest;
import com.astavet.dto.request.product.VariantRequest;
import com.astavet.dto.response.product.ProductResponse;
import com.astavet.entity.product.ProductStatus;
import com.astavet.repository.order.OrderRepository;
import com.astavet.repository.product.ProductRepository;
import com.astavet.repository.product.ProductVariantRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
                List.of(new VariantRequest(null, "Nhỏ", "MULTI-S", 100_000, 10, true),
                        new VariantRequest(null, "Lớn", "MULTI-L", 150_000, 5, true))));
        UUID removedVariantId = created.variants().getFirst().id();
        UUID retainedVariantId = created.variants().getLast().id();

        ProductResponse updated = productService.update(created.id(), new UpsertProductRequest(
                created.slug(), created.name(), created.shortDescription(), created.description(), created.status(),
                List.of(new ImageRequest("/images/two.jpg", "Ảnh giữ lại"),
                        new ImageRequest("/images/three.jpg", "Ảnh mới")),
                List.of(new VariantRequest(retainedVariantId, "Lớn cập nhật", "MULTI-L", 160_000, 7, true),
                        new VariantRequest(null, "Rất lớn", "MULTI-XL", 200_000, 3, true))));

        assertThat(updated.images()).extracting("url")
                .containsExactly("/images/two.jpg", "/images/three.jpg");
        assertThat(updated.variants()).extracting("name")
                .containsExactlyInAnyOrder("Lớn cập nhật", "Rất lớn");
        assertThat(variantRepository.existsById(removedVariantId)).isFalse();
    }
}
