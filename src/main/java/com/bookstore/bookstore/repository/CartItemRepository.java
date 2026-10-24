package com.bookstore.bookstore.repository;

import com.bookstore.bookstore.entity.Book;
import com.bookstore.bookstore.entity.Cart;
import com.bookstore.bookstore.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartAndBook(Cart cart, Book book);
    void deleteByCartAndBook(Cart cart, Book book);
}
