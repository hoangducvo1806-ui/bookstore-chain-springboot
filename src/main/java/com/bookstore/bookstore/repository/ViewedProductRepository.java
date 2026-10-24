package com.bookstore.bookstore.repository;

import com.bookstore.bookstore.entity.Book;
import com.bookstore.bookstore.entity.User;
import com.bookstore.bookstore.entity.ViewedProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ViewedProductRepository extends JpaRepository<ViewedProduct, Long> {
    List<ViewedProduct> findByUserOrderByViewedAtDesc(User user);
    Optional<ViewedProduct> findByUserAndBook(User user, Book book);
}
