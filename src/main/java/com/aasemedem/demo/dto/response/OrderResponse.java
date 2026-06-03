package com.aasemedem.demo.dto.response;

import com.aasemedem.demo.entity.Order;
import com.aasemedem.demo.entity.OrderItem;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data @Builder
public class OrderResponse {
    private Long id;
    private Long userId;
    private String userEmail;
    private String status;
    private BigDecimal totalInr;
    private String notes;
    private List<ItemDetail> items;
    private LocalDateTime createdAt;

    @Data @Builder
    public static class ItemDetail {
        private Long productId;
        private String productName;
        private String productSku;
        private String orderedUnit;
        private BigDecimal orderedQty;
        private BigDecimal baseQty;
        private String baseUnit;
        private BigDecimal unitPriceInr;
        private BigDecimal lineTotalInr;
    }

    public static OrderResponse from(Order o) {
        List<ItemDetail> items = o.getItems().stream().map(i -> ItemDetail.builder()
                .productId(i.getProduct().getId())
                .productName(i.getProduct().getName())
                .productSku(i.getProduct().getSku())
                .orderedUnit(i.getOrderedUnit())
                .orderedQty(i.getOrderedQty())
                .baseQty(i.getBaseQty())
                .baseUnit(i.getProduct().getBaseUnit())
                .unitPriceInr(i.getUnitPriceInr())
                .lineTotalInr(i.getLineTotalInr())
                .build()
        ).collect(Collectors.toList());

        return OrderResponse.builder()
                .id(o.getId())
                .userId(o.getUser().getId())
                .userEmail(o.getUser().getEmail())
                .status(o.getStatus())
                .totalInr(o.getTotalInr())
                .notes(o.getNotes())
                .items(items)
                .createdAt(o.getCreatedAt())
                .build();
    }
}

