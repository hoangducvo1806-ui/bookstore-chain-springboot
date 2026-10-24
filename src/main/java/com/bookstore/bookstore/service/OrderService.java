package com.bookstore.bookstore.service;

import com.bookstore.bookstore.dto.CheckoutForm;
import com.bookstore.bookstore.entity.Order;
import com.bookstore.bookstore.entity.User;
import com.bookstore.bookstore.enums.OrderStatus;

import java.util.List;

public interface OrderService {

    List<Order> checkout(User customer, CheckoutForm form);

    List<Order> getOrdersByUserAndTab(User customer, String tab);

    Order getOrderByCode(String orderCode, User customer);

    void cancelOrder(String orderCode, User customer, String reason);

    void requestReturnOrder(String orderCode, User customer, String reason);

    void updatePaymentSuccess(String orderCode, String transactionId, String paymentInfo);
}
