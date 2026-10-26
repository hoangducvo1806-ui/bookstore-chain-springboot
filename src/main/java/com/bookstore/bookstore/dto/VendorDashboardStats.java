package com.bookstore.bookstore.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorDashboardStats {
    private String shopName;
    private String shopAddress;
    private String shopPhone;
    private BigDecimal totalDeliveredRevenue;
    private BigDecimal totalRevenue;
    private long totalOrders;
    private long newOrdersCount;
    private long processingOrdersCount;
    private long shippingOrdersCount;
    private long deliveredOrdersCount;
    private long cancelledOrdersCount;
    private long totalBooks;
    private long lowStockBooks;
    private Map<String, Long> ordersByStatus;
}
