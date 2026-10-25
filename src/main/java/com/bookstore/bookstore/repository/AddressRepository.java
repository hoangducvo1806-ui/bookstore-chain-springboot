package com.bookstore.bookstore.repository;

import com.bookstore.bookstore.entity.Address;
import com.bookstore.bookstore.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUserOrderByIdDesc(User user);
    Optional<Address> findByUserAndIsDefaultTrue(User user);

    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.user = :user")
    void resetDefaultAddressForUser(User user);
}
