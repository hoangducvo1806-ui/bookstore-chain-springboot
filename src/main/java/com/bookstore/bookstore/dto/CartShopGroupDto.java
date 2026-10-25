package com.bookstore.bookstore.dto;

import com.bookstore.bookstore.entity.Book;
import com.bookstore.bookstore.entity.Shop;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartShopGroupDto {

    private Shop shop;

    @Builder.Default
    private List<CartItemViewDto> items = new ArrayList<>();

    public BigDecimal getShopSubtotal() {
        if (items == null) return BigDecimal.ZERO;
        return items.stream()
                .map(CartItemViewDto::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartItemViewDto {
        private Long id;
        private Book book;
        private Integer quantity;
        private BigDecimal price;
        private BigDecimal subtotal;
        private boolean outOfStock;
    }
}
