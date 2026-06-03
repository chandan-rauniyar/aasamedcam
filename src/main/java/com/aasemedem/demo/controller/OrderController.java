package com.aasemedem.demo.controller;

import com.aasemedem.demo.dto.request.OrderRequest;
import com.aasemedem.demo.dto.response.OrderResponse;
import com.aasemedem.demo.entity.User;
import com.aasemedem.demo.service.OrderService;
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

import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // ── Buyer endpoints ───────────────────────────────────────

    /** POST /api/orders — place a new order/quotation */
    @PostMapping
    @PreAuthorize("hasAnyRole('BUYER','SELLER','ADMIN')")
    public ResponseEntity<OrderResponse> place(@Valid @RequestBody OrderRequest.Place req,
                                               @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.place(req, user));
    }

    /** GET /api/orders/mine — buyer's own orders */
    @GetMapping("/mine")
    public Page<OrderResponse> myOrders(@AuthenticationPrincipal User user,
                                        @PageableDefault(size = 20) Pageable pageable) {
        return orderService.myOrders(user.getId(), pageable);
    }

    /** GET /api/orders/mine/{id} */
    @GetMapping("/mine/{id}")
    public OrderResponse getMyOrder(@PathVariable Long id,
                                    @AuthenticationPrincipal User user) {
        return orderService.getMyOrder(id, user.getId());
    }

    /** DELETE /api/orders/mine/{id} — buyer cancels own PENDING order */
    @DeleteMapping("/mine/{id}")
    public OrderResponse cancelMine(@PathVariable Long id,
                                    @AuthenticationPrincipal User user) {
        return orderService.cancelMine(id, user.getId());
    }

    // ── Admin endpoints ───────────────────────────────────────

    /** GET /api/orders — all orders (admin) */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<OrderResponse> all(@RequestParam(required = false) String status,
                                   @PageableDefault(size = 20) Pageable pageable) {
        if (status != null && !status.isBlank()) {
            return orderService.byStatus(status, pageable);
        }
        return orderService.all(pageable);
    }

    /** GET /api/orders/{id} — any specific order (admin) */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public OrderResponse getAny(@PathVariable Long id) {
        return orderService.getAny(id);
    }

    /** PATCH /api/orders/{id}/status — admin updates order status */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public OrderResponse updateStatus(@PathVariable Long id,
                                      @Valid @RequestBody OrderRequest.StatusUpdate req) {
        return orderService.updateStatus(id, req);
    }

    /** GET /api/orders/stats — admin dashboard counts */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> stats() {
        return orderService.stats();
    }
}

