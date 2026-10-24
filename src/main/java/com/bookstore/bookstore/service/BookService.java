package com.bookstore.bookstore.service;

import com.bookstore.bookstore.dto.BookFilterDto;
import com.bookstore.bookstore.entity.Book;
import org.springframework.data.domain.Page;

import java.util.List;

public interface BookService {

    // Cho Guest: Hiển thị các sách đã bán trên 10 cuốn, sắp xếp giảm dần theo số lượng bán
    List<Book> getBooksForGuestHome();

    // Cho User: Các khối Sách mới, Bán chạy, Đánh giá cao, Được yêu thích
    List<Book> getNewestBooks();
    List<Book> getBestSellerBooks();
    List<Book> getTopRatedBooks();
    List<Book> getMostWishlistedBooks();

    // Trang sản phẩm theo danh mục (20 sp/trang)
    Page<Book> getBooksByCategorySlug(String slug, int page, int size, String sort);

    // Tìm kiếm và lọc sản phẩm đa tiêu chí
    Page<Book> filterBooks(BookFilterDto filterDto);

    // Chi tiết sách
    Book getBookById(Long id);

    // Sách liên quan (cùng danh mục)
    List<Book> getRelatedBooks(Book book);

    // Điểm đánh giá trung bình và số lượng review
    Double getAverageRating(Long bookId);
    Long getReviewCount(Long bookId);
}
