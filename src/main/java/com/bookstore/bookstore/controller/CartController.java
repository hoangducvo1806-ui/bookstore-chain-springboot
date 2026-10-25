package com.bookstore.bookstore.controller;

import com.bookstore.bookstore.dto.CartShopGroupDto;
import com.bookstore.bookstore.entity.User;
import com.bookstore.bookstore.service.CartService;
import com.bookstore.bookstore.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public String viewCart(Model model, RedirectAttributes redirectAttributes) {
        User user = securityUtils.getCurrentUser().orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng đăng nhập để xem giỏ hàng của bạn!");
            return "redirect:/login";
        }

        List<CartShopGroupDto> shopGroups = cartService.getCartGroupedByShop(user);
        BigDecimal subtotal = cartService.getCartSubtotal(user);

        model.addAttribute("shopGroups", shopGroups);
        model.addAttribute("cartSubtotal", subtotal);
        return "user/cart";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam("bookId") Long bookId,
                            @RequestParam(value = "quantity", defaultValue = "1") int quantity,
                            @RequestParam(value = "buyNow", defaultValue = "false") boolean buyNow,
                            RedirectAttributes redirectAttributes) {
        User user = securityUtils.getCurrentUser().orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng đăng nhập tài khoản để thực hiện mua sách!");
            return "redirect:/login";
        }

        try {
            cartService.addToCart(user, bookId, quantity);
            if (buyNow) {
                return "redirect:/checkout";
            }
            redirectAttributes.addFlashAttribute("successMessage", "Đã thêm sách vào giỏ hàng thành công!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/books/" + bookId;
    }

    @PostMapping("/update")
    public String updateQuantity(@RequestParam("cartItemId") Long cartItemId,
                                 @RequestParam("quantity") int quantity,
                                 RedirectAttributes redirectAttributes) {
        User user = securityUtils.getCurrentUser().orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        try {
            cartService.updateQuantity(user, cartItemId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật số lượng thành công!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/cart";
    }

    @PostMapping("/remove/{id}")
    public String removeItem(@PathVariable("id") Long cartItemId,
                             RedirectAttributes redirectAttributes) {
        User user = securityUtils.getCurrentUser().orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        try {
            cartService.removeFromCart(user, cartItemId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa sách khỏi giỏ hàng!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/cart";
    }
}
