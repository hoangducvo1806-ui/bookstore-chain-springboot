package com.bookstore.bookstore.controller;

import com.bookstore.bookstore.entity.Book;
import com.bookstore.bookstore.entity.User;
import com.bookstore.bookstore.service.WishlistService;
import com.bookstore.bookstore.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/user/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public String viewWishlist(Model model, RedirectAttributes redirectAttributes) {
        User user = securityUtils.getCurrentUser().orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng đăng nhập để xem danh sách yêu thích!");
            return "redirect:/login";
        }

        List<Book> wishlistBooks = wishlistService.getWishlistBooks(user);
        model.addAttribute("wishlistBooks", wishlistBooks);

        return "user/wishlist";
    }

    @PostMapping("/toggle/{bookId}")
    public String toggleWishlistForm(@PathVariable("bookId") Long bookId,
                                     @RequestHeader(value = "Referer", required = false) String referer,
                                     RedirectAttributes redirectAttributes) {
        User user = securityUtils.getCurrentUser().orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng đăng nhập để lưu sản phẩm yêu thích!");
            return "redirect:/login";
        }

        boolean liked = wishlistService.toggleWishlist(user, bookId);
        if (liked) {
            redirectAttributes.addFlashAttribute("successMessage", "Đã thêm vào danh sách yêu thích!");
        } else {
            redirectAttributes.addFlashAttribute("infoMessage", "Đã xóa khỏi danh sách yêu thích!");
        }

        return referer != null ? "redirect:" + referer : "redirect:/books/" + bookId;
    }

    @PostMapping("/api/toggle/{bookId}")
    @ResponseBody
    public ResponseEntity<?> toggleWishlistApi(@PathVariable("bookId") Long bookId) {
        User user = securityUtils.getCurrentUser().orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Vui lòng đăng nhập"));
        }

        boolean liked = wishlistService.toggleWishlist(user, bookId);
        long count = wishlistService.countWishlist(user);
        return ResponseEntity.ok(Map.of("liked", liked, "count", count));
    }
}
