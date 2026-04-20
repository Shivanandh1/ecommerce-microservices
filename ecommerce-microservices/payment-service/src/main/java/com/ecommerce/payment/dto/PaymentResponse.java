package com.ecommerce.payment.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PaymentResponse {
    private String paymentId;
    private String orderNumber;
    private BigDecimal amount;
    private String status;
    private String method;
    private String transactionId;
    private LocalDateTime createdAt;
}
