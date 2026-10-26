package com.bookstore.bookstore.entity;

import com.bookstore.bookstore.enums.BookCondition;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Nationalized;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "books")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop; // Cửa hàng/chi nhánh bán cuốn sách này

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Nationalized
    @Column(nullable = false, length = 255, columnDefinition = "NVARCHAR(255)")
    private String title;

    @Column(length = 50)
    private String isbn;

    @Nationalized
    @Column(nullable = false, length = 150, columnDefinition = "NVARCHAR(150)")
    private String author;

    @Nationalized
    @Column(length = 150, columnDefinition = "NVARCHAR(150)")
    private String publisher;

    private Integer publishYear;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BookCondition bookCondition; // Tình trạng sách cũ (Như mới, 90%, 75%...)

    @Nationalized
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String conditionNote; // Ghi chú chi tiết tình trạng: có ố vàng nhẹ, không rách trang...

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal originalPrice; // Giá bìa gốc

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price; // Giá bán sách cũ

    @Column(precision = 12, scale = 2)
    private BigDecimal promotionalPrice; // Giá khuyến mãi (nếu có)

    @Builder.Default
    @Column(nullable = false)
    private Integer stock = 1; // Số lượng tồn kho (quantity)

    @Builder.Default
    @Column(nullable = false)
    private Integer soldQuantity = 0; // Đã bán

    @Nationalized
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(length = 500)
    private String mainImageUrl;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BookImage> images = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
