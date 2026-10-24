package com.bookstore.bookstore.repository;

import com.bookstore.bookstore.entity.ShippingCarrier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShippingCarrierRepository extends JpaRepository<ShippingCarrier, Long> {
    List<ShippingCarrier> findByActiveTrue();
}
