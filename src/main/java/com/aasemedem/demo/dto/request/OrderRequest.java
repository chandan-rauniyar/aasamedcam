package com.aasemedem.demo.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

public class OrderRequest {

    @Data
    public static class Place {
        @NotNull @NotEmpty
        private List<ItemLine> items;
        private String notes;
    }

    @Data
    public static class ItemLine {
        @NotNull
        private Long productId;

        @NotNull @DecimalMin("0.0001")
        private BigDecimal qty;

        @NotBlank
        private String unit;   // g / kg / mL / L / count
    }

    @Data
    public static class StatusUpdate {
        @NotBlank
        @Pattern(regexp = "^(PENDING|CONFIRMED|DISPATCHED|CANCELLED)$")
        private String status;
    }
}

