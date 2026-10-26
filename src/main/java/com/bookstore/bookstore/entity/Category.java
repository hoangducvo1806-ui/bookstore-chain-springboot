package com.bookstore.bookstore.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Nationalized
    @Column(nullable = false, unique = true, length = 100, columnDefinition = "NVARCHAR(100)")
    private String name;

    @Column(nullable = false, unique = true, length = 120)
    private String slug;

    @Nationalized
    @Column(length = 255, columnDefinition = "NVARCHAR(255)")
    private String description;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;
}
