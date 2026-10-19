package com.bookstore.bookstore.repository;

import com.bookstore.bookstore.entity.Shop;
import com.bookstore.bookstore.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Long> {
    Optional<Shop> findByOwner(User owner);
    boolean existsByName(String name);
}
