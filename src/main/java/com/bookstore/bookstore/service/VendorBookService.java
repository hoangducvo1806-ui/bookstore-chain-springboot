package com.bookstore.bookstore.service;

import com.bookstore.bookstore.entity.Book;
import com.bookstore.bookstore.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface VendorBookService {
    Page<Book> getMyBooks(User vendor, String keyword, int page);
    Book getMyBook(User vendor, Long bookId);
    Book saveBook(User vendor, Book book, Long categoryId, List<String> imageUrls, MultipartFile mainImageFile, List<MultipartFile> imageFiles);
    void toggleBook(User vendor, Long bookId);
    void deleteBook(User vendor, Long bookId);
}
