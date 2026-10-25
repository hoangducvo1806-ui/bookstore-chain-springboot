package com.bookstore.bookstore.service;

import com.bookstore.bookstore.entity.Book;
import com.bookstore.bookstore.entity.User;

import java.util.List;

public interface WishlistService {

    boolean toggleWishlist(User user, Long bookId);

    boolean isWishlisted(User user, Long bookId);

    List<Book> getWishlistBooks(User user);

    long countWishlist(User user);
}
