package com.ecommerce.payment.service;

import com.ecommerce.payment.dto.*;
import com.ecommerce.payment.entity.Payment;
import com.ecommerce.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Transactional
    public PaymentResponse processPayment(PaymentRequest request, String customerEmail) {
        // Simulate payment processing
        boolean paymentSuccess = Math.random() > 0.1; // 90% success rate

        Payment payment = Payment.builder()
                .paymentId("PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .orderNumber(request.getOrderNumber())
                .customerEmail(customerEmail)
                .amount(request.getAmount())
                .method(Payment.PaymentMethod.valueOf(request.getPaymentMethod()))
                .status(paymentSuccess ? Payment.PaymentStatus.SUCCESS : Payment.PaymentStatus.FAILED)
                .transactionId(paymentSuccess ? "TXN-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase() : null)
                .failureReason(paymentSuccess ? null : "Payment declined by bank")
                .build();

        Payment saved = paymentRepository.save(payment);
        log.info("Payment processed: {} - Status: {}", saved.getPaymentId(), saved.getStatus());
        return toResponse(saved);
    }

    public PaymentResponse getPayment(String paymentId) {
        return toResponse(paymentRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId)));
    }

    public List<PaymentResponse> getMyPayments(String email) {
        return paymentRepository.findByCustomerEmail(email).stream()
                .map(this::toResponse).toList();
    }

    @Transactional
    public PaymentResponse refund(String paymentId, String email) {
        Payment payment = paymentRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        if (!payment.getCustomerEmail().equals(email)) throw new RuntimeException("Access denied");
        if (payment.getStatus() != Payment.PaymentStatus.SUCCESS) {
            throw new RuntimeException("Only successful payments can be refunded");
        }
        payment.setStatus(Payment.PaymentStatus.REFUNDED);
        return toResponse(paymentRepository.save(payment));
    }

    private PaymentResponse toResponse(Payment p) {
        return PaymentResponse.builder()
                .paymentId(p.getPaymentId()).orderNumber(p.getOrderNumber())
                .amount(p.getAmount()).status(p.getStatus().name())
                .method(p.getMethod().name()).transactionId(p.getTransactionId())
                .createdAt(p.getCreatedAt()).build();
    }
}
