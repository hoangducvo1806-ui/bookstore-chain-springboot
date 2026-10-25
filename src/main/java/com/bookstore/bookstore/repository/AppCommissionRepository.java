package com.bookstore.bookstore.repository;

import com.bookstore.bookstore.entity.AppCommission;
import com.bookstore.bookstore.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppCommissionRepository extends JpaRepository<AppCommission, Long> {
    Optional<AppCommission> findByShop(Shop shop);
    boolean existsByShop(Shop shop);
}
