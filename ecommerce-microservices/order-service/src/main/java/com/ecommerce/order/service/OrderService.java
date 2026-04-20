package com.ecommerce.order.service;

import com.ecommerce.order.dto.*;
import com.ecommerce.order.entity.*;
import com.ecommerce.order.event.OrderEvent;
import com.ecommerce.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    @Transactional
    public OrderResponse createOrder(OrderRequest request, String customerEmail) {
        List<OrderItem> items = request.getItems().stream().map(item -> {
            BigDecimal unitPrice = BigDecimal.valueOf(99.99); // In real app, fetch from product-service
            return OrderItem.builder()
                    .productId(item.getProductId())
                    .productName("Product-" + item.getProductId())
                    .quantity(item.getQuantity())
                    .unitPrice(unitPrice)
                    .totalPrice(unitPrice.multiply(BigDecimal.valueOf(item.getQuantity())))
                    .build();
        }).collect(Collectors.toList());

        BigDecimal total = items.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = Order.builder()
                .orderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .customerEmail(customerEmail)
                .totalAmount(total)
                .shippingAddress(request.getShippingAddress())
                .status(Order.OrderStatus.PENDING)
                .build();

        items.forEach(item -> item.setOrder(order));
        order.setItems(items);

        Order saved = orderRepository.save(order);

        // Publish Kafka event
        OrderEvent event = OrderEvent.builder()
                .eventType("ORDER_CREATED")
                .orderId(saved.getId())
                .orderNumber(saved.getOrderNumber())
                .customerEmail(customerEmail)
                .totalAmount(total)
                .status(saved.getStatus().name())
                .timestamp(LocalDateTime.now())
                .build();
        kafkaTemplate.send("order-events", event);
        log.info("Order created: {}", saved.getOrderNumber());

        return toResponse(saved);
    }

    public Page<OrderResponse> getMyOrders(String email, int page, int size) {
        return orderRepository.findByCustomerEmail(email,
                PageRequest.of(page, size, Sort.by("createdAt").descending()))
                .map(this::toResponse);
    }

    public OrderResponse getByOrderNumber(String orderNumber, String email) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderNumber));
        if (!order.getCustomerEmail().equals(email)) {
            throw new RuntimeException("Access denied");
        }
        return toResponse(order);
    }

    @Transactional
    public OrderResponse cancelOrder(String orderNumber, String email) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        if (!order.getCustomerEmail().equals(email)) throw new RuntimeException("Access denied");
        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new RuntimeException("Only pending orders can be cancelled");
        }
        order.setStatus(Order.OrderStatus.CANCELLED);
        return toResponse(orderRepository.save(order));
    }

    private OrderResponse toResponse(Order o) {
        List<OrderItemResponse> items = o.getItems().stream().map(i ->
                OrderItemResponse.builder()
                        .productId(i.getProductId()).productName(i.getProductName())
                        .quantity(i.getQuantity()).unitPrice(i.getUnitPrice())
                        .totalPrice(i.getTotalPrice()).build()
        ).collect(Collectors.toList());

        return OrderResponse.builder()
                .id(o.getId()).orderNumber(o.getOrderNumber())
                .customerEmail(o.getCustomerEmail()).items(items)
                .totalAmount(o.getTotalAmount()).status(o.getStatus().name())
                .shippingAddress(o.getShippingAddress()).createdAt(o.getCreatedAt()).build();
    }
}
