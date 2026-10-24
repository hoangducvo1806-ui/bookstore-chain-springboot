package com.bookstore.bookstore.service;

import com.bookstore.bookstore.entity.Book;
import com.bookstore.bookstore.entity.User;

import java.util.List;

public interface ViewedProductService {

    void recordViewedBook(User user, Long bookId);

    List<Book> getViewedBooks(User user);
}
