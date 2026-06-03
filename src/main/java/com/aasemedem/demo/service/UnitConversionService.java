package com.aasemedem.demo.service;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Single source of truth for unit conversion.
 *
 * STORAGE STRATEGY
 * ─────────────────
 * Weight  → always stored in grams  (g)
 * Volume  → always stored in mL
 * Count   → stored as-is (count)
 *
 * CONVERSION TABLE
 * ─────────────────
 * kg  → g    × 1000
 * g   → g    × 1
 * L   → mL   × 1000
 * mL  → mL   × 1
 * count → count × 1
 *
 * Price is always per 1 base unit (per g, per mL, per count).
 */
@Service
public class UnitConversionService {

    private static final Map<String, BigDecimal> TO_BASE = Map.of(
            "g",     BigDecimal.ONE,
            "kg",    new BigDecimal("1000"),
            "mL",    BigDecimal.ONE,
            "L",     new BigDecimal("1000"),
            "count", BigDecimal.ONE
    );

    /** Convert user-entered qty to base unit */
    public BigDecimal toBase(BigDecimal qty, String unit) {
        BigDecimal factor = TO_BASE.get(unit);
        if (factor == null) throw new IllegalArgumentException("Unknown unit: " + unit);
        return qty.multiply(factor);
    }

    /** Convert base qty back to display unit */
    public BigDecimal fromBase(BigDecimal baseQty, String unit) {
        BigDecimal factor = TO_BASE.get(unit);
        if (factor == null) throw new IllegalArgumentException("Unknown unit: " + unit);
        if (factor.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return baseQty.divide(factor, 4, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Calculate the line total.
     * lineTotal = (orderedQty * conversionFactor) * basePricePerUnit
     */
    public BigDecimal lineTotal(BigDecimal orderedQty, String orderedUnit, BigDecimal basePricePerUnit) {
        BigDecimal baseQty = toBase(orderedQty, orderedUnit);
        return baseQty.multiply(basePricePerUnit)
                .setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /** Valid units a buyer can choose depending on product base_unit */
    public List<String> allowedUnits(String baseUnit) {
        return switch (baseUnit) {
            case "g"     -> List.of("g", "kg");
            case "mL"    -> List.of("mL", "L");
            case "count" -> List.of("count");
            default      -> List.of(baseUnit);
        };
    }

    public boolean isValidUnit(String baseUnit, String orderedUnit) {
        return allowedUnits(baseUnit).contains(orderedUnit);
    }
}

