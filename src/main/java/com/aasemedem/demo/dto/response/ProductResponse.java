package com.aasemedem.demo.dto.response;

import com.aasemedem.demo.entity.Product;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder
public class ProductResponse {
    private Long id;
    private String name;
    private String sku;
    private String description;
    private String category;
    private String baseUnit;
    private BigDecimal basePrice;        // price per base unit INR
    private BigDecimal stockInBase;
    private List<String> allowedUnits;   // e.g. ["g","kg"]
    private Boolean isActive;
    private LocalDateTime createdAt;

    public static ProductResponse from(Product p, List<String> allowedUnits) {
        return ProductResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .sku(p.getSku())
                .description(p.getDescription())
                .category(p.getCategory())
                .baseUnit(p.getBaseUnit())
                .basePrice(p.getBasePrice())
                .stockInBase(p.getStockInBase())
                .allowedUnits(allowedUnits)
                .isActive(p.getIsActive())
                .createdAt(p.getCreatedAt())
                .build();
    }
}

