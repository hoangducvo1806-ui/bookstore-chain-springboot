package com.bookstore.bookstore.repository;

import com.bookstore.bookstore.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    List<Book> findBySoldQuantityGreaterThanEqualOrderBySoldQuantityDesc(Integer soldQuantity);
    boolean existsByIsbn(String isbn);
}
