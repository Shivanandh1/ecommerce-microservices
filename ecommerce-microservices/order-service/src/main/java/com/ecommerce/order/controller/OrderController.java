package com.ecommerce.order.controller;

import com.ecommerce.order.dto.*;
import com.ecommerce.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody OrderRequest request,
            @RequestHeader("X-Auth-User") String email) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(request, email));
    }

    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getMyOrders(
            @RequestHeader("X-Auth-User") String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(orderService.getMyOrders(email, page, size));
    }

    @GetMapping("/{orderNumber}")
    public ResponseEntity<OrderResponse> getOrder(
            @PathVariable String orderNumber,
            @RequestHeader("X-Auth-User") String email) {
        return ResponseEntity.ok(orderService.getByOrderNumber(orderNumber, email));
    }

    @PutMapping("/{orderNumber}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable String orderNumber,
            @RequestHeader("X-Auth-User") String email) {
        return ResponseEntity.ok(orderService.cancelOrder(orderNumber, email));
    }
}
