package com.bookstore.bookstore.repository;

import com.bookstore.bookstore.entity.Book;
import com.bookstore.bookstore.entity.Order;
import com.bookstore.bookstore.entity.Review;
import com.bookstore.bookstore.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByBookOrderByCreatedAtDesc(Book book);
    List<Review> findByUserIdOrderByCreatedAtDesc(Long userId);
    boolean existsByBookAndOrderAndUser(Book book, Order order, User user);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.book.id = :bookId")
    Double getAverageRatingForBook(Long bookId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.book.id = :bookId")
    Long countReviewsForBook(Long bookId);
}
