package com.bookstore.bookstore.controller;

import com.bookstore.bookstore.entity.Category;
import com.bookstore.bookstore.entity.User;
import com.bookstore.bookstore.repository.CategoryRepository;
import com.bookstore.bookstore.service.CartService;
import com.bookstore.bookstore.service.WishlistService;
import com.bookstore.bookstore.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAttributes {

    private final SecurityUtils securityUtils;
    private final CartService cartService;
    private final WishlistService wishlistService;
    private final CategoryRepository categoryRepository;

    @ModelAttribute("currentUser")
    public User getCurrentUser() {
        return securityUtils.getCurrentUser().orElse(null);
    }

    @ModelAttribute("cartCount")
    public int getCartCount() {
        return securityUtils.getCurrentUser()
                .map(cartService::getTotalItemsCount)
                .orElse(0);
    }

    @ModelAttribute("wishlistCount")
    public long getWishlistCount() {
        return securityUtils.getCurrentUser()
                .map(wishlistService::countWishlist)
                .orElse(0L);
    }

    @ModelAttribute("allCategories")
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
}
