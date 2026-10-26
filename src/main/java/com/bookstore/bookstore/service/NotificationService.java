package com.bookstore.bookstore.service;

import com.bookstore.bookstore.dto.OrderNotificationDto;
import com.bookstore.bookstore.entity.ChatMessage;
import com.bookstore.bookstore.entity.Order;

public interface NotificationService {

    void sendOrderNotification(Order order, String message);

    void sendOrderNotification(Order order, String type, String message);

    void sendChatMessage(ChatMessage message);
}
