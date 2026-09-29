package com.astavet.product;

import com.astavet.product.ProductDtos.ImageRequest;
import com.astavet.product.ProductDtos.ProductResponse;
import com.astavet.product.ProductDtos.UpsertProductRequest;
import com.astavet.product.ProductDtos.VariantRequest;
import com.astavet.shared.ApiException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findActiveProducts() {
        return productRepository.findAllByStatusOrderByCreatedAtDesc(ProductStatus.ACTIVE).stream()
                .map(product -> ProductResponse.from(product, false))
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse findActiveBySlug(String slug) {
        Product product = productRepository.findBySlugAndStatus(normalizeSlug(slug), ProductStatus.ACTIVE)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND", "Không tìm thấy sản phẩm."));
        return ProductResponse.from(product, false);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findAllForAdmin() {
        return productRepository.findAll().stream()
                .map(product -> ProductResponse.from(product, true))
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
        return ProductResponse.from(productRepository.save(product), true);
    }

    @Transactional
    public ProductResponse update(UUID id, UpsertProductRequest request) {
        Product product = productRepository.findOneById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND", "Không tìm thấy sản phẩm."));
        String slug = normalizeSlug(request.slug());
        if (!product.getSlug().equals(slug) && productRepository.existsBySlug(slug)) {
            throw new ApiException(HttpStatus.CONFLICT, "SLUG_EXISTS", "Đường dẫn sản phẩm đã tồn tại.");
        }
        product.update(slug, request.name().trim(), trim(request.shortDescription()), trim(request.description()), request.status());
        replaceDetails(product, request);
        return ProductResponse.from(product, true);
    }

    private void replaceDetails(Product product, UpsertProductRequest request) {
        List<Product.ProductImageInput> images = request.images() == null ? List.of() : request.images().stream()
                .map(this::toImageInput)
                .toList();
        product.replaceImages(images);

        for (VariantRequest variantRequest : request.variants()) {
            ProductVariant variant = variantRequest.id() == null
                    ? new ProductVariant(product, variantRequest.name().trim(), variantRequest.sku().trim(),
                            variantRequest.price(), variantRequest.stockQuantity(), variantRequest.active())
                    : product.getVariants().stream()
                            .filter(candidate -> candidate.getId().equals(variantRequest.id()))
                            .findFirst()
                            .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "INVALID_VARIANT", "Biến thể không thuộc sản phẩm."));
            if (variantRequest.id() == null) {
                product.addVariant(variant);
            } else {
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

