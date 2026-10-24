package com.bookstore.bookstore.controller;

import com.bookstore.bookstore.entity.Book;
import com.bookstore.bookstore.entity.User;
import com.bookstore.bookstore.service.ViewedProductService;
import com.bookstore.bookstore.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/user/viewed-books")
@RequiredArgsConstructor
public class ViewedProductController {

    private final ViewedProductService viewedProductService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public String listViewedBooks(Model model, RedirectAttributes redirectAttributes) {
        User user = securityUtils.getCurrentUser().orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng đăng nhập để xem lịch sử đã xem!");
            return "redirect:/login";
        }

        List<Book> viewedBooks = viewedProductService.getViewedBooks(user);
        model.addAttribute("viewedBooks", viewedBooks);

        return "user/viewed-books";
    }
}
