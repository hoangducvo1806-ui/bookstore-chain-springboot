package com.bookstore.bookstore.service;

import com.bookstore.bookstore.dto.CartShopGroupDto;
import com.bookstore.bookstore.entity.Cart;
import com.bookstore.bookstore.entity.User;

import java.math.BigDecimal;
import java.util.List;

public interface CartService {

    Cart getOrCreateCart(User user);

    List<CartShopGroupDto> getCartGroupedByShop(User user);

    void addToCart(User user, Long bookId, int quantity);

    void updateQuantity(User user, Long cartItemId, int quantity);

    void removeFromCart(User user, Long cartItemId);

    void clearCart(User user);

    int getTotalItemsCount(User user);

    BigDecimal getCartSubtotal(User user);
}
