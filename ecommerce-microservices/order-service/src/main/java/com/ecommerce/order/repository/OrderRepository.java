package com.ecommerce.order.repository;

import com.ecommerce.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByCustomerEmail(String customerEmail, Pageable pageable);
    Optional<Order> findByOrderNumber(String orderNumber);
}
