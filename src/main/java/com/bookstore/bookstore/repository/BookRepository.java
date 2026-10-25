package com.bookstore.bookstore.repository;

import com.bookstore.bookstore.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

    List<Book> findBySoldQuantityGreaterThanEqualAndActiveTrueOrderBySoldQuantityDesc(Integer soldQuantity);

    List<Book> findBySoldQuantityGreaterThanEqualOrderBySoldQuantityDesc(Integer soldQuantity);

    boolean existsByIsbn(String isbn);

    Optional<Book> findByIsbn(String isbn);

    Page<Book> findByActiveTrue(Pageable pageable);

    Page<Book> findByCategoryIdAndActiveTrue(Long categoryId, Pageable pageable);

    Page<Book> findByShopIdAndActiveTrue(Long shopId, Pageable pageable);

    List<Book> findTop8ByActiveTrueOrderByCreatedAtDesc();

    List<Book> findTop8ByActiveTrueOrderBySoldQuantityDesc();

    List<Book> findTop4ByCategoryIdAndActiveTrueAndIdNot(Long categoryId, Long bookId);

    @Query("SELECT b FROM Book b WHERE b.active = true " +
           "AND (:keyword IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(b.isbn) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:categoryId IS NULL OR b.category.id = :categoryId) " +
           "AND (:shopId IS NULL OR b.shop.id = :shopId)")
    Page<Book> searchBooks(@Param("keyword") String keyword,
                           @Param("categoryId") Long categoryId,
                           @Param("shopId") Long shopId,
                           Pageable pageable);
}
