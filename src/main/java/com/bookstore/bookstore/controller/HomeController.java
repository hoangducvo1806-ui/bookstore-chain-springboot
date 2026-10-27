package com.bookstore.bookstore.controller;

import com.bookstore.bookstore.entity.Book;
import com.bookstore.bookstore.entity.User;
import com.bookstore.bookstore.service.BookService;
import com.bookstore.bookstore.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final BookService bookService;
    private final SecurityUtils securityUtils;

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        boolean isAuthenticated = securityUtils.isAuthenticated();
        User currentUser = securityUtils.getCurrentUser().orElse(null);

        // B. Role Guest: hiển thị các sách đã bán trên 10 cuốn của các shop, sắp xếp giảm dần theo số lượng bán
        List<Book> guestHotBooks = bookService.getBooksForGuestHome();
        model.addAttribute("guestHotBooks", guestHotBooks);

        // C1. Role User: Các khối Sách mới, Bán chạy, Đánh giá cao, Được yêu thích
        if (isAuthenticated) {
            model.addAttribute("newestBooks", bookService.getNewestBooks());
            model.addAttribute("bestSellerBooks", bookService.getBestSellerBooks());
            model.addAttribute("topRatedBooks", bookService.getTopRatedBooks());
            model.addAttribute("mostWishlistedBooks", bookService.getMostWishlistedBooks());
            model.addAttribute("isUserLoggedIn", true);
            return "user/home";
        }

        model.addAttribute("isUserLoggedIn", false);
        return "guest/index";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "error/access-denied";
    }

    @GetMapping("/orders")
    public String ordersRedirect() {
        return "redirect:/user/orders";
    }
}
