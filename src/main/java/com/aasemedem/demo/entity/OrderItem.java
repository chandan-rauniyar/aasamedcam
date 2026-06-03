package com.aasemedem.demo.entity;


import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /** Unit the buyer chose: g / kg / mL / L / count */
    @Column(name = "ordered_unit", nullable = false, length = 10)
    private String orderedUnit;

    /** Raw value as entered by the user */
    @Column(name = "ordered_qty", nullable = false, precision = 18, scale = 4)
    private BigDecimal orderedQty;

    /** Converted to base_unit before saving */
    @Column(name = "base_qty", nullable = false, precision = 18, scale = 4)
    private BigDecimal baseQty;

    /** Snapshot of base_price at time of order */
    @Column(name = "unit_price_inr", nullable = false, precision = 18, scale = 4)
    private BigDecimal unitPriceInr;

    /** base_qty * unit_price_inr */
    @Column(name = "line_total_inr", nullable = false, precision = 18, scale = 2)
    private BigDecimal lineTotalInr;
}