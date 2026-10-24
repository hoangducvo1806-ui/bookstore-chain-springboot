package com.bookstore.bookstore.service;

import com.bookstore.bookstore.dto.ReviewForm;
import com.bookstore.bookstore.entity.Book;
import com.bookstore.bookstore.entity.Order;
import com.bookstore.bookstore.entity.Review;
import com.bookstore.bookstore.entity.User;

import java.util.List;

public interface ReviewService {

    boolean canUserReviewBookInOrder(User user, Long bookId, Long orderId);

    Review createReview(User user, ReviewForm form);

    List<Review> getReviewsByBook(Book book);

    List<Order> getDeliveredOrdersForReview(User user);
}
