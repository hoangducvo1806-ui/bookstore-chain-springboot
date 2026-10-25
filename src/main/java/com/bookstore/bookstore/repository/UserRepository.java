package com.bookstore.bookstore.repository;

import com.bookstore.bookstore.entity.User;
import com.bookstore.bookstore.enums.RoleName;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("SELECT DISTINCT u FROM User u LEFT JOIN u.roles r WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR u.phone LIKE CONCAT('%', :keyword, '%')) AND " +
           "(:roleName IS NULL OR r.name = :roleName)")
    Page<User> searchUsers(@Param("keyword") String keyword, @Param("roleName") RoleName roleName, Pageable pageable);

    long countByRoles_Name(RoleName roleName);
}
