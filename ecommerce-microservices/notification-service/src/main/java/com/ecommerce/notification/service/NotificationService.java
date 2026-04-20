package com.ecommerce.notification.service;

import com.ecommerce.notification.entity.Notification;
import com.ecommerce.notification.event.OrderEvent;
import com.ecommerce.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final JavaMailSender mailSender;

    @KafkaListener(topics = "order-events", groupId = "notification-group")
    public void handleOrderEvent(OrderEvent event) {
        log.info("Received order event: {} for order {}", event.getEventType(), event.getOrderNumber());

        String subject = buildSubject(event);
        String message = buildMessage(event);

        Notification notification = Notification.builder()
                .recipientEmail(event.getCustomerEmail())
                .subject(subject)
                .message(message)
                .type(Notification.NotificationType.EMAIL)
                .sent(false)
                .build();

        try {
            sendEmail(event.getCustomerEmail(), subject, message);
            notification.setSent(true);
        } catch (Exception e) {
            log.error("Failed to send email notification: {}", e.getMessage());
        }

        notificationRepository.save(notification);
    }

    private void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    private String buildSubject(OrderEvent event) {
        return switch (event.getEventType()) {
            case "ORDER_CREATED" -> "Order Confirmed - " + event.getOrderNumber();
            case "ORDER_CANCELLED" -> "Order Cancelled - " + event.getOrderNumber();
            case "ORDER_SHIPPED" -> "Your Order is Shipped - " + event.getOrderNumber();
            default -> "Order Update - " + event.getOrderNumber();
        };
    }

    private String buildMessage(OrderEvent event) {
        return String.format(
            "Dear Customer,\n\nYour order %s has been %s.\nTotal Amount: $%.2f\n\nThank you for shopping with us!",
            event.getOrderNumber(), event.getStatus(), event.getTotalAmount()
        );
    }
}
