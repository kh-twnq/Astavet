package com.astavet.shop.controller.v1;

import com.astavet.shop.dto.SaveProductRequest;
import com.astavet.shop.dto.ProductResponse;
import com.astavet.shop.dto.StockAdjustmentResponse;
import com.astavet.shop.dto.AdjustStockRequest;
import com.astavet.shop.service.CatalogueAdminService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.UUID;
import java.util.List;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
@RestController
@RequestMapping("/api/v1/admin/products")
public class AdminProductController {
    private final CatalogueAdminService catalogue;
    public AdminProductController(CatalogueAdminService catalogue) { this.catalogue = catalogue; }
    @GetMapping public List<ProductResponse> list(@RequestParam(defaultValue = "0") int page) { return catalogue.list(page).stream().map(ProductResponse::from).toList(); }
    @PostMapping public ProductResponse create(@Valid @RequestBody SaveProductRequest r) { return save(null, r); }
    @PutMapping("/{id}") public ProductResponse update(@PathVariable UUID id, @Valid @RequestBody SaveProductRequest r) { return save(id, r); }
    @PostMapping("/{id}/stock") public StockAdjustmentResponse adjust(@PathVariable UUID id, @Valid @RequestBody AdjustStockRequest r, Principal actor) {
        return StockAdjustmentResponse.from(catalogue.adjust(id, r.operationId(), r.delta(), r.reason(), r.expectedVersion(), actor.getName()));
    }
    @GetMapping("/{id}/stock") public List<StockAdjustmentResponse> adjustments(@PathVariable UUID id) { return catalogue.adjustments(id).stream().map(StockAdjustmentResponse::from).toList(); }
    private ProductResponse save(UUID id, SaveProductRequest r) { return ProductResponse.from(catalogue.save(id, r.slug(), r.name(), r.description(), r.price(), r.active(), r.imagePath(), r.expectedVersion())); }
}
