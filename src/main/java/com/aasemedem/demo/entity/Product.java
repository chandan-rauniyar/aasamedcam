package com.aasemedem.demo.entity;


import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true)
    private String sku;

    private String description;

    private String category;

    /**
     * base_unit: always one of  g | mL | count
     * ALL quantities stored in this unit.
     * kg -> stored as g, L -> stored as mL.
     */
    @Column(name = "base_unit", nullable = false, length = 10)
    private String baseUnit;

    /**
     * Price per 1 base_unit in INR.
     * NUMERIC(18,4) handles sub-paisa precision for bulk chemicals.
     */
    @Column(name = "base_price", nullable = false, precision = 18, scale = 4)
    private BigDecimal basePrice;

    /**
     * Current stock expressed in base_unit.
     */
    @Column(name = "stock_in_base", nullable = false, precision = 18, scale = 4)
    @Builder.Default
    private BigDecimal stockInBase = BigDecimal.ZERO;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void onUpdate() { this.updatedAt = LocalDateTime.now(); }
}
