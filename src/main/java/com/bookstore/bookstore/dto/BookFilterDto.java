package com.bookstore.bookstore.dto;

import com.bookstore.bookstore.enums.BookCondition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookFilterDto {

    private String keyword;
    private String author;
    private String isbn;
    private Long categoryId;
    private Long shopId;
    private BookCondition condition;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String sort; // "newest", "price_asc", "price_desc", "best_seller"

    @Builder.Default
    private int page = 0;

    @Builder.Default
    private int size = 20; // 20 sản phẩm mỗi trang theo C2
}
