package com.ecommerce.order.event;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class OrderEvent {
    private String eventType; // ORDER_CREATED, ORDER_CANCELLED, etc.
    private Long orderId;
    private String orderNumber;
    private String customerEmail;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime timestamp;
}
