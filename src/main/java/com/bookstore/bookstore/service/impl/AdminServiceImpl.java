package com.bookstore.bookstore.service.impl;

import com.bookstore.bookstore.dto.AdminDashboardStats;
import com.bookstore.bookstore.entity.*;
import com.bookstore.bookstore.enums.OrderStatus;
import com.bookstore.bookstore.enums.RoleName;
import com.bookstore.bookstore.repository.*;
import com.bookstore.bookstore.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ShopRepository shopRepository;
    private final BookRepository bookRepository;
    private final OrderRepository orderRepository;
    private final CategoryRepository categoryRepository;
    private final ShippingCarrierRepository shippingCarrierRepository;
    private final AppCommissionRepository appCommissionRepository;
    private final VoucherRepository voucherRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AdminDashboardStats getDashboardStats() {
        Map<String, Long> statusCounts = new LinkedHashMap<>();
        for (OrderStatus status : OrderStatus.values()) {
            statusCounts.put(status.name(), orderRepository.countByStatus(status));
        }

        BigDecimal revenue = orderRepository.calculateTotalDeliveredRevenue();

        return AdminDashboardStats.builder()
                .totalUsers(userRepository.count())
                .totalShops(shopRepository.count())
                .totalBooks(bookRepository.count())
                .totalOrders(orderRepository.count())
                .totalDeliveredRevenue(revenue != null ? revenue : BigDecimal.ZERO)
                .ordersByStatus(statusCounts)
                .build();
    }

    @Override
    public List<Order> getRecentOrders() {
        return orderRepository.findTop10ByOrderByCreatedAtDesc();
    }

    @Override
    public Page<User> searchUsers(String keyword, RoleName roleName, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return userRepository.searchUsers(keyword, roleName, pageable);
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy người dùng ID: " + id));
    }

    @Override
    @Transactional
    public void toggleUserLock(Long id) {
        User user = getUserById(id);
        user.setLocked(!user.getLocked());
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void updateUserRoles(Long id, Set<RoleName> roleNames) {
        User user = getUserById(id);
        Set<Role> roles = new HashSet<>();
        for (RoleName rName : roleNames) {
            Role role = roleRepository.findByName(rName)
                    .orElseGet(() -> roleRepository.save(Role.builder()
                            .name(rName)
                            .description("Vai trò " + rName.name())
                            .build()));
            roles.add(role);
        }
        user.setRoles(roles);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public User createStaffAccount(String fullName, String email, String password, String phone, RoleName roleName) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email này đã được sử dụng!");
        }

        Role role = roleRepository.findByName(roleName)
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name(roleName)
                        .description("Vai trò " + roleName.name())
                        .build()));

        User user = User.builder()
                .fullName(fullName)
                .email(email.toLowerCase().trim())
                .password(passwordEncoder.encode(password))
                .phone(phone)
                .enabled(true) // Tài khoản nội bộ được kích hoạt sẵn
                .locked(false)
                .roles(Set.of(role))
                .build();

        return userRepository.save(user);
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy danh mục ID: " + id));
    }

    @Override
    @Transactional
    public Category saveCategory(Category category) {
        if (category.getSlug() == null || category.getSlug().isBlank()) {
            category.setSlug(toSlug(category.getName()));
        }
        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public void toggleCategoryStatus(Long id) {
        Category category = getCategoryById(id);
        category.setActive(!category.getActive());
        categoryRepository.save(category);
    }

    @Override
    public Page<Shop> searchShops(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return shopRepository.searchShops(keyword, pageable);
    }

    @Override
    public Shop getShopById(Long id) {
        return shopRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy cửa hàng ID: " + id));
    }

    @Override
    @Transactional
    public void toggleShopStatus(Long id) {
        Shop shop = getShopById(id);
        shop.setActive(!shop.getActive());
        shopRepository.save(shop);
    }

    @Override
    public List<ShippingCarrier> getAllCarriers() {
        return shippingCarrierRepository.findAll();
    }

    @Override
    public ShippingCarrier getCarrierById(Long id) {
        return shippingCarrierRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy nhà vận chuyển ID: " + id));
    }

    @Override
    @Transactional
    public ShippingCarrier saveCarrier(ShippingCarrier carrier) {
        return shippingCarrierRepository.save(carrier);
    }

    @Override
    @Transactional
    public void toggleCarrierStatus(Long id) {
        ShippingCarrier carrier = getCarrierById(id);
        carrier.setActive(!carrier.getActive());
        shippingCarrierRepository.save(carrier);
    }

    @Override
    public List<AppCommission> getAllCommissions() {
        return appCommissionRepository.findAll();
    }

    @Override
    @Transactional
    public AppCommission saveCommission(Long shopId, BigDecimal commissionRate, String note) {
        Shop shop = getShopById(shopId);
        AppCommission commission = appCommissionRepository.findByShop(shop)
                .orElse(AppCommission.builder().shop(shop).build());
        commission.setCommissionRate(commissionRate);
        commission.setNote(note);
        return appCommissionRepository.save(commission);
    }

    @Override
    public List<Voucher> getAllVouchers() {
        return voucherRepository.findAll();
    }

    @Override
    public Voucher getVoucherById(Long id) {
        return voucherRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy mã giảm giá ID: " + id));
    }

    @Override
    @Transactional
    public Voucher saveVoucher(Voucher voucher) {
        return voucherRepository.save(voucher);
    }

    @Override
    @Transactional
    public void toggleVoucherStatus(Long id) {
        Voucher voucher = getVoucherById(id);
        voucher.setActive(!voucher.getActive());
        voucherRepository.save(voucher);
    }

    @Override
    public Page<Book> searchBooks(String keyword, Long shopId, Long categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return bookRepository.searchBooks(keyword, shopId, categoryId, pageable);
    }

    @Override
    @Transactional
    public void toggleBookStatus(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy sách ID: " + id));
        book.setActive(!book.getActive());
        bookRepository.save(book);
    }

    @Override
    public Page<Order> getOrders(OrderStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        if (status != null) {
            return orderRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        }
        return orderRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    private String toSlug(String input) {
        if (input == null) return "";
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String noDiacritics = pattern.matcher(normalized).replaceAll("")
                .replace("đ", "d").replace("Đ", "d");
        return noDiacritics.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }
}
