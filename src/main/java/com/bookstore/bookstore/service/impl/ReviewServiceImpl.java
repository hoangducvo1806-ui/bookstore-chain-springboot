package com.bookstore.bookstore.service.impl;

import com.bookstore.bookstore.dto.ReviewForm;
import com.bookstore.bookstore.entity.*;
import com.bookstore.bookstore.enums.OrderStatus;
import com.bookstore.bookstore.repository.BookRepository;
import com.bookstore.bookstore.repository.OrderRepository;
import com.bookstore.bookstore.repository.ReviewMediaRepository;
import com.bookstore.bookstore.repository.ReviewRepository;
import com.bookstore.bookstore.service.CloudinaryService;
import com.bookstore.bookstore.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewMediaRepository reviewMediaRepository;
    private final BookRepository bookRepository;
    private final OrderRepository orderRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional(readOnly = true)
    public boolean canUserReviewBookInOrder(User user, Long bookId, Long orderId) {
        if (user == null || bookId == null || orderId == null) return false;

        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null || !order.getCustomer().getId().equals(user.getId())) return false;
        if (order.getStatus() != OrderStatus.DELIVERED) return false;

        boolean bookInOrder = order.getItems().stream()
                .anyMatch(item -> item.getBook().getId().equals(bookId));
        if (!bookInOrder) return false;

        Book book = bookRepository.findById(bookId).orElse(null);
        if (book == null) return false;

        return !reviewRepository.existsByBookAndOrderAndUser(book, order, user);
    }

    @Override
    public Review createReview(User user, ReviewForm form) {
        if (!canUserReviewBookInOrder(user, form.getBookId(), form.getOrderId())) {
            throw new IllegalArgumentException("Bạn chỉ được đánh giá sản phẩm sau khi đơn hàng đã giao thành công và mỗi cuốn sách chỉ đánh giá 1 lần!");
        }

        if (form.getComment() == null || form.getComment().trim().length() < 50) {
            throw new IllegalArgumentException("Nội dung đánh giá phải có độ dài tối thiểu 50 ký tự!");
        }

        Book book = bookRepository.findById(form.getBookId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sách!"));
        Order order = orderRepository.findById(form.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng!"));

        Review review = Review.builder()
                .user(user)
                .book(book)
                .order(order)
                .rating(form.getRating())
                .comment(form.getComment().trim())
                .mediaList(new ArrayList<>())
                .build();

        review = reviewRepository.save(review);

        // Upload ảnh lên Cloudinary
        if (form.getImageFiles() != null && !form.getImageFiles().isEmpty()) {
            for (MultipartFile file : form.getImageFiles()) {
                if (file != null && !file.isEmpty()) {
                    try {
                        String imageUrl = cloudinaryService.uploadImage(file, "reviews");
                        ReviewMedia media = ReviewMedia.builder()
                                .review(review)
                                .mediaUrl(imageUrl)
                                .mediaType("IMAGE")
                                .build();
                        reviewMediaRepository.save(media);
                    } catch (Exception ex) {
                        log.warn("Không thể tải ảnh đánh giá lên Cloudinary: {}", ex.getMessage());
                    }
                }
            }
        }

        // Upload video lên Cloudinary nếu có
        if (form.getVideoFile() != null && !form.getVideoFile().isEmpty()) {
            try {
                String videoUrl = cloudinaryService.uploadVideo(form.getVideoFile(), "reviews");
                ReviewMedia media = ReviewMedia.builder()
                        .review(review)
                        .mediaUrl(videoUrl)
                        .mediaType("VIDEO")
                        .build();
                reviewMediaRepository.save(media);
            } catch (Exception ex) {
                log.warn("Không thể tải video đánh giá lên Cloudinary: {}", ex.getMessage());
            }
        }

        return review;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Review> getReviewsByBook(Book book) {
        return reviewRepository.findByBookOrderByCreatedAtDesc(book);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getDeliveredOrdersForReview(User user) {
        return orderRepository.findByCustomerAndStatusOrderByCreatedAtDesc(user, OrderStatus.DELIVERED);
    }
}
