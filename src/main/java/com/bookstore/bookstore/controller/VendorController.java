package com.bookstore.bookstore.controller;

import com.bookstore.bookstore.dto.ShopRegisterDto;
import com.bookstore.bookstore.dto.VendorDashboardStats;
import com.bookstore.bookstore.entity.Book;
import com.bookstore.bookstore.entity.BookImage;
import com.bookstore.bookstore.entity.Order;
import com.bookstore.bookstore.entity.Shop;
import com.bookstore.bookstore.entity.User;
import com.bookstore.bookstore.enums.BookCondition;
import com.bookstore.bookstore.enums.OrderStatus;
import com.bookstore.bookstore.repository.CategoryRepository;
import com.bookstore.bookstore.repository.OrderRepository;
import com.bookstore.bookstore.security.CustomUserDetailsService;
import com.bookstore.bookstore.security.JwtService;
import com.bookstore.bookstore.service.OrderService;
import com.bookstore.bookstore.service.ShopService;
import com.bookstore.bookstore.service.VendorBookService;
import com.bookstore.bookstore.util.SecurityUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/vendor")
@Slf4j
public class VendorController {

    private final VendorBookService vendorBookService;
    private final ShopService shopService;
    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final CategoryRepository categoryRepository;
    private final SecurityUtils securityUtils;
    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;

    // =========================================================================
    // 1. ĐĂNG KÝ SHOP (/vendor/register)
    // =========================================================================

    @GetMapping("/register")
    public String registerShopPage(Model model, RedirectAttributes redirectAttributes) {
        User currentUser = securityUtils.getCurrentUser().orElse(null);
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng đăng nhập để mở cửa hàng!");
            return "redirect:/login";
        }

        if (shopService.hasShop(currentUser)) {
            redirectAttributes.addFlashAttribute("infoMessage", "Bạn đã có cửa hàng, chuyển hướng tới trang quản trị.");
            return "redirect:/vendor/dashboard";
        }

        if (!model.containsAttribute("shopRegisterDto")) {
            ShopRegisterDto dto = new ShopRegisterDto();
            dto.setPhone(currentUser.getPhone());
            model.addAttribute("shopRegisterDto", dto);
        }

        return "vendor/register";
    }

    @PostMapping("/register")
    public String handleRegisterShop(
            @Valid @ModelAttribute("shopRegisterDto") ShopRegisterDto dto,
            BindingResult bindingResult,
            @RequestParam(value = "logoFile", required = false) MultipartFile logoFile,
            @RequestParam(value = "bannerFile", required = false) MultipartFile bannerFile,
            HttpServletResponse response,
            RedirectAttributes redirectAttributes,
            Model model) {

        User currentUser = securityUtils.getCurrentUser().orElse(null);
        if (currentUser == null) return "redirect:/login";

        if (bindingResult.hasErrors()) {
            return "vendor/register";
        }

        try {
            Shop shop = shopService.registerShop(currentUser, dto, logoFile, bannerFile);

            // Tái cấp phát token JWT chứa quyền VENDOR để người dùng truy cập được ngay lập tức
            UserDetails userDetails = userDetailsService.loadUserByUsername(currentUser.getEmail());
            String newToken = jwtService.generateToken(userDetails);

            Cookie cookie = new Cookie("jwt_token", newToken);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(7 * 24 * 60 * 60);
            response.addCookie(cookie);

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(authToken);

            redirectAttributes.addFlashAttribute("successMessage", 
                    "Chúc mừng bạn! Cửa hàng '" + shop.getName() + "' đã được khởi tạo thành công.");
            return "redirect:/vendor/dashboard";
        } catch (Exception e) {
            log.error("Lỗi đăng ký shop: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", e.getMessage());
            return "vendor/register";
        }
    }

    // =========================================================================
    // 2. DASHBOARD QUẢN TRỊ SHOP (/vendor/dashboard)
    // =========================================================================

    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(Model model, RedirectAttributes redirectAttributes) {
        User vendor = securityUtils.getCurrentUser().orElse(null);
        if (vendor == null) return "redirect:/login";

        if (!shopService.hasShop(vendor)) {
            redirectAttributes.addFlashAttribute("infoMessage", "Vui lòng hoàn tất đăng ký thông tin cửa hàng trước.");
            return "redirect:/vendor/register";
        }

        Shop shop = shopService.getShopByOwner(vendor);
        VendorDashboardStats stats = shopService.getDashboardStats(vendor);
        List<Order> recentOrders = orderRepository.findTop5ByShopOrderByCreatedAtDesc(shop);

        model.addAttribute("shop", shop);
        model.addAttribute("stats", stats);
        model.addAttribute("recentOrders", recentOrders);
        model.addAttribute("activeNav", "dashboard");

        return "vendor/dashboard";
    }

    @GetMapping("/revenue")
    public String revenuePage(Model model, RedirectAttributes redirectAttributes) {
        User vendor = securityUtils.getCurrentUser().orElse(null);
        if (vendor == null) return "redirect:/login";

        if (!shopService.hasShop(vendor)) {
            redirectAttributes.addFlashAttribute("infoMessage", "Vui lòng hoàn tất đăng ký thông tin cửa hàng trước.");
            return "redirect:/vendor/register";
        }

        Shop shop = shopService.getShopByOwner(vendor);
        java.math.BigDecimal deliveredRevenue = orderRepository.calculateDeliveredRevenueByShop(shop);
        if (deliveredRevenue == null) deliveredRevenue = java.math.BigDecimal.ZERO;

        long totalOrders = orderRepository.countByShop(shop);
        long deliveredOrders = orderRepository.countByShopAndStatus(shop, OrderStatus.DELIVERED);
        long preparingOrders = orderRepository.countByShopAndStatus(shop, OrderStatus.CONFIRMED);
        long shippingOrders = orderRepository.countByShopAndStatus(shop, OrderStatus.SHIPPING);

        List<Order> deliveredList = orderRepository.findByShopAndStatusOrderByCreatedAtDesc(shop, OrderStatus.DELIVERED);

        model.addAttribute("shop", shop);
        model.addAttribute("deliveredRevenue", deliveredRevenue);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("deliveredOrders", deliveredOrders);
        model.addAttribute("preparingOrders", preparingOrders);
        model.addAttribute("shippingOrders", shippingOrders);
        model.addAttribute("deliveredList", deliveredList);
        model.addAttribute("activeNav", "revenue");

        return "vendor/revenue";
    }

    // =========================================================================
    // 3. CẤU HÌNH THÔNG TIN SHOP (/vendor/shop/profile)
    // =========================================================================

    @GetMapping("/shop/profile")
    public String shopProfilePage(Model model) {
        User vendor = securityUtils.getCurrentUser().orElseThrow();
        if (!shopService.hasShop(vendor)) return "redirect:/vendor/register";

        Shop shop = shopService.getShopByOwner(vendor);
        ShopRegisterDto dto = ShopRegisterDto.builder()
                .name(shop.getName())
                .phone(shop.getPhone())
                .address(shop.getAddress())
                .description(shop.getDescription())
                .bio(shop.getBio())
                .build();

        model.addAttribute("shop", shop);
        model.addAttribute("shopDto", dto);
        model.addAttribute("activeNav", "profile");
        return "vendor/store-profile";
    }

    @PostMapping("/shop/profile")
    public String updateShopProfile(
            @Valid @ModelAttribute("shopDto") ShopRegisterDto dto,
            BindingResult bindingResult,
            @RequestParam(value = "logoFile", required = false) MultipartFile logoFile,
            @RequestParam(value = "bannerFile", required = false) MultipartFile bannerFile,
            RedirectAttributes redirectAttributes,
            Model model) {

        User vendor = securityUtils.getCurrentUser().orElseThrow();
        if (bindingResult.hasErrors()) {
            Shop shop = shopService.getShopByOwner(vendor);
            model.addAttribute("shop", shop);
            model.addAttribute("activeNav", "profile");
            return "vendor/store-profile";
        }

        try {
            shopService.updateShop(vendor, dto, logoFile, bannerFile);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin cửa hàng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/vendor/shop/profile";
    }

    // =========================================================================
    // 4. QUẢN LÝ ĐƠN HÀNG CỦA SHOP THEO TAB (/vendor/orders)
    // =========================================================================

    @GetMapping("/orders")
    public String listOrders(
            @RequestParam(value = "tab", defaultValue = "all") String tab,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {

        User vendor = securityUtils.getCurrentUser().orElseThrow();
        if (!shopService.hasShop(vendor)) return "redirect:/vendor/register";

        Shop shop = shopService.getShopByOwner(vendor);
        Page<Order> orderPage = orderService.getOrdersByShopAndTab(vendor, tab, page, size);

        model.addAttribute("shop", shop);
        model.addAttribute("orderPage", orderPage);
        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("currentTab", tab);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());
        model.addAttribute("totalOrders", orderPage.getTotalElements());

        // Đếm số lượng theo tab để hiển thị badge
        model.addAttribute("countNew", orderRepository.countByShopAndStatus(shop, OrderStatus.NEW));
        model.addAttribute("countConfirmed", orderRepository.countByShopAndStatus(shop, OrderStatus.CONFIRMED));
        model.addAttribute("countPickedUp", orderRepository.countByShopAndStatus(shop, OrderStatus.PICKED_UP));
        model.addAttribute("countShipping", orderRepository.countByShopAndStatus(shop, OrderStatus.SHIPPING));
        model.addAttribute("countDelivered", orderRepository.countByShopAndStatus(shop, OrderStatus.DELIVERED));
        model.addAttribute("countCancelled", orderRepository.countByShopAndStatus(shop, OrderStatus.CANCELLED));

        model.addAttribute("activeNav", "orders");
        return "vendor/orders";
    }

    @GetMapping("/orders/{id}")
    public String orderDetail(@PathVariable("id") Long id, Model model) {
        User vendor = securityUtils.getCurrentUser().orElseThrow();
        if (!shopService.hasShop(vendor)) return "redirect:/vendor/register";

        Shop shop = shopService.getShopByOwner(vendor);
        Order order = orderService.getOrderByShopAndId(vendor, id);

        model.addAttribute("shop", shop);
        model.addAttribute("order", order);
        model.addAttribute("activeNav", "orders");
        return "vendor/order-detail";
    }

    @PostMapping("/orders/{id}/status")
    public String updateOrderStatus(
            @PathVariable("id") Long id,
            @RequestParam("status") OrderStatus newStatus,
            @RequestParam(value = "note", required = false) String note,
            @RequestParam(value = "redirectTab", defaultValue = "all") String redirectTab,
            RedirectAttributes redirectAttributes) {

        User vendor = securityUtils.getCurrentUser().orElseThrow();
        try {
            orderService.updateOrderStatusByVendor(vendor, id, newStatus, note);
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Đã cập nhật trạng thái đơn hàng sang: " + newStatus.getLabel());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/vendor/orders?tab=" + redirectTab;
    }

    // =========================================================================
    // 5. CRUD SẢN PHẨM SÁCH CỦA SHOP (/vendor/books)
    // =========================================================================

    @GetMapping("/books")
    public String books(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        var vendor = securityUtils.getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("Vui lòng đăng nhập."));

        if (!shopService.hasShop(vendor)) return "redirect:/vendor/register";

        Shop shop = shopService.getShopByOwner(vendor);
        var bookPage = vendorBookService.getMyBooks(vendor, keyword, Math.max(0, page));

        model.addAttribute("shop", shop);
        model.addAttribute("bookPage", bookPage);
        model.addAttribute("books", bookPage.getContent());
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentPage", bookPage.getNumber());
        model.addAttribute("totalPages", bookPage.getTotalPages());
        model.addAttribute("activeNav", "books");

        return "vendor/books";
    }

    @GetMapping("/books/new")
    public String newBook(Model model) {
        var vendor = securityUtils.getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("Vui lòng đăng nhập."));

        if (!shopService.hasShop(vendor)) return "redirect:/vendor/register";

        Shop shop = shopService.getShopByOwner(vendor);
        model.addAttribute("shop", shop);
        model.addAttribute("book", new Book());
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("conditions", BookCondition.values());
        model.addAttribute("categoryId", null);
        model.addAttribute("imageUrls", "");
        model.addAttribute("pageTitle", "Thêm Sách Mới");
        model.addAttribute("activeNav", "books");
        return "vendor/book-form";
    }

    @GetMapping("/books/{id}/edit")
    public String editBook(@PathVariable Long id, Model model) {
        var vendor = securityUtils.getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("Vui lòng đăng nhập."));

        if (!shopService.hasShop(vendor)) return "redirect:/vendor/register";

        Shop shop = shopService.getShopByOwner(vendor);
        Book book = vendorBookService.getMyBook(vendor, id);

        model.addAttribute("shop", shop);
        model.addAttribute("book", book);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("conditions", BookCondition.values());
        model.addAttribute("categoryId", book.getCategory().getId());
        model.addAttribute("imageUrls", book.getImages().stream()
                .map(BookImage::getImageUrl)
                .reduce((a, b) -> a + "\n" + b).orElse(""));
        model.addAttribute("pageTitle", "Chỉnh Sửa Sách: " + book.getTitle());
        model.addAttribute("activeNav", "books");
        return "vendor/book-form";
    }

    @PostMapping("/books/save")
    public String saveBook(
            @ModelAttribute("book") Book book,
            @RequestParam Long categoryId,
            @RequestParam(required = false, defaultValue = "") String imageUrls,
            @RequestParam(value = "mainImageFile", required = false) MultipartFile mainImageFile,
            @RequestParam(value = "imageFiles", required = false) List<MultipartFile> imageFiles,
            RedirectAttributes redirectAttributes) {

        try {
            var vendor = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new IllegalStateException("Vui lòng đăng nhập."));

            List<String> urls = Arrays.stream(imageUrls.split("[,\\r\\n]+"))
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .toList();

            vendorBookService.saveBook(vendor, book, categoryId, urls, mainImageFile, imageFiles);
            redirectAttributes.addFlashAttribute("successMessage", "Đã lưu thông tin sách thành công.");
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

    @PostMapping("/books/{id}/delete")
    public String deleteBook(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        try {
            var vendor = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new IllegalStateException("Vui lòng đăng nhập."));
            vendorBookService.deleteBook(vendor, id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa sách khỏi kho của cửa hàng.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa sách: " + e.getMessage());
        }
        return "redirect:/vendor/books";
    }
}
