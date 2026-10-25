package com.bookstore.bookstore.controller;

import com.bookstore.bookstore.dto.ReviewForm;
import com.bookstore.bookstore.entity.Book;
import com.bookstore.bookstore.entity.User;
import com.bookstore.bookstore.service.BookService;
import com.bookstore.bookstore.service.ReviewService;
import com.bookstore.bookstore.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/user/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final BookService bookService;
    private final SecurityUtils securityUtils;

    @GetMapping("/write")
    public String writeReviewForm(@RequestParam("bookId") Long bookId,
                                  @RequestParam("orderId") Long orderId,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        User user = securityUtils.getCurrentUser().orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        if (!reviewService.canUserReviewBookInOrder(user, bookId, orderId)) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                    "Bạn không có quyền đánh giá sản phẩm này hoặc đơn hàng chưa hoàn thành/đã được đánh giá rồi!");
            return "redirect:/user/orders";
        }

        Book book = bookService.getBookById(bookId);
        
        ReviewForm form = ReviewForm.builder()
                .bookId(bookId)
                .orderId(orderId)
                .rating(5)
                .build();

        model.addAttribute("reviewForm", form);
        model.addAttribute("book", book);
        model.addAttribute("orderId", orderId);

        return "user/write-review";
    }

    @PostMapping("/submit")
    public String submitReview(@Valid @ModelAttribute("reviewForm") ReviewForm form,
                               BindingResult bindingResult,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        User user = securityUtils.getCurrentUser().orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            Book book = bookService.getBookById(form.getBookId());
            model.addAttribute("book", book);
            model.addAttribute("orderId", form.getOrderId());
            return "user/write-review";
        }

        try {
            reviewService.createReview(user, form);
            redirectAttributes.addFlashAttribute("successMessage", "Cảm ơn bạn đã gửi đánh giá chi tiết cho sản phẩm!");
            return "redirect:/books/" + form.getBookId();
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/user/reviews/write?bookId=" + form.getBookId() + "&orderId=" + form.getOrderId();
        }
    }
}
