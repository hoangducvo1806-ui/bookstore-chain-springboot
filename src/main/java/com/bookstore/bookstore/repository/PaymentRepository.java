package com.bookstore.bookstore.repository;

import com.bookstore.bookstore.entity.Order;
import com.bookstore.bookstore.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByOrder(Order order);
    Optional<Payment> findByTransactionId(String transactionId);
}
