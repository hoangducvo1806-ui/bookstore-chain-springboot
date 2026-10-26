package com.bookstore.bookstore.repository;

import com.bookstore.bookstore.entity.Order;
import com.bookstore.bookstore.entity.Shop;
import com.bookstore.bookstore.entity.User;
import com.bookstore.bookstore.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Admin & Manager Queries
    List<Order> findTop10ByOrderByCreatedAtDesc();

    Page<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status, Pageable pageable);

    long countByStatus(OrderStatus status);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = com.bookstore.bookstore.enums.OrderStatus.DELIVERED")
    BigDecimal calculateTotalDeliveredRevenue();

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o")
    BigDecimal calculateTotalRevenue();

    // Customer Queries (TV1)
    List<Order> findByCustomerOrderByCreatedAtDesc(User customer);

    List<Order> findByCustomerAndStatusOrderByCreatedAtDesc(User customer, OrderStatus status);

    List<Order> findByCustomerAndStatusInOrderByCreatedAtDesc(User customer, List<OrderStatus> statuses);

    Optional<Order> findByOrderCode(String orderCode);

    Optional<Order> findByOrderCodeAndCustomer(String orderCode, User customer);

    // Vendor / Shop Queries (TV2)
    List<Order> findTop5ByShopOrderByCreatedAtDesc(Shop shop);

    List<Order> findByShopOrderByCreatedAtDesc(Shop shop);

    List<Order> findByShopAndStatusOrderByCreatedAtDesc(Shop shop, OrderStatus status);

    Page<Order> findByShopOrderByCreatedAtDesc(Shop shop, Pageable pageable);

    Page<Order> findByShopAndStatusOrderByCreatedAtDesc(Shop shop, OrderStatus status, Pageable pageable);

    Page<Order> findByShopAndStatusInOrderByCreatedAtDesc(Shop shop, List<OrderStatus> statuses, Pageable pageable);

    long countByShop(Shop shop);

    long countByShopAndStatus(Shop shop, OrderStatus status);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.shop = :shop AND o.status = com.bookstore.bookstore.enums.OrderStatus.DELIVERED")
    BigDecimal calculateShopDeliveredRevenue(@Param("shop") Shop shop);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.shop = :shop AND o.status = com.bookstore.bookstore.enums.OrderStatus.DELIVERED")
    BigDecimal calculateDeliveredRevenueByShop(@Param("shop") Shop shop);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.shop = :shop")
    BigDecimal calculateShopTotalRevenue(@Param("shop") Shop shop);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.shop = :shop")
    BigDecimal calculateTotalRevenueByShop(@Param("shop") Shop shop);

    // Shipper Queries (TV2)
    List<Order> findByShipperOrderByCreatedAtDesc(User shipper);

    List<Order> findByStatusInOrderByCreatedAtDesc(List<OrderStatus> statuses);

    @Query("SELECT o FROM Order o WHERE o.shipper = :shipper OR (o.shipper IS NULL AND o.status IN :statuses) ORDER BY o.createdAt DESC")
    List<Order> findAvailableForShipper(@Param("shipper") User shipper, @Param("statuses") List<OrderStatus> statuses);

    Page<Order> findByShipperOrderByCreatedAtDesc(User shipper, Pageable pageable);

    Page<Order> findByShipperAndStatusOrderByCreatedAtDesc(User shipper, OrderStatus status, Pageable pageable);

    Page<Order> findByStatusAndShipperIsNullOrderByCreatedAtDesc(OrderStatus status, Pageable pageable);

    long countByShipperAndStatus(User shipper, OrderStatus status);
}
