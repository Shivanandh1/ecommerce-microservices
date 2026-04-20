package com.ecommerce.order.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class OrderRequest {
    @NotEmpty
    private List<OrderItemRequest> items;
    @NotNull
    private String shippingAddress;
}
