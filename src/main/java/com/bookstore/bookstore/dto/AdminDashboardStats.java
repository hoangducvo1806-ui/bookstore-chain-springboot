package com.bookstore.bookstore.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardStats {
    private long totalUsers;
    private long totalShops;
    private long totalBooks;
    private long totalOrders;
    private BigDecimal totalDeliveredRevenue;
    private Map<String, Long> ordersByStatus;
}
