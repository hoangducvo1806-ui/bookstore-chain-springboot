package com.bookstore.bookstore.service.impl;

import com.bookstore.bookstore.entity.Book;
import com.bookstore.bookstore.entity.User;
import com.bookstore.bookstore.entity.ViewedProduct;
import com.bookstore.bookstore.repository.BookRepository;
import com.bookstore.bookstore.repository.ViewedProductRepository;
import com.bookstore.bookstore.service.ViewedProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ViewedProductServiceImpl implements ViewedProductService {

    private final ViewedProductRepository viewedProductRepository;
    private final BookRepository bookRepository;

    @Override
    public void recordViewedBook(User user, Long bookId) {
        if (user == null || bookId == null) return;
        Book book = bookRepository.findById(bookId).orElse(null);
        if (book == null) return;

        Optional<ViewedProduct> existing = viewedProductRepository.findByUserAndBook(user, book);
        if (existing.isPresent()) {
            ViewedProduct vp = existing.get();
            vp.setViewedAt(LocalDateTime.now());
            viewedProductRepository.save(vp);
        } else {
            ViewedProduct vp = ViewedProduct.builder()
                    .user(user)
                    .book(book)
                    .viewedAt(LocalDateTime.now())
                    .build();
            viewedProductRepository.save(vp);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Book> getViewedBooks(User user) {
        if (user == null) return List.of();
        return viewedProductRepository.findByUserOrderByViewedAtDesc(user)
                .stream()
                .map(ViewedProduct::getBook)
                .collect(Collectors.toList());
    }
}
