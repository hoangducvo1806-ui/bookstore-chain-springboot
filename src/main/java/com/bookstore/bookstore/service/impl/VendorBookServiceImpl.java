package com.bookstore.bookstore.service.impl;

import com.bookstore.bookstore.entity.*;
import com.bookstore.bookstore.repository.*;
import com.bookstore.bookstore.service.CloudinaryService;
import com.bookstore.bookstore.service.VendorBookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class VendorBookServiceImpl implements VendorBookService {

    private final BookRepository bookRepository;
    private final ShopRepository shopRepository;
    private final CategoryRepository categoryRepository;
    private final CloudinaryService cloudinaryService;

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
            var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
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
    public Book saveBook(User vendor, Book input, Long categoryId, List<String> imageUrls,
                         MultipartFile mainImageFile, List<MultipartFile> imageFiles) {
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
            book.setSoldQuantity(0);
        }

        book.setCategory(category);
        book.setTitle(input.getTitle().trim());
        book.setIsbn(input.getIsbn() != null ? input.getIsbn().trim() : null);
        book.setAuthor(input.getAuthor().trim());
        book.setPublisher(input.getPublisher());
        book.setPublishYear(input.getPublishYear());
        book.setBookCondition(input.getBookCondition());
        book.setConditionNote(input.getConditionNote());
        book.setOriginalPrice(input.getOriginalPrice());
        book.setPrice(input.getPrice());
        book.setPromotionalPrice(input.getPromotionalPrice());
        book.setStock(input.getStock() != null && input.getStock() >= 0 ? input.getStock() : 1);
        book.setDescription(input.getDescription());

        // 1. Upload ảnh bìa chính lên Cloudinary nếu có file
        if (mainImageFile != null && !mainImageFile.isEmpty()) {
            try {
                String uploadedMainUrl = cloudinaryService.uploadImage(mainImageFile, "books/main");
                book.setMainImageUrl(uploadedMainUrl);
            } catch (Exception e) {
                log.warn("Lỗi upload ảnh bìa chính lên Cloudinary: {}", e.getMessage());
            }
        } else if (input.getMainImageUrl() != null && !input.getMainImageUrl().isBlank()) {
            book.setMainImageUrl(input.getMainImageUrl().trim());
        }

        // 2. Upload các ảnh bổ sung từ file
        List<String> finalUrls = new ArrayList<>();
        if (imageUrls != null) {
            finalUrls.addAll(imageUrls);
        }

        if (imageFiles != null && !imageFiles.isEmpty()) {
            try {
                List<String> uploadedMoreUrls = cloudinaryService.uploadMultipleImages(imageFiles, "books/gallery");
                finalUrls.addAll(uploadedMoreUrls);
            } catch (Exception e) {
                log.warn("Lỗi upload ảnh thư viện lên Cloudinary: {}", e.getMessage());
            }
        }

        if (!finalUrls.isEmpty()) {
            book.getImages().clear();
            for (String url : finalUrls) {
                if (url != null && !url.isBlank()) {
                    BookImage image = BookImage.builder()
                        .book(book)
                        .imageUrl(url.trim())
                        .build();
                    book.getImages().add(image);
                }
            }
            if (book.getMainImageUrl() == null || book.getMainImageUrl().isBlank()) {
                book.setMainImageUrl(finalUrls.get(0));
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

    @Override
    public void deleteBook(User vendor, Long bookId) {
        Book book = getMyBook(vendor, bookId);
        bookRepository.delete(book);
    }
}
