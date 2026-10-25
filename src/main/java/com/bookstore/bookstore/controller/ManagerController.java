package com.bookstore.bookstore.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller cho Role MANAGER (Quản lý chuỗi cửa hàng).
 * Sử dụng chung các view quản trị với Admin theo đúng mô hình 3 tầng (tránh trùng lặp mã nguồn).
 */
@Controller
@RequestMapping("/manager")
public class ManagerController {

    @GetMapping({"", "/", "/dashboard"})
    public String dashboard() {
        return "redirect:/admin/dashboard";
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
