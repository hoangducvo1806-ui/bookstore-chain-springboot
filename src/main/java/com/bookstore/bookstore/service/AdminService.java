package com.bookstore.bookstore.service;

import com.bookstore.bookstore.dto.AdminDashboardStats;
import com.bookstore.bookstore.dto.ShopRevenueDTO;
import com.bookstore.bookstore.entity.*;
import com.bookstore.bookstore.enums.OrderStatus;
import com.bookstore.bookstore.enums.RoleName;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public interface AdminService {

    // Thống kê Dashboard
    AdminDashboardStats getDashboardStats();
    List<Order> getRecentOrders();
    List<ShopRevenueDTO> getShopRevenueStats();

    // Quản lý User & Phân quyền
    Page<User> searchUsers(String keyword, RoleName roleName, int page, int size);
    User getUserById(Long id);
    void toggleUserLock(Long id);
    void updateUserRoles(Long id, Set<RoleName> roleNames);
    User createStaffAccount(String fullName, String email, String password, String phone, RoleName roleName);

    // Quản lý Danh mục sách
    List<Category> getAllCategories();
    Category getCategoryById(Long id);
    Category saveCategory(Category category);
    void toggleCategoryStatus(Long id);

    // Quản lý Chi nhánh Cửa hàng (Shop)
    Page<Shop> searchShops(String keyword, int page, int size);
    Shop getShopById(Long id);
    void toggleShopStatus(Long id);

    // Quản lý Đơn vị vận chuyển
    List<ShippingCarrier> getAllCarriers();
    ShippingCarrier getCarrierById(Long id);
    ShippingCarrier saveCarrier(ShippingCarrier carrier);
    void toggleCarrierStatus(Long id);

    // Quản lý Chiết khấu app cho shop
    List<AppCommission> getAllCommissions();
    AppCommission saveCommission(Long shopId, BigDecimal commissionRate, String note);

    // Quản lý Mã giảm giá (Voucher)
    List<Voucher> getAllVouchers();
    Voucher getVoucherById(Long id);
    Voucher saveVoucher(Voucher voucher);
    void toggleVoucherStatus(Long id);

    // Quản lý Sách của các shop (Duyệt/Gỡ sách vi phạm)
    Page<Book> searchBooks(String keyword, Long shopId, Long categoryId, int page, int size);
    void toggleBookStatus(Long id);

    // Quản lý Đơn hàng toàn chuỗi
    Page<Order> getOrders(OrderStatus status, int page, int size);
}
