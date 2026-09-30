package com.astavet.service.product;

import com.astavet.dto.request.product.ImageRequest;
import com.astavet.dto.request.product.UpsertProductRequest;
import com.astavet.dto.request.product.VariantRequest;
import com.astavet.dto.response.product.ProductResponse;
import com.astavet.entity.product.Product;
import com.astavet.entity.product.ProductStatus;
import com.astavet.entity.product.ProductVariant;
import com.astavet.exception.ApiException;
import com.astavet.mapper.product.ProductMapper;
import com.astavet.repository.order.OrderItemRepository;
import com.astavet.repository.product.ProductRepository;
import com.astavet.repository.product.ProductVariantRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final OrderItemRepository orderItemRepository;

    public ProductService(ProductRepository productRepository, ProductVariantRepository variantRepository,
            OrderItemRepository orderItemRepository) {
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findActiveProducts() {
        return productRepository.findAllByStatusOrderByCreatedAtDesc(ProductStatus.ACTIVE).stream()
                .map(product -> ProductMapper.toResponse(product, false))
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse findActiveBySlug(String slug) {
        Product product = productRepository.findBySlugAndStatus(normalizeSlug(slug), ProductStatus.ACTIVE)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND", "Không tìm thấy sản phẩm."));
        return ProductMapper.toResponse(product, false);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findAllForAdmin() {
        return productRepository.findAll().stream()
                .map(product -> ProductMapper.toResponse(product, true))
                .toList();
    }

    @Transactional
    public ProductResponse create(UpsertProductRequest request) {
        String slug = normalizeSlug(request.slug());
        if (productRepository.existsBySlug(slug)) {
            throw new ApiException(HttpStatus.CONFLICT, "SLUG_EXISTS", "Đường dẫn sản phẩm đã tồn tại.");
        }
        Product product = new Product(slug, request.name().trim(), trim(request.shortDescription()),
                trim(request.description()), request.status());
        replaceDetails(product, request);
        return ProductMapper.toResponse(productRepository.saveAndFlush(product), true);
    }

    @Transactional
    public ProductResponse update(UUID id, UpsertProductRequest request) {
        Product product = productRepository.findOneById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND", "Không tìm thấy sản phẩm."));
        variantRepository.findAllByProductIdForUpdate(id);
        String slug = normalizeSlug(request.slug());
        if (!product.getSlug().equals(slug) && productRepository.existsBySlug(slug)) {
            throw new ApiException(HttpStatus.CONFLICT, "SLUG_EXISTS", "Đường dẫn sản phẩm đã tồn tại.");
        }
        product.update(slug, request.name().trim(), trim(request.shortDescription()), trim(request.description()), request.status());
        replaceDetails(product, request);
        productRepository.flush();
        return ProductMapper.toResponse(product, true);
    }

    private void replaceDetails(Product product, UpsertProductRequest request) {
        List<Product.ProductImageInput> images = request.images() == null ? List.of() : request.images().stream()
                .map(this::toImageInput)
                .toList();
        product.replaceImages(images);

        Set<UUID> requestedVariantIds = new HashSet<>();
        for (VariantRequest variantRequest : request.variants()) {
            if (variantRequest.id() != null && !requestedVariantIds.add(variantRequest.id())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "DUPLICATE_VARIANT",
                        "Một biến thể không thể xuất hiện nhiều lần.");
            }
        }
        for (ProductVariant variant : product.getVariants()) {
            if (variant.getId() != null && !requestedVariantIds.contains(variant.getId())
                    && orderItemRepository.existsByVariantId(variant.getId())) {
                throw new ApiException(HttpStatus.CONFLICT, "VARIANT_HAS_ORDERS",
                        "Không thể xóa biến thể đã có đơn hàng. Hãy ngừng bán biến thể này.");
            }
        }
        product.getVariants().removeIf(variant -> variant.getId() != null
                && !requestedVariantIds.contains(variant.getId()));

        for (VariantRequest variantRequest : request.variants()) {
            ProductVariant variant = variantRequest.id() == null
                    ? new ProductVariant(product, variantRequest.name().trim(), variantRequest.sku().trim(),
                            variantRequest.price(), variantRequest.stockQuantity(), variantRequest.active())
                    : product.getVariants().stream()
                            .filter(candidate -> variantRequest.id().equals(candidate.getId()))
                            .findFirst()
                            .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "INVALID_VARIANT", "Biến thể không thuộc sản phẩm."));
            if (variantRequest.id() == null) {
                product.addVariant(variant);
            } else {
                if (variantRequest.version() == null || variantRequest.version() != variant.getVersion()) {
                    throw new ApiException(HttpStatus.CONFLICT, "STALE_VARIANT",
                            "Biến thể đã thay đổi. Vui lòng tải lại sản phẩm trước khi lưu.");
                }
                variant.update(variantRequest.name().trim(), variantRequest.sku().trim(), variantRequest.price(),
                        variantRequest.stockQuantity(), variantRequest.active());
            }
        }
    }

    private Product.ProductImageInput toImageInput(ImageRequest request) {
        return new Product.ProductImageInput(request.url().trim(), trim(request.altText()));
    }

    private String normalizeSlug(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
