package com.bookstore.bookstore.controller;

import com.bookstore.bookstore.dto.BookFilterDto;
import com.bookstore.bookstore.entity.Book;
import com.bookstore.bookstore.entity.Category;
import com.bookstore.bookstore.entity.Review;
import com.bookstore.bookstore.entity.User;
import com.bookstore.bookstore.enums.BookCondition;
import com.bookstore.bookstore.repository.CategoryRepository;
import com.bookstore.bookstore.repository.ShopRepository;
import com.bookstore.bookstore.service.BookService;
import com.bookstore.bookstore.service.ReviewService;
import com.bookstore.bookstore.service.ViewedProductService;
import com.bookstore.bookstore.service.WishlistService;
import com.bookstore.bookstore.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;
    private final ReviewService reviewService;
    private final WishlistService wishlistService;
    private final ViewedProductService viewedProductService;
    private final CategoryRepository categoryRepository;
    private final ShopRepository shopRepository;
    private final SecurityUtils securityUtils;

    // Xem danh sách và tìm kiếm / lọc sản phẩm
    @GetMapping({"/books", "/search"})
    public String listBooks(@ModelAttribute("filter") BookFilterDto filter, Model model) {
        Page<Book> bookPage = bookService.filterBooks(filter);

        model.addAttribute("bookPage", bookPage);
        model.addAttribute("books", bookPage.getContent());
        model.addAttribute("currentPage", filter.getPage());
        model.addAttribute("totalPages", bookPage.getTotalPages());
        model.addAttribute("totalItems", bookPage.getTotalElements());

        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("shops", shopRepository.findAll());
        model.addAttribute("conditions", BookCondition.values());

        return "guest/books";
    }

    // C2. Xem sản phẩm theo danh mục (20 sp/trang)
    @GetMapping("/categories/{slug}")
    public String booksByCategory(@PathVariable("slug") String slug,
                                  @RequestParam(value = "page", defaultValue = "0") int page,
                                  @RequestParam(value = "sort", defaultValue = "newest") String sort,
                                  Model model) {
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục!"));

        Page<Book> bookPage = bookService.getBooksByCategorySlug(slug, page, 20, sort);

        model.addAttribute("category", category);
        model.addAttribute("bookPage", bookPage);
        model.addAttribute("books", bookPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", bookPage.getTotalPages());
        model.addAttribute("totalItems", bookPage.getTotalElements());
        model.addAttribute("currentSort", sort);

        return "guest/category-books";
    }

    // C3. Xem chi tiết sách cũ
    @GetMapping("/books/{id}")
    public String bookDetail(@PathVariable("id") Long id, Model model) {
        Book book = bookService.getBookById(id);
        User currentUser = securityUtils.getCurrentUser().orElse(null);

        // Lưu lịch sử xem nếu đã đăng nhập (C9)
        if (currentUser != null) {
            viewedProductService.recordViewedBook(currentUser, id);
        }

        // Kiểm tra đã thích chưa (C8)
        boolean isWishlisted = currentUser != null && wishlistService.isWishlisted(currentUser, id);

        // Đánh giá và bình luận (C3, C10)
        List<Review> reviews = reviewService.getReviewsByBook(book);
        Double avgRating = bookService.getAverageRating(id);
        Long reviewCount = bookService.getReviewCount(id);

        // Sách liên quan (cùng danh mục)
        List<Book> relatedBooks = bookService.getRelatedBooks(book);

        model.addAttribute("book", book);
        model.addAttribute("reviews", reviews);
        model.addAttribute("avgRating", avgRating);
        model.addAttribute("reviewCount", reviewCount);
        model.addAttribute("relatedBooks", relatedBooks);
        model.addAttribute("isWishlisted", isWishlisted);
        model.addAttribute("isAuthenticated", currentUser != null);

        return "guest/book-detail";
    }
}
