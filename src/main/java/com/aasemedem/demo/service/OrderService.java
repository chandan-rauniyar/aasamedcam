package com.aasemedem.demo.service;

import com.aasemedem.demo.dto.request.OrderRequest;
import com.aasemedem.demo.response.OrderResponse;
import com.aasemedem.demo.entity.*;
import com.aasemedem.demo.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepo;
    private final ProductService productService;
    private final UnitConversionService unitConversion;

    // ── Buyer: place an order ─────────────────────────────────

    @Transactional
    public OrderResponse place(OrderRequest.Place req, User buyer) {
        List<OrderItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (OrderRequest.ItemLine line : req.getItems()) {
            Product product = productService.getEntity(line.getProductId());

            // Validate unit is compatible with product's base_unit
            if (!unitConversion.isValidUnit(product.getBaseUnit(), line.getUnit())) {
                throw new IllegalArgumentException(
                        "Unit '" + line.getUnit() + "' is not valid for product base unit '" + product.getBaseUnit() + "'");
            }

            BigDecimal baseQty    = unitConversion.toBase(line.getQty(), line.getUnit());
            BigDecimal lineTotal  = unitConversion.lineTotal(line.getQty(), line.getUnit(), product.getBasePrice());

            // Check stock
            if (product.getStockInBase().compareTo(baseQty) < 0) {
                throw new IllegalArgumentException(
                        "Insufficient stock for: " + product.getName() +
                                " (available: " + product.getStockInBase() + " " + product.getBaseUnit() + ")");
            }

            // Deduct stock
            product.setStockInBase(product.getStockInBase().subtract(baseQty));

            OrderItem item = OrderItem.builder()
                    .product(product)
                    .orderedUnit(line.getUnit())
                    .orderedQty(line.getQty())
                    .baseQty(baseQty)
                    .unitPriceInr(product.getBasePrice())
                    .lineTotalInr(lineTotal)
                    .build();

            items.add(item);
            total = total.add(lineTotal);
        }

        Order order = Order.builder()
                .user(buyer)
                .notes(req.getNotes())
                .totalInr(total)
                .build();

        items.forEach(item -> item.setOrder(order));
        order.setItems(items);

        return OrderResponse.from(orderRepo.save(order));
    }

    // ── Buyer: view own orders ────────────────────────────────

    public Page<OrderResponse> myOrders(Long userId, Pageable pageable) {
        return orderRepo.findByUserId(userId, pageable).map(OrderResponse::from);
    }

    public OrderResponse getMyOrder(Long orderId, Long userId) {
        Order order = orderRepo.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        return OrderResponse.from(order);
    }

    // ── Admin: view all orders ────────────────────────────────

    public Page<OrderResponse> all(Pageable pageable) {
        return orderRepo.findAll(pageable).map(OrderResponse::from);
    }

    public Page<OrderResponse> byStatus(String status, Pageable pageable) {
        return orderRepo.findByStatus(status, pageable).map(OrderResponse::from);
    }

    public OrderResponse getAny(Long orderId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        return OrderResponse.from(order);
    }

    // ── Admin: update order status ────────────────────────────

    @Transactional
    public OrderResponse updateStatus(Long orderId, OrderRequest.StatusUpdate req) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        // Restore stock if CANCELLED
        if ("CANCELLED".equals(req.getStatus()) && !"CANCELLED".equals(order.getStatus())) {
            order.getItems().forEach(item -> {
                Product p = item.getProduct();
                p.setStockInBase(p.getStockInBase().add(item.getBaseQty()));
            });
        }

        order.setStatus(req.getStatus());
        return OrderResponse.from(orderRepo.save(order));
    }

    // ── Buyer: cancel own pending order ──────────────────────

    @Transactional
    public OrderResponse cancelMine(Long orderId, Long userId) {
        Order order = orderRepo.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        if (!"PENDING".equals(order.getStatus())) {
            throw new IllegalArgumentException("Only PENDING orders can be cancelled");
        }

        // Restore stock
        order.getItems().forEach(item -> {
            Product p = item.getProduct();
            p.setStockInBase(p.getStockInBase().add(item.getBaseQty()));
        });

        order.setStatus("CANCELLED");
        return OrderResponse.from(orderRepo.save(order));
    }

    // ── Dashboard stats for admin ─────────────────────────────

    public java.util.Map<String, Object> stats() {
        return java.util.Map.of(
                "total",      orderRepo.count(),
                "pending",    orderRepo.countByStatus("PENDING"),
                "confirmed",  orderRepo.countByStatus("CONFIRMED"),
                "dispatched", orderRepo.countByStatus("DISPATCHED"),
                "cancelled",  orderRepo.countByStatus("CANCELLED")
        );
    }
}
public class OrderService {
}
