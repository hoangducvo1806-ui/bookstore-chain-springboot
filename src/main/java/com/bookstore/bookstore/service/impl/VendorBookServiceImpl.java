package com.bookstore.bookstore.service.impl;

import com.bookstore.bookstore.entity.*;
import com.bookstore.bookstore.repository.*;
import com.bookstore.bookstore.service.VendorBookService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class VendorBookServiceImpl implements VendorBookService {

    private final BookRepository bookRepository;
    private final ShopRepository shopRepository;
    private final CategoryRepository categoryRepository;

    private Shop getShop(User vendor) {
        return shopRepository.findByOwner(vendor)
            .orElseThrow(() -> new IllegalArgumentException("Bạn chưa đăng ký cửa hàng."));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Book> getMyBooks(User vendor, String keyword, int page) {
        Shop shop = getShop(vendor);
        Pageable pageable = PageRequest.of(
            Math.max(page, 0), 10, Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return bookRepository.findAll((root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            predicates.add(cb.equal(root.get("shop").get("id"), shop.getId()));

            if (keyword != null && !keyword.isBlank()) {
                String kw = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("title")), kw),
                    cb.like(cb.lower(root.get("author")), kw),
                    cb.like(cb.lower(root.get("isbn")), kw)
                ));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        }, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Book getMyBook(User vendor, Long bookId) {
        Shop shop = getShop(vendor);
        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sách."));

        if (!book.getShop().getId().equals(shop.getId())) {
            throw new org.springframework.security.access.AccessDeniedException(
                "Bạn không có quyền truy cập sách của cửa hàng khác."
            );
        }
        return book;
    }

    @Override
    public Book saveBook(User vendor, Book input, Long categoryId, List<String> imageUrls) {
        Shop shop = getShop(vendor);
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new IllegalArgumentException("Danh mục không tồn tại."));

        Book book;
        if (input.getId() != null) {
            book = getMyBook(vendor, input.getId());
        } else {
            book = new Book();
            book.setShop(shop);
            book.setActive(true);
            book.setStock(1);
            book.setSoldQuantity(0);
        }

        book.setCategory(category);
        book.setTitle(input.getTitle());
        book.setIsbn(input.getIsbn());
        book.setAuthor(input.getAuthor());
        book.setPublisher(input.getPublisher());
        book.setPublishYear(input.getPublishYear());
        book.setBookCondition(input.getBookCondition());
        book.setConditionNote(input.getConditionNote());
        book.setOriginalPrice(input.getOriginalPrice());
        book.setPrice(input.getPrice());
        book.setStock(input.getStock());
        book.setDescription(input.getDescription());

        if (imageUrls != null && !imageUrls.isEmpty()) {
            book.setMainImageUrl(imageUrls.get(0));
            book.getImages().clear();

            for (String url : imageUrls) {
                BookImage image = BookImage.builder()
                    .book(book)
                    .imageUrl(url)
                    .build();
                book.getImages().add(image);
            }
        }

        return bookRepository.save(book);
    }

    @Override
    public void toggleBook(User vendor, Long bookId) {
        Book book = getMyBook(vendor, bookId);
        book.setActive(!Boolean.TRUE.equals(book.getActive()));
        bookRepository.save(book);
    }
}
