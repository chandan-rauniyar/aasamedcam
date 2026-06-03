package com.aasemedem.demo.service;

import com.aasemedem.demo.dto.request.ProductRequest;
import com.aasemedem.demo.dto.response.ProductResponse;
import com.aasemedem.demo.entity.Product;
import com.aasemedem.demo.entity.User;
import com.aasemedem.demo.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepo;
    private final UnitConversionService unitConversion;

    // ── Public browse ─────────────────────────────────────────

    public Page<ProductResponse> list(Pageable pageable) {
        return productRepo.findByIsActiveTrue(pageable)
                .map(this::toResponse);
    }

    public Page<ProductResponse> search(String q, Pageable pageable) {
        if (q == null || q.isBlank()) return list(pageable);
        return productRepo.search(q.trim(), pageable).map(this::toResponse);
    }

    public Page<ProductResponse> byCategory(String category, Pageable pageable) {
        return productRepo.findByCategoryAndIsActiveTrue(category, pageable)
                .map(this::toResponse);
    }

    public ProductResponse get(Long id) {
        Product p = productRepo.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
        return toResponse(p);
    }

    public List<String> categories() {
        return productRepo.findDistinctCategories();
    }

    // ── Admin / Seller CRUD ───────────────────────────────────

    @Transactional
    public ProductResponse create(ProductRequest.Create req, User creator) {
        if (req.getSku() != null && productRepo.existsBySku(req.getSku())) {
            throw new IllegalArgumentException("SKU already exists: " + req.getSku());
        }
        Product p = Product.builder()
                .name(req.getName())
                .sku(req.getSku())
                .description(req.getDescription())
                .category(req.getCategory())
                .baseUnit(req.getBaseUnit())
                .basePrice(req.getBasePrice())
                .stockInBase(req.getStockInBase())
                .createdBy(creator)
                .build();
        return toResponse(productRepo.save(p));
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest.Update req) {
        Product p = productRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
        if (req.getName()        != null) p.setName(req.getName());
        if (req.getDescription() != null) p.setDescription(req.getDescription());
        if (req.getCategory()    != null) p.setCategory(req.getCategory());
        if (req.getBasePrice()   != null) p.setBasePrice(req.getBasePrice());
        if (req.getStockInBase() != null) p.setStockInBase(req.getStockInBase());
        if (req.getIsActive()    != null) p.setIsActive(req.getIsActive());
        return toResponse(productRepo.save(p));
    }

    @Transactional
    public void delete(Long id) {
        Product p = productRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
        p.setIsActive(false);   // soft delete
        productRepo.save(p);
    }

    @Transactional
    public ProductResponse adjustStock(Long id, ProductRequest.StockAdjust req) {
        Product p = productRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
        java.math.BigDecimal newStock = p.getStockInBase().add(req.getDelta());
        if (newStock.compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Stock cannot go below zero");
        }
        p.setStockInBase(newStock);
        return toResponse(productRepo.save(p));
    }

    // ── Internal ──────────────────────────────────────────────

    public Product getEntity(Long id) {
        return productRepo.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
    }

    private ProductResponse toResponse(Product p) {
        return ProductResponse.from(p, unitConversion.allowedUnits(p.getBaseUnit()));
    }
}

