package com.bookstore.bookstore.repository;

import com.bookstore.bookstore.entity.Review;
import com.bookstore.bookstore.entity.ReviewMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewMediaRepository extends JpaRepository<ReviewMedia, Long> {
    List<ReviewMedia> findByReview(Review review);
}
