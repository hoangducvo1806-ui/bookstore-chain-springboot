package com.bookstore.bookstore.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDateTime;

@Entity
@Table(name = "shops")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false, unique = true)
    private User owner; // Chủ chi nhánh (Role VENDOR)

    @Nationalized
    @Column(nullable = false, length = 150, columnDefinition = "NVARCHAR(150)")
    private String name;

    @Nationalized
    @Column(length = 1000, columnDefinition = "NVARCHAR(1000)")
    private String description;

    @Nationalized
    @Column(length = 500, columnDefinition = "NVARCHAR(500)")
    private String bio; // Tiểu sử ngắn của cửa hàng

    @Column(length = 500)
    private String logoUrl;

    @Column(length = 500)
    private String bannerUrl;

    @Nationalized
    @Column(nullable = false, length = 255, columnDefinition = "NVARCHAR(255)")
    private String address;

    @Column(length = 20)
    private String phone;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
