package com.astavet.controller.admin;

import com.astavet.dto.request.product.UpsertProductRequest;
import com.astavet.dto.response.product.ProductResponse;
import com.astavet.service.product.ProductService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/products")
public class AdminProductController {

    private final ProductService productService;

    public AdminProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<ProductResponse> findAll() {
        return productService.findAllForAdmin();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(@Valid @RequestBody UpsertProductRequest request) {
        return productService.create(request);
    }

    @PutMapping("/{id}")
    public ProductResponse update(@PathVariable UUID id, @Valid @RequestBody UpsertProductRequest request) {
        return productService.update(id, request);
    }
}
