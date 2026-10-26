package com.bookstore.bookstore.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderNotificationDto {
    private String type; // ORDER_CREATED, ORDER_CONFIRMED, ORDER_PICKED_UP, ORDER_SHIPPING, ORDER_DELIVERED, ORDER_CANCELLED
    private String orderCode;
    private Long orderId;
    private Long shopId;
    private String shopName;
    private String status;
    private String statusLabel;
    private String message;
    private String time;
}
