package com.bookstore.bookstore.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "review_media")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @Column(nullable = false, length = 500)
    private String mediaUrl; // URL trên Cloudinary

    @Column(nullable = false, length = 20)
    private String mediaType; // IMAGE hoặc VIDEO
}
