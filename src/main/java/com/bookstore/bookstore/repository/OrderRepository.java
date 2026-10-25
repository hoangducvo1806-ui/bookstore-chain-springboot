package com.bookstore.bookstore.repository;

import com.bookstore.bookstore.entity.Order;
import com.bookstore.bookstore.entity.User;
import com.bookstore.bookstore.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Admin & Manager Queries
    List<Order> findTop10ByOrderByCreatedAtDesc();

    Page<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status, Pageable pageable);

    long countByStatus(OrderStatus status);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = com.bookstore.bookstore.enums.OrderStatus.DELIVERED")
    BigDecimal calculateTotalDeliveredRevenue();

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o")
    BigDecimal calculateTotalRevenue();

    // Customer Queries (TV1)
    List<Order> findByCustomerOrderByCreatedAtDesc(User customer);

    List<Order> findByCustomerAndStatusOrderByCreatedAtDesc(User customer, OrderStatus status);

    List<Order> findByCustomerAndStatusInOrderByCreatedAtDesc(User customer, List<OrderStatus> statuses);

    Optional<Order> findByOrderCode(String orderCode);

    Optional<Order> findByOrderCodeAndCustomer(String orderCode, User customer);
}
