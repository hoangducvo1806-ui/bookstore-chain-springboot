package com.bookstore.bookstore.controller;

import com.bookstore.bookstore.entity.Book;
import com.bookstore.bookstore.enums.BookCondition;
import com.bookstore.bookstore.repository.CategoryRepository;
import com.bookstore.bookstore.service.VendorBookService;
import com.bookstore.bookstore.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/vendor")
public class VendorController {

    private final VendorBookService vendorBookService;
    private final CategoryRepository categoryRepository;
    private final SecurityUtils securityUtils;

    @GetMapping("/books")
    public String books(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        var vendor = securityUtils.getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("Vui lòng đăng nhập."));

        var bookPage = vendorBookService.getMyBooks(vendor, keyword, Math.max(0, page));

        model.addAttribute("bookPage", bookPage);
        model.addAttribute("books", bookPage.getContent());
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentPage", bookPage.getNumber());
        model.addAttribute("totalPages", bookPage.getTotalPages());

        return "vendor/books";
    }

    @GetMapping("/books/new")
    public String newBook(Model model) {
        model.addAttribute("book", new Book());
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("conditions", BookCondition.values());
        model.addAttribute("categoryId", null);
        model.addAttribute("imageUrls", "");
        model.addAttribute("pageTitle", "Thêm sách mới");
        return "vendor/book-form";
    }

    @GetMapping("/books/{id}/edit")
    public String editBook(@PathVariable Long id, Model model) {
        var vendor = securityUtils.getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("Vui lòng đăng nhập."));
        Book book = vendorBookService.getMyBook(vendor, id);

        model.addAttribute("book", book);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("conditions", BookCondition.values());
        model.addAttribute("categoryId", book.getCategory().getId());
        model.addAttribute("imageUrls", book.getImages().stream()
                .map(img -> img.getImageUrl())
                .reduce((a, b) -> a + "\n" + b).orElse(""));
        model.addAttribute("pageTitle", "Chỉnh sửa sách");
        return "vendor/book-form";
    }

    @PostMapping("/books/save")
    public String saveBook(
            @ModelAttribute("book") Book book,
            @RequestParam Long categoryId,
            @RequestParam(required = false, defaultValue = "") String imageUrls,
            RedirectAttributes redirectAttributes) {

        try {
            var vendor = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new IllegalStateException("Vui lòng đăng nhập."));

            List<String> urls = Arrays.stream(imageUrls.split("[,\\r\\n]+"))
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .toList();

            vendorBookService.saveBook(vendor, book, categoryId, urls);
            redirectAttributes.addFlashAttribute("successMessage", "Đã lưu thông tin sách.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage() == null ? "Không thể lưu sách." : e.getMessage()
            );
            return book.getId() == null
                    ? "redirect:/vendor/books/new"
                    : "redirect:/vendor/books/" + book.getId() + "/edit";
        }

        return "redirect:/vendor/books";
    }

    @PostMapping("/books/{id}/toggle")
    public String toggleBook(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        try {
            var vendor = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new IllegalStateException("Vui lòng đăng nhập."));
            vendorBookService.toggleBook(vendor, id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật trạng thái sách.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể cập nhật trạng thái sách.");
        }
        return "redirect:/vendor/books";
    }
}