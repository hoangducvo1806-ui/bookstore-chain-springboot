package com.bookstore.bookstore.controller;

import com.bookstore.bookstore.entity.Category;
import com.bookstore.bookstore.entity.ShippingCarrier;
import com.bookstore.bookstore.entity.Voucher;
import com.bookstore.bookstore.enums.OrderStatus;
import com.bookstore.bookstore.enums.RoleName;
import com.bookstore.bookstore.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Set;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // 1. DASHBOARD
    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(Model model) {
        model.addAttribute("stats", adminService.getDashboardStats());
        model.addAttribute("recentOrders", adminService.getRecentOrders());
        model.addAttribute("shopRevenues", adminService.getShopRevenueStats());
        model.addAttribute("activeTab", "dashboard");
        return "admin/dashboard";
    }

    @GetMapping("/revenue/export-excel")
    public void exportRevenueExcel(jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"bao-cao-doanh-thu-chuoi-sach.csv\"");

        var out = response.getOutputStream();
        // UTF-8 BOM so Excel opens with proper accents and diacritics
        out.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});

        java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.OutputStreamWriter(out, java.nio.charset.StandardCharsets.UTF_8));
        writer.println("Mã Shop,Tên Chi Nhánh Shop,Chủ Cửa Hàng,Tổng Số Đơn,Số Đơn Hoàn Tất,Doanh Thu Hoàn Tất (VND)");

        var revenues = adminService.getShopRevenueStats();
        java.math.BigDecimal grandTotal = java.math.BigDecimal.ZERO;
        long totalOrdersAll = 0;
        long deliveredOrdersAll = 0;

        for (var r : revenues) {
            grandTotal = grandTotal.add(r.getDeliveredRevenue() != null ? r.getDeliveredRevenue() : java.math.BigDecimal.ZERO);
            totalOrdersAll += r.getTotalOrders();
            deliveredOrdersAll += r.getDeliveredOrders();
            writer.printf("\"%d\",\"%s\",\"%s\",%d,%d,%s\n",
                    r.getShopId(),
                    r.getShopName().replace("\"", "\"\""),
                    r.getOwnerName().replace("\"", "\"\""),
                    r.getTotalOrders(),
                    r.getDeliveredOrders(),
                    r.getDeliveredRevenue() != null ? r.getDeliveredRevenue().toPlainString() : "0"
            );
        }

        writer.printf("\"TỔNG CỘNG\",\"Toàn bộ chuỗi cửa hàng\",\"---\",%d,%d,%s\n",
                totalOrdersAll,
                deliveredOrdersAll,
                grandTotal.toPlainString()
        );

        writer.flush();
    }

    // 2. QUẢN LÝ NGƯỜI DÙNG
    @GetMapping("/users")
    public String listUsers(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "role", required = false) RoleName role,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model
    ) {
        model.addAttribute("usersPage", adminService.searchUsers(keyword, role, page, size));
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedRole", role);
        model.addAttribute("allRoles", RoleName.values());
        model.addAttribute("activeTab", "users");
        return "admin/users";
    }

    @PostMapping("/users/{id}/toggle-lock")
    public String toggleUserLock(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            adminService.toggleUserLock(id);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái khóa tài khoản thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/roles")
    public String updateUserRoles(@PathVariable Long id,
                                  @RequestParam("roles") Set<RoleName> roles,
                                  RedirectAttributes redirectAttributes) {
        try {
            adminService.updateUserRoles(id, roles);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật phân quyền người dùng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/create-staff")
    public String createStaffAccount(
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam(required = false) String phone,
            @RequestParam RoleName roleName,
            RedirectAttributes redirectAttributes
    ) {
        try {
            adminService.createStaffAccount(fullName, email, password, phone, roleName);
            redirectAttributes.addFlashAttribute("successMessage", "Tạo tài khoản nhân sự (" + roleName + ") thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    // 3. QUẢN LÝ DANH MỤC SÁCH
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
            redirectAttributes.addFlashAttribute("successMessage", "Lưu danh mục thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/{id}/toggle-status")
    public String toggleCategoryStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            adminService.toggleCategoryStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái danh mục thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    // 4. QUẢN LÝ CHI NHÁNH SHOP
    @GetMapping("/shops")
    public String listShops(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model
    ) {
        model.addAttribute("shopsPage", adminService.searchShops(keyword, page, size));
        model.addAttribute("keyword", keyword);
        model.addAttribute("activeTab", "shops");
        return "admin/shops";
    }

    @PostMapping("/shops/{id}/toggle-status")
    public String toggleShopStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            adminService.toggleShopStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái cửa hàng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/shops";
    }

    // 5. QUẢN LÝ ĐƠN VỊ VẬN CHUYỂN
    @GetMapping("/shipping")
    public String listCarriers(Model model) {
        model.addAttribute("carriers", adminService.getAllCarriers());
        model.addAttribute("activeTab", "shipping");
        return "admin/shipping";
    }

    @PostMapping("/shipping/save")
    public String saveCarrier(@ModelAttribute ShippingCarrier carrier, RedirectAttributes redirectAttributes) {
        try {
            adminService.saveCarrier(carrier);
            redirectAttributes.addFlashAttribute("successMessage", "Lưu thông tin nhà vận chuyển thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/shipping";
    }

    @PostMapping("/shipping/{id}/toggle-status")
    public String toggleCarrierStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            adminService.toggleCarrierStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái nhà vận chuyển thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/shipping";
    }

    // 6. QUẢN LÝ CHIẾT KHẤU APP CHO TỪNG SHOP
    @GetMapping("/commissions")
    public String listCommissions(Model model) {
        model.addAttribute("commissions", adminService.getAllCommissions());
        model.addAttribute("shops", adminService.searchShops(null, 0, 100).getContent());
        model.addAttribute("activeTab", "commissions");
        return "admin/commissions";
    }

    @PostMapping("/commissions/save")
    public String saveCommission(
            @RequestParam Long shopId,
            @RequestParam BigDecimal commissionRate,
            @RequestParam(required = false) String note,
            RedirectAttributes redirectAttributes
    ) {
        try {
            adminService.saveCommission(shopId, commissionRate, note);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật chiết khấu cho shop thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/commissions";
    }

    // 7. QUẢN LÝ VOUCHER KHUYẾN MÃI
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
            redirectAttributes.addFlashAttribute("successMessage", "Lưu mã giảm giá thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/vouchers";
    }

    @PostMapping("/vouchers/{id}/toggle-status")
    public String toggleVoucherStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            adminService.toggleVoucherStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái mã giảm giá thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/vouchers";
    }

    // 8. GIÁM SÁT SÁCH TOÀN CHUỖI
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
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái hiển thị của sách thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/books";
    }

    // 9. THEO DÕI ĐƠN HÀNG TOÀN CHUỖI
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
}
