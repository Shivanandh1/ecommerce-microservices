package com.ecommerce.notification.event;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor
public class OrderEvent {
    private String eventType;
    private Long orderId;
    private String orderNumber;
    private String customerEmail;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime timestamp;
}
