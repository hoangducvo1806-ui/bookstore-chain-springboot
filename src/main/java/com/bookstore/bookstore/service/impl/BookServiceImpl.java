package com.bookstore.bookstore.service.impl;

import com.bookstore.bookstore.dto.BookFilterDto;
import com.bookstore.bookstore.entity.Book;
import com.bookstore.bookstore.entity.Category;
import com.bookstore.bookstore.repository.BookRepository;
import com.bookstore.bookstore.repository.CategoryRepository;
import com.bookstore.bookstore.repository.ReviewRepository;
import com.bookstore.bookstore.service.BookService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final ReviewRepository reviewRepository;

    @Override
    public List<Book> getBooksForGuestHome() {
        // Yêu cầu: sách đã bán trên 10 cuốn của các shop, sắp xếp theo số lượng bán giảm dần
        List<Book> books = bookRepository.findBySoldQuantityGreaterThanEqualAndActiveTrueOrderBySoldQuantityDesc(10);
        if (books.isEmpty()) {
            // Fallback nếu chưa có sách bán > 10
            return bookRepository.findTop8ByActiveTrueOrderBySoldQuantityDesc();
        }
        return books;
    }

    @Override
    public List<Book> getNewestBooks() {
        return bookRepository.findTop8ByActiveTrueOrderByCreatedAtDesc();
    }

    @Override
    public List<Book> getBestSellerBooks() {
        return bookRepository.findTop8ByActiveTrueOrderBySoldQuantityDesc();
    }

    @Override
    public List<Book> getTopRatedBooks() {
        // Tạm thời lấy danh sách sách hoạt động, sắp xếp theo giá hoặc ID
        return bookRepository.findTop8ByActiveTrueOrderBySoldQuantityDesc();
    }

    @Override
    public List<Book> getMostWishlistedBooks() {
        return bookRepository.findTop8ByActiveTrueOrderByCreatedAtDesc();
    }

    @Override
    public Page<Book> getBooksByCategorySlug(String slug, int page, int size, String sort) {
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục: " + slug));

        Sort sortObj = parseSort(sort);
        Pageable pageable = PageRequest.of(Math.max(0, page), size > 0 ? size : 20, sortObj);
        return bookRepository.findByCategoryIdAndActiveTrue(category.getId(), pageable);
    }

    @Override
    public Page<Book> filterBooks(BookFilterDto filterDto) {
        Sort sortObj = parseSort(filterDto.getSort());
        int size = filterDto.getSize() > 0 ? filterDto.getSize() : 20;
        Pageable pageable = PageRequest.of(Math.max(0, filterDto.getPage()), size, sortObj);

        Specification<Book> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isTrue(root.get("active")));

            if (filterDto.getKeyword() != null && !filterDto.getKeyword().trim().isEmpty()) {
                String kw = "%" + filterDto.getKeyword().trim().toLowerCase() + "%";
                Predicate titlePred = cb.like(cb.lower(root.get("title")), kw);
                Predicate authorPred = cb.like(cb.lower(root.get("author")), kw);
                Predicate isbnPred = cb.like(cb.lower(root.get("isbn")), kw);
                predicates.add(cb.or(titlePred, authorPred, isbnPred));
            }

            if (filterDto.getAuthor() != null && !filterDto.getAuthor().trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("author")), "%" + filterDto.getAuthor().trim().toLowerCase() + "%"));
            }

            if (filterDto.getIsbn() != null && !filterDto.getIsbn().trim().isEmpty()) {
                predicates.add(cb.equal(root.get("isbn"), filterDto.getIsbn().trim()));
            }

            if (filterDto.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("category").get("id"), filterDto.getCategoryId()));
            }

            if (filterDto.getShopId() != null) {
                predicates.add(cb.equal(root.get("shop").get("id"), filterDto.getShopId()));
            }

            if (filterDto.getCondition() != null) {
                predicates.add(cb.equal(root.get("bookCondition"), filterDto.getCondition()));
            }

            if (filterDto.getMinPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), filterDto.getMinPrice()));
            }

            if (filterDto.getMaxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), filterDto.getMaxPrice()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return bookRepository.findAll(spec, pageable);
    }

    @Override
    public Book getBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sách với ID: " + id));
    }

    @Override
    public List<Book> getRelatedBooks(Book book) {
        if (book.getCategory() == null) return List.of();
        return bookRepository.findTop4ByCategoryIdAndActiveTrueAndIdNot(book.getCategory().getId(), book.getId());
    }

    @Override
    public Double getAverageRating(Long bookId) {
        Double avg = reviewRepository.getAverageRatingForBook(bookId);
        return avg != null ? Math.round(avg * 10.0) / 10.0 : 5.0;
    }

    @Override
    public Long getReviewCount(Long bookId) {
        return reviewRepository.countReviewsForBook(bookId);
    }

    private Sort parseSort(String sort) {
        if (sort == null) return Sort.by(Sort.Direction.DESC, "createdAt");
        return switch (sort.toLowerCase()) {
            case "price_asc" -> Sort.by(Sort.Direction.ASC, "price");
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "price");
            case "best_seller" -> Sort.by(Sort.Direction.DESC, "soldQuantity");
            case "oldest" -> Sort.by(Sort.Direction.ASC, "createdAt");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }
}
