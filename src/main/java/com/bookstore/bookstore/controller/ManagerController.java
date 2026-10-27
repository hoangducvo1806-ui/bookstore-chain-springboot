package com.bookstore.bookstore.controller;

import com.bookstore.bookstore.repository.CategoryRepository;
import com.bookstore.bookstore.repository.VoucherRepository;
import com.bookstore.bookstore.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller chuyên biệt cho Role MANAGER (Quản lý vận hành chuỗi cửa hàng sách cũ).
 * Trọng tâm: Điều hành chi nhánh, kiểm duyệt sách cũ, điều phối đơn hàng và chương trình khuyến mãi.
 */
@Controller
@RequestMapping("/manager")
@RequiredArgsConstructor
public class ManagerController {

    private final AdminService adminService;
    private final CategoryRepository categoryRepository;
    private final VoucherRepository voucherRepository;

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

    @GetMapping("/shops")
    public String shops() {
        return "redirect:/admin/shops";
    }

    @GetMapping("/categories")
    public String categories() {
        return "redirect:/admin/categories";
    }

    @GetMapping("/shipping")
    public String shipping() {
        return "redirect:/admin/shipping";
    }

    @GetMapping("/commissions")
    public String commissions() {
        return "redirect:/admin/commissions";
    }

    @GetMapping("/vouchers")
    public String vouchers() {
        return "redirect:/admin/vouchers";
    }

    @GetMapping("/books")
    public String books() {
        return "redirect:/admin/books";
    }

    @GetMapping("/orders")
    public String orders() {
        return "redirect:/admin/orders";
    }
}
