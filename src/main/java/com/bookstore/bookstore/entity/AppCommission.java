package com.bookstore.bookstore.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "app_commissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppCommission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false, unique = true)
    private Shop shop; // Cửa hàng chi nhánh chịu mức chiết khấu

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal commissionRate; // % chiết khấu app thu (VD: 5.00%, 10.00%)

    @Column(length = 255, columnDefinition = "NVARCHAR(255)")
    private String note;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
