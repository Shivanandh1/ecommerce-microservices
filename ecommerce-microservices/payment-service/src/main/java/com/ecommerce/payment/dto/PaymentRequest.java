package com.ecommerce.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class PaymentRequest {
    @NotBlank private String orderNumber;
    @NotNull private BigDecimal amount;
    @NotBlank private String paymentMethod; // CREDIT_CARD, UPI, etc.
}
