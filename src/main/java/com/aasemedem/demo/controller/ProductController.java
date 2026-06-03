package com.aasemedem.demo.controller;

import com.aasemedem.demo.dto.request.ProductRequest;
import com.aasemedem.demo.dto.response.ProductResponse;
import com.aasemedem.demo.entity.User;
import com.aasemedem.demo.service.ProductService;
import com.aasemedem.demo.service.UnitConversionService;
import com.aasemedem.demo.dto.response.ProductResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final UnitConversionService unitConversion;

    // ── Public browse (any authenticated user) ────────────────

    /** GET /api/products?page=0&size=20 */
    @GetMapping
    public Page<ProductResponse> list(@PageableDefault(size = 20) Pageable pageable) {
        return productService.list(pageable);
    }

    /** GET /api/products/search?q=sodium&page=0 */
    @GetMapping("/search")
    public Page<ProductResponse> search(@RequestParam(defaultValue = "") String q,
                                        @PageableDefault(size = 20) Pageable pageable) {
        return productService.search(q, pageable);
    }

    /** GET /api/products/categories */
    @GetMapping("/categories")
    public List<String> categories() {
        return productService.categories();
    }

    /** GET /api/products/by-category?category=Reagent */
    @GetMapping("/by-category")
    public Page<ProductResponse> byCategory(@RequestParam String category,
                                            @PageableDefault(size = 20) Pageable pageable) {
        return productService.byCategory(category, pageable);
    }

    /** GET /api/products/{id} */
    @GetMapping("/{id}")
    public ProductResponse get(@PathVariable Long id) {
        return productService.get(id);
    }

    /**
     * GET /api/products/{id}/price-preview?qty=2&unit=kg
     * Calculates what the buyer will pay BEFORE placing the order.
     */
    @GetMapping("/{id}/price-preview")
    public Map<String, Object> pricePreview(@PathVariable Long id,
                                            @RequestParam BigDecimal qty,
                                            @RequestParam String unit) {
        ProductResponse product = productService.get(id);

        if (!unitConversion.isValidUnit(product.getBaseUnit(), unit)) {
            throw new IllegalArgumentException("Unit '" + unit + "' not valid for this product");
        }

        BigDecimal baseQty   = unitConversion.toBase(qty, unit);
        BigDecimal lineTotal = unitConversion.lineTotal(qty, unit, product.getBasePrice());

        return Map.of(
                "productId",     id,
                "productName",   product.getName(),
                "orderedQty",    qty,
                "orderedUnit",   unit,
                "baseQty",       baseQty,
                "baseUnit",      product.getBaseUnit(),
                "unitPriceInr",  product.getBasePrice(),
                "lineTotalInr",  lineTotal
        );
    }

    // ── Admin / Seller: create & update ──────────────────────

    /** POST /api/products */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SELLER')")
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest.Create req,
                                                  @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(req, user));
    }

    /** PUT /api/products/{id} */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SELLER')")
    public ProductResponse update(@PathVariable Long id,
                                  @Valid @RequestBody ProductRequest.Update req) {
        return productService.update(id, req);
    }

    /** PATCH /api/products/{id}/stock */
    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasAnyRole('ADMIN','SELLER')")
    public ProductResponse adjustStock(@PathVariable Long id,
                                       @Valid @RequestBody ProductRequest.StockAdjust req) {
        return productService.adjustStock(id, req);
    }

    /** DELETE /api/products/{id} — soft delete, ADMIN only */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

