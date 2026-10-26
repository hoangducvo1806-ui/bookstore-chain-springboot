package com.bookstore.bookstore.controller;

import com.bookstore.bookstore.entity.Book;
import com.bookstore.bookstore.entity.Order;
import com.bookstore.bookstore.entity.Shop;
import com.bookstore.bookstore.enums.BookCondition;
import com.bookstore.bookstore.enums.OrderStatus;
import com.bookstore.bookstore.repository.CategoryRepository;
import com.bookstore.bookstore.repository.OrderRepository;
import com.bookstore.bookstore.repository.ShopRepository;
import com.bookstore.bookstore.service.VendorBookService;
import com.bookstore.bookstore.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/vendor")
public class VendorController {

    private final VendorBookService vendorBookService;
    private final CategoryRepository categoryRepository;
    private final ShopRepository shopRepository;
    private final OrderRepository orderRepository;
    private final SecurityUtils securityUtils;

    private Shop getOrCreateVendorShop(com.bookstore.bookstore.entity.User vendor) {
        return shopRepository.findByOwner(vendor).orElseGet(() -> {
            Shop newShop = Shop.builder()
                    .name("Hiệu Sách Cũ - " + vendor.getFullName())
                    .owner(vendor)
                    .phone(vendor.getPhone() != null ? vendor.getPhone() : "0901234567")
                    .address("Chi nhánh chuỗi sách - " + vendor.getFullName())
                    .active(true)
                    .build();
            return shopRepository.save(newShop);
        });
    }

    @GetMapping("/books")
    public String books(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        var vendor = securityUtils.getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("Vui lòng đăng nhập."));

        var bookPage = vendorBookService.getMyBooks(vendor, keyword, Math.max(0, page));

        model.addAttribute("bookPage", bookPage);
        model.addAttribute("books", bookPage.getContent());
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentPage", bookPage.getNumber());
        model.addAttribute("totalPages", bookPage.getTotalPages());

        return "vendor/books";
    }

    @GetMapping("/books/new")
    public String newBook(Model model) {
        model.addAttribute("book", new Book());
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("conditions", BookCondition.values());
        model.addAttribute("categoryId", null);
        model.addAttribute("imageUrls", "");
        model.addAttribute("pageTitle", "Thêm sách mới");
        return "vendor/book-form";
    }

    @GetMapping("/books/{id}/edit")
    public String editBook(@PathVariable Long id, Model model) {
        var vendor = securityUtils.getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("Vui lòng đăng nhập."));
        Book book = vendorBookService.getMyBook(vendor, id);

        model.addAttribute("book", book);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("conditions", BookCondition.values());
        model.addAttribute("categoryId", book.getCategory().getId());
        model.addAttribute("imageUrls", book.getImages().stream()
                .map(img -> img.getImageUrl())
                .reduce((a, b) -> a + "\n" + b).orElse(""));
        model.addAttribute("pageTitle", "Chỉnh sửa sách");
        return "vendor/book-form";
    }

    @PostMapping("/books/save")
    public String saveBook(
            @ModelAttribute("book") Book book,
            @RequestParam Long categoryId,
            @RequestParam(required = false, defaultValue = "") String imageUrls,
            RedirectAttributes redirectAttributes) {

        try {
            var vendor = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new IllegalStateException("Vui lòng đăng nhập."));

            List<String> urls = Arrays.stream(imageUrls.split("[,\\r\\n]+"))
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .toList();

            vendorBookService.saveBook(vendor, book, categoryId, urls);
            redirectAttributes.addFlashAttribute("successMessage", "Đã lưu thông tin sách.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage() == null ? "Không thể lưu sách." : e.getMessage()
            );
            return book.getId() == null
                    ? "redirect:/vendor/books/new"
                    : "redirect:/vendor/books/" + book.getId() + "/edit";
        }

        return "redirect:/vendor/books";
    }

    @PostMapping("/books/{id}/toggle")
    public String toggleBook(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        try {
            var vendor = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new IllegalStateException("Vui lòng đăng nhập."));
            vendorBookService.toggleBook(vendor, id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật trạng thái sách.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể cập nhật trạng thái sách.");
        }
        return "redirect:/vendor/books";
    }

    // ==========================================
    // QUẢN LÝ ĐƠN HÀNG CỬA HÀNG (VENDOR ORDERS)
    // ==========================================
    @GetMapping("/orders")
    public String orders(
            @RequestParam(value = "status", required = false) OrderStatus status,
            Model model) {
        var vendor = securityUtils.getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("Vui lòng đăng nhập."));
        Shop shop = getOrCreateVendorShop(vendor);

        List<Order> orders;
        if (status != null) {
            orders = orderRepository.findByShopAndStatusOrderByCreatedAtDesc(shop, status);
        } else {
            orders = orderRepository.findByShopOrderByCreatedAtDesc(shop);
        }

        model.addAttribute("orders", orders);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("allStatuses", OrderStatus.values());
        model.addAttribute("shop", shop);
        return "vendor/orders";
    }

    @PostMapping("/orders/{id}/status")
    public String updateOrderStatus(
            @PathVariable Long id,
            @RequestParam("status") OrderStatus status,
            RedirectAttributes redirectAttributes) {
        try {
            var vendor = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new IllegalStateException("Vui lòng đăng nhập."));
            Shop shop = getOrCreateVendorShop(vendor);

            Order order = orderRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Đơn hàng không tồn tại."));

            if (!order.getShop().getId().equals(shop.getId())) {
                throw new IllegalStateException("Bạn không có quyền thao tác trên đơn hàng của shop khác.");
            }

            order.setStatus(status);
            orderRepository.save(order);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật trạng thái đơn hàng sang: " + status.getLabel());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/vendor/orders";
    }

    // ==========================================
    // BÁO CÁO DOANH THU SHOP (VENDOR REVENUE)
    // ==========================================
    @GetMapping("/revenue")
    public String revenue(Model model) {
        var vendor = securityUtils.getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("Vui lòng đăng nhập."));
        Shop shop = getOrCreateVendorShop(vendor);

        long totalOrders = orderRepository.countByShop(shop);
        long deliveredOrders = orderRepository.countByShopAndStatus(shop, OrderStatus.DELIVERED);
        long preparingOrders = orderRepository.countByShopAndStatus(shop, OrderStatus.PICKED_UP)
                + orderRepository.countByShopAndStatus(shop, OrderStatus.CONFIRMED);
        long shippingOrders = orderRepository.countByShopAndStatus(shop, OrderStatus.SHIPPING);

        BigDecimal deliveredRevenue = orderRepository.calculateDeliveredRevenueByShop(shop);
        if (deliveredRevenue == null) deliveredRevenue = BigDecimal.ZERO;

        List<Order> deliveredList = orderRepository.findByShopAndStatusOrderByCreatedAtDesc(shop, OrderStatus.DELIVERED);

        model.addAttribute("shop", shop);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("deliveredOrders", deliveredOrders);
        model.addAttribute("preparingOrders", preparingOrders);
        model.addAttribute("shippingOrders", shippingOrders);
        model.addAttribute("deliveredRevenue", deliveredRevenue);
        model.addAttribute("deliveredList", deliveredList);

        return "vendor/revenue";
    }
}