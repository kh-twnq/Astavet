package com.astavet.product;

import com.astavet.shared.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "products")
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 180)
    private String slug;

    @Column(nullable = false)
    private String name;

    @Column(name = "short_description", length = 500)
    private String shortDescription;

    @Column(columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ProductStatus status;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("sortOrder asc")
    private List<ProductImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("createdAt asc")
    private List<ProductVariant> variants = new ArrayList<>();

    protected Product() {
    }

    public Product(String slug, String name, String shortDescription, String description, ProductStatus status) {
        this.slug = slug;
        this.name = name;
        this.shortDescription = shortDescription;
        this.description = description;
        this.status = status;
    }

    public void update(String slug, String name, String shortDescription, String description, ProductStatus status) {
        this.slug = slug;
        this.name = name;
        this.shortDescription = shortDescription;
        this.description = description;
        this.status = status;
    }

    public void replaceImages(List<ProductImageInput> inputs) {
        images.clear();
        for (int index = 0; index < inputs.size(); index++) {
            ProductImageInput input = inputs.get(index);
            images.add(new ProductImage(this, input.url(), input.altText(), index));
        }
    }

    public void addVariant(ProductVariant variant) {
        variants.add(variant);
    }

    public UUID getId() {
        return id;
    }

    public String getSlug() {
        return slug;
    }

    public String getName() {
        return name;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public String getDescription() {
        return description;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public List<ProductImage> getImages() {
        return images;
    }

    public List<ProductVariant> getVariants() {
        return variants;
    }

    public record ProductImageInput(String url, String altText) {
    }
}

