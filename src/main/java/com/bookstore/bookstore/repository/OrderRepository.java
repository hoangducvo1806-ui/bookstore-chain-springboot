package com.bookstore.bookstore.repository;

import com.bookstore.bookstore.entity.Order;
import com.bookstore.bookstore.entity.User;
import com.bookstore.bookstore.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerOrderByCreatedAtDesc(User customer);
    List<Order> findByCustomerAndStatusOrderByCreatedAtDesc(User customer, OrderStatus status);
    List<Order> findByCustomerAndStatusInOrderByCreatedAtDesc(User customer, List<OrderStatus> statuses);
    Optional<Order> findByOrderCode(String orderCode);
    Optional<Order> findByOrderCodeAndCustomer(String orderCode, User customer);
}
