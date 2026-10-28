package com.bookstore.bookstore.controller;

import com.bookstore.bookstore.entity.Category;
import com.bookstore.bookstore.entity.Order;
import com.bookstore.bookstore.entity.User;
import com.bookstore.bookstore.entity.Voucher;
import com.bookstore.bookstore.enums.OrderStatus;
import com.bookstore.bookstore.enums.RoleName;
import com.bookstore.bookstore.repository.CategoryRepository;
import com.bookstore.bookstore.repository.VoucherRepository;
import com.bookstore.bookstore.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller chuyên biệt cho Role MANAGER (Quản lý vận hành kinh doanh & hàng hóa chuỗi).
 * Tách biệt hoàn toàn chức năng với ADMIN (Quản trị hệ thống, bảo mật & tài chính cấp cao).
 */
@Controller
@RequestMapping("/manager")
@RequiredArgsConstructor
public class ManagerController {

    private final AdminService adminService;
    private final CategoryRepository categoryRepository;
    private final VoucherRepository voucherRepository;

    // 1. TRUNG TÂM ĐIỀU HÀNH (OPERATIONS DASHBOARD)
    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(Model model) {
        model.addAttribute("stats", adminService.getDashboardStats());
        model.addAttribute("recentOrders", adminService.getRecentOrders());
        model.addAttribute("shopRevenues", adminService.getShopRevenueStats());
        model.addAttribute("totalCategories", categoryRepository.count());
        model.addAttribute("totalVouchers", voucherRepository.count());
        model.addAttribute("activeTab", "dashboard");
        return "manager/dashboard";
    }

    // 2. KIỂM DUYỆT KHO SÁCH CŨ (/manager/books)
    @GetMapping("/books")
    public String listBooks(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "shopId", required = false) Long shopId,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model
    ) {
        model.addAttribute("booksPage", adminService.searchBooks(keyword, shopId, categoryId, page, size));
        model.addAttribute("categories", adminService.getAllCategories());
        model.addAttribute("shops", adminService.searchShops(null, 0, 100).getContent());
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedShopId", shopId);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("activeTab", "books");
        return "admin/books";
    }

    @PostMapping("/books/{id}/toggle-status")
    public String toggleBookStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            adminService.toggleBookStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật trạng thái hiển thị sách cũ!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/manager/books";
    }

    // 3. ĐIỀU PHỐI ĐƠN HÀNG (/manager/orders)
    @GetMapping("/orders")
    public String listOrders(
            @RequestParam(value = "status", required = false) OrderStatus status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model
    ) {
        model.addAttribute("ordersPage", adminService.getOrders(status, page, size));
        model.addAttribute("selectedStatus", status);
        model.addAttribute("allStatuses", OrderStatus.values());
        model.addAttribute("activeTab", "orders");
        return "admin/orders";
    }

    // 4. QUẢN LÝ DANH MỤC SÁCH (/manager/categories)
    @GetMapping("/categories")
    public String listCategories(Model model) {
        model.addAttribute("categories", adminService.getAllCategories());
        model.addAttribute("activeTab", "categories");
        return "admin/categories";
    }

    @PostMapping("/categories/save")
    public String saveCategory(@ModelAttribute Category category, RedirectAttributes redirectAttributes) {
        try {
            adminService.saveCategory(category);
            redirectAttributes.addFlashAttribute("successMessage", "Lưu danh mục sách thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/manager/categories";
    }

    @PostMapping("/categories/{id}/toggle-status")
    public String toggleCategoryStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            adminService.toggleCategoryStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái danh mục thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/manager/categories";
    }

    // 5. CHIẾN DỊCH MÃ GIẢM GIÁ (/manager/vouchers)
    @GetMapping("/vouchers")
    public String listVouchers(Model model) {
        model.addAttribute("vouchers", adminService.getAllVouchers());
        model.addAttribute("activeTab", "vouchers");
        return "admin/vouchers";
    }

    @PostMapping("/vouchers/save")
    public String saveVoucher(@ModelAttribute Voucher voucher, RedirectAttributes redirectAttributes) {
        try {
            adminService.saveVoucher(voucher);
            redirectAttributes.addFlashAttribute("successMessage", "Lưu chiến dịch mã giảm giá thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/manager/vouchers";
    }

    @PostMapping("/vouchers/{id}/toggle-status")
    public String toggleVoucherStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            adminService.toggleVoucherStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái mã giảm giá thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/manager/vouchers";
    }

    // 6. GIÁM SÁT NHÂN SỰ VẬN HÀNH & SHIPPER (/manager/staff & /manager/reports)
    @GetMapping({"/staff", "/reports"})
    public String staffAndPerformance(Model model) {
        List<User> vendors = adminService.searchUsers(null, RoleName.VENDOR, 0, 50).getContent();
        List<User> shippers = adminService.searchUsers(null, RoleName.SHIPPER, 0, 50).getContent();
        List<Order> allOrders = adminService.getOrders(null, 0, 500).getContent();

        Map<Long, Long> shipperDeliveredCount = new HashMap<>();
        Map<Long, Long> shipperActiveCount = new HashMap<>();
        for (Order o : allOrders) {
            if (o.getShipper() != null) {
                Long sid = o.getShipper().getId();
                if (o.getStatus() == OrderStatus.DELIVERED) {
                    shipperDeliveredCount.put(sid, shipperDeliveredCount.getOrDefault(sid, 0L) + 1L);
                } else if (o.getStatus() == OrderStatus.SHIPPING || o.getStatus() == OrderStatus.PICKED_UP) {
                    shipperActiveCount.put(sid, shipperActiveCount.getOrDefault(sid, 0L) + 1L);
                }
            }
        }

        model.addAttribute("vendors", vendors);
        model.addAttribute("shippers", shippers);
        model.addAttribute("shopRevenues", adminService.getShopRevenueStats());
        model.addAttribute("shipperDeliveredCount", shipperDeliveredCount);
        model.addAttribute("shipperActiveCount", shipperActiveCount);
        model.addAttribute("activeTab", "staff");
        return "manager/staff";
    }
}
