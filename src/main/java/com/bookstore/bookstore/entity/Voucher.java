package com.bookstore.bookstore.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "vouchers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code; // Mã voucher (VD: SACHCU10, FREESHIP...)

    @Column(nullable = false, length = 150, columnDefinition = "NVARCHAR(150)")
    private String title;

    @Column(nullable = false, length = 30)
    private String discountType; // PERCENTAGE hoặc FIXED_AMOUNT hoặc FREE_SHIPPING

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal discountValue; // Giá trị giảm (VD: 10% hoặc 30000 VNĐ)

    @Column(precision = 12, scale = 2)
    private BigDecimal minOrderAmount; // Đơn tối thiểu áp dụng

    @Column(precision = 12, scale = 2)
    private BigDecimal maxDiscountAmount; // Giảm tối đa (đối với %)

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private Integer usageLimit; // Số lượt dùng tối đa

    @Builder.Default
    private Integer usedCount = 0;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;
}
