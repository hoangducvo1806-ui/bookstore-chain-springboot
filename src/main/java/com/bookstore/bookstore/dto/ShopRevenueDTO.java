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
public class ShopRevenueDTO {
    private Long shopId;
    private String shopName;
    private String ownerName;
    private long totalOrders;
    private long deliveredOrders;
    private BigDecimal deliveredRevenue;
}
