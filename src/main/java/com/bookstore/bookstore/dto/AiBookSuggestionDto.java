package com.bookstore.bookstore.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiBookSuggestionDto {

    private Long id;
    private String title;
    private String author;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String condition;
    private String conditionLabel;
    private String imageUrl;
    private String categoryName;
    private String shopName;
}
