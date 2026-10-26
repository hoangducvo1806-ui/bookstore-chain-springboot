package com.bookstore.bookstore.service;

import com.bookstore.bookstore.dto.CheckoutForm;
import com.bookstore.bookstore.entity.Order;
import com.bookstore.bookstore.entity.User;
import com.bookstore.bookstore.enums.OrderStatus;
import org.springframework.data.domain.Page;

import java.util.List;

public interface OrderService {

    List<Order> checkout(User customer, CheckoutForm form);

    List<Order> getOrdersByUserAndTab(User customer, String tab);

    Order getOrderByCode(String orderCode, User customer);

    void cancelOrder(String orderCode, User customer, String reason);

    void requestReturnOrder(String orderCode, User customer, String reason);

    void updatePaymentSuccess(String orderCode, String transactionId, String paymentInfo);

    // Vendor / Shop methods (TV2)
    Page<Order> getOrdersByShopAndTab(User vendor, String tab, int page, int size);

    Order getOrderByShopAndId(User vendor, Long orderId);

    void updateOrderStatusByVendor(User vendor, Long orderId, OrderStatus newStatus, String note);

    // Shipper methods (TV2)
    Page<Order> getOrdersByShipperAndTab(User shipper, String tab, int page, int size);

    Page<Order> getAvailableOrdersForShipper(int page, int size);

    void claimOrder(User shipper, Long orderId);

    void updateOrderStatusByShipper(User shipper, Long orderId, OrderStatus newStatus, String note);
}
