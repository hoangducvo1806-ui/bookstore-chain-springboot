package com.bookstore.bookstore.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;

@Entity
@Table(name = "shipping_carriers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingCarrier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Nationalized
    @Column(nullable = false, unique = true, length = 100, columnDefinition = "NVARCHAR(100)")
    private String name; // Giao Hàng Nhanh, Viettel Post, Giao Hàng Tiết Kiệm...

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal shippingFee; // Cước phí vận chuyển cơ bản

    @Nationalized
    @Column(length = 255, columnDefinition = "NVARCHAR(255)")
    private String description;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;
}
