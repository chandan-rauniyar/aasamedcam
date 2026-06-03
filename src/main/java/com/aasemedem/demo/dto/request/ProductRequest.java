package com.aasemedem.demo.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

public class ProductRequest {

    @Data
    public static class Create {
        @NotBlank
        private String name;

        private String sku;
        private String description;
        private String category;

        @NotBlank
        @Pattern(regexp = "^(g|mL|count)$", message = "baseUnit must be g, mL, or count")
        private String baseUnit;

        @NotNull @DecimalMin("0.0001")
        private BigDecimal basePrice;

        @DecimalMin("0")
        private BigDecimal stockInBase = BigDecimal.ZERO;
    }

    @Data
    public static class Update {
        private String name;
        private String description;
        private String category;
        @DecimalMin("0.0001")
        private BigDecimal basePrice;
        @DecimalMin("0")
        private BigDecimal stockInBase;
        private Boolean isActive;
    }

    @Data
    public static class StockAdjust {
        @NotNull
        private BigDecimal delta; // positive = add, negative = reduce
    }
}

