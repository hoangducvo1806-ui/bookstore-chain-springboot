package com.bookstore.bookstore.service.impl;

import com.bookstore.bookstore.entity.Book;
import com.bookstore.bookstore.entity.User;
import com.bookstore.bookstore.entity.Wishlist;
import com.bookstore.bookstore.repository.BookRepository;
import com.bookstore.bookstore.repository.WishlistRepository;
import com.bookstore.bookstore.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final BookRepository bookRepository;

    @Override
    public boolean toggleWishlist(User user, Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sách!"));

        Optional<Wishlist> existing = wishlistRepository.findByUserAndBook(user, book);
        if (existing.isPresent()) {
            wishlistRepository.delete(existing.get());
            return false; // Đã bỏ thích
        } else {
            Wishlist wl = Wishlist.builder()
                    .user(user)
                    .book(book)
                    .build();
            wishlistRepository.save(wl);
            return true; // Đã thêm thích
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isWishlisted(User user, Long bookId) {
        if (user == null || bookId == null) return false;
        Book book = bookRepository.findById(bookId).orElse(null);
        if (book == null) return false;
        return wishlistRepository.existsByUserAndBook(user, book);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Book> getWishlistBooks(User user) {
        return wishlistRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(Wishlist::getBook)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long countWishlist(User user) {
        if (user == null) return 0;
        return wishlistRepository.countByUser(user);
    }
}
