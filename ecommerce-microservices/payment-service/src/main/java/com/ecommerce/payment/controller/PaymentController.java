package com.ecommerce.payment.controller;

import com.ecommerce.payment.dto.*;
import com.ecommerce.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponse> processPayment(
            @Valid @RequestBody PaymentRequest request,
            @RequestHeader("X-Auth-User") String email) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.processPayment(request, email));
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable String paymentId) {
        return ResponseEntity.ok(paymentService.getPayment(paymentId));
    }

    @GetMapping("/my")
    public ResponseEntity<List<PaymentResponse>> getMyPayments(@RequestHeader("X-Auth-User") String email) {
        return ResponseEntity.ok(paymentService.getMyPayments(email));
    }

    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<PaymentResponse> refund(
            @PathVariable String paymentId,
            @RequestHeader("X-Auth-User") String email) {
        return ResponseEntity.ok(paymentService.refund(paymentId, email));
    }
}
