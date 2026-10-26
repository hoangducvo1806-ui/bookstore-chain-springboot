package com.bookstore.bookstore.service.impl;

import com.bookstore.bookstore.dto.OrderNotificationDto;
import com.bookstore.bookstore.entity.ChatMessage;
import com.bookstore.bookstore.entity.Order;
import com.bookstore.bookstore.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void sendOrderNotification(Order order, String message) {
        sendOrderNotification(order, "ORDER_UPDATE", message);
    }

    @Override
    public void sendOrderNotification(Order order, String type, String message) {
        if (order == null) return;

        try {
            OrderNotificationDto dto = OrderNotificationDto.builder()
                    .type(type)
                    .orderId(order.getId())
                    .orderCode(order.getOrderCode())
                    .shopId(order.getShop() != null ? order.getShop().getId() : null)
                    .shopName(order.getShop() != null ? order.getShop().getName() : "")
                    .status(order.getStatus() != null ? order.getStatus().name() : "")
                    .statusLabel(order.getStatus() != null ? order.getStatus().getLabel() : "")
                    .message(message)
                    .time(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy")))
                    .build();

            // 1. Broadcast toàn hệ thống
            messagingTemplate.convertAndSend("/topic/orders", dto);

            // 2. Gửi riêng cho Shop của Vendor
            if (order.getShop() != null) {
                messagingTemplate.convertAndSend("/topic/shop/" + order.getShop().getId(), dto);
            }

            // 3. Gửi riêng cho Khách hàng
            if (order.getCustomer() != null) {
                messagingTemplate.convertAndSend("/topic/user/" + order.getCustomer().getId(), dto);
            }

            log.info("WebSocket: Đã gửi thông báo đơn hàng {} tới topic /topic/orders và /topic/shop/{}",
                    order.getOrderCode(), order.getShop() != null ? order.getShop().getId() : "null");
        } catch (Exception e) {
            log.warn("Không thể gửi thông báo WebSocket: {}", e.getMessage());
        }
    }

    @Override
    public void sendChatMessage(ChatMessage message) {
        if (message == null) return;
        try {
            // Gửi tới người nhận
            if (message.getReceiver() != null) {
                messagingTemplate.convertAndSend("/topic/chat/user/" + message.getReceiver().getId(), message);
            }
            if (message.getSender() != null) {
                messagingTemplate.convertAndSend("/topic/chat/user/" + message.getSender().getId(), message);
            }
        } catch (Exception e) {
            log.warn("Không thể gửi tin nhắn chat WebSocket: {}", e.getMessage());
        }
    }
}
