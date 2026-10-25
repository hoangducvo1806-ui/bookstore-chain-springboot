package com.bookstore.bookstore.repository;

import com.bookstore.bookstore.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    List<Book> findBySoldQuantityGreaterThanEqualOrderBySoldQuantityDesc(Integer soldQuantity);
    boolean existsByIsbn(String isbn);
    long countByActive(Boolean active);

    @Query("SELECT b FROM Book b WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%')) OR b.isbn LIKE CONCAT('%', :keyword, '%')) AND " +
           "(:shopId IS NULL OR b.shop.id = :shopId) AND " +
           "(:categoryId IS NULL OR b.category.id = :categoryId)")
    Page<Book> searchBooks(@Param("keyword") String keyword,
                           @Param("shopId") Long shopId,
                           @Param("categoryId") Long categoryId,
                           Pageable pageable);
}
