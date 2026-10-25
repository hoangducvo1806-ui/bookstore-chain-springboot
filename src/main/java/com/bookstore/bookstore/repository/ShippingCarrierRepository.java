package com.bookstore.bookstore.repository;

import com.bookstore.bookstore.entity.ShippingCarrier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShippingCarrierRepository extends JpaRepository<ShippingCarrier, Long> {
    List<ShippingCarrier> findByActiveTrue();
    boolean existsByName(String name);
    Optional<ShippingCarrier> findByName(String name);
}
