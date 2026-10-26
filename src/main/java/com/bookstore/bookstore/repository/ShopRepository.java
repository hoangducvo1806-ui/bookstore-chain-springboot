package com.bookstore.bookstore.repository;

import com.bookstore.bookstore.entity.Shop;
import com.bookstore.bookstore.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Long> {
    Optional<Shop> findByOwner(User owner);
    boolean existsByName(String name);
    boolean existsByOwner(User owner);

    long countByActive(Boolean active);

    @Query("SELECT s FROM Shop s WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(s.address) LIKE LOWER(CONCAT('%', :keyword, '%')) OR s.phone LIKE CONCAT('%', :keyword, '%'))")
    Page<Shop> searchShops(@Param("keyword") String keyword, Pageable pageable);
}
