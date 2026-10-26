package com.bookstore.bookstore.config;

import com.bookstore.bookstore.entity.*;
import com.bookstore.bookstore.enums.*;
import com.bookstore.bookstore.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Configuration
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ShopRepository shopRepository;
    private final BookRepository bookRepository;
    private final ShippingCarrierRepository shippingCarrierRepository;
    private final AppCommissionRepository appCommissionRepository;
    private final VoucherRepository voucherRepository;
    private final OrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;
    private final ExtendedBookDataSeeder extendedBookDataSeeder;

    @Override
    @Transactional
    public void run(String... args) {
        // 1. Tạo 5 Role nếu chưa có
        Map<RoleName, Role> roleMap = new HashMap<>();
        for (RoleName roleName : RoleName.values()) {
            Role role = roleRepository.findByName(roleName)
                    .orElseGet(() -> roleRepository.save(
                            Role.builder()
                                    .name(roleName)
                                    .description("Vai trò " + roleName.name())
                                    .build()
                    ));
            roleMap.put(roleName, role);
        }

        // 2. Tạo các tài khoản mẫu (Password mặc định: 123456)
        String defaultPassword = passwordEncoder.encode("123456");

        // Admin
        User admin = userRepository.findByEmail("admin@gmail.com")
                .orElseGet(() -> userRepository.save(User.builder()
                        .email("admin@gmail.com")
                        .fullName("Quản Trị Viên (Admin)")
                        .password(defaultPassword)
                        .phone("0900000001")
                        .enabled(true)
                        .roles(Set.of(roleMap.get(RoleName.ADMIN)))
                        .build()));

        // Manager
        User manager = userRepository.findByEmail("manager@gmail.com")
                .orElseGet(() -> userRepository.save(User.builder()
                        .email("manager@gmail.com")
                        .fullName("Quản Lý Chuỗi Cửa Hàng (Manager)")
                        .password(defaultPassword)
                        .phone("0900000002")
                        .enabled(true)
                        .roles(Set.of(roleMap.get(RoleName.MANAGER)))
                        .build()));

        // Shipper
        User shipper = userRepository.findByEmail("shipper@gmail.com")
                .orElseGet(() -> userRepository.save(User.builder()
                        .email("shipper@gmail.com")
                        .fullName("Nhân Viên Giao Hàng (Shipper)")
                        .password(defaultPassword)
                        .phone("0900000003")
                        .enabled(true)
                        .roles(Set.of(roleMap.get(RoleName.SHIPPER)))
                        .build()));

        // Vendor (Chủ chi nhánh shop)
        User vendor = userRepository.findByEmail("vendor@gmail.com")
                .orElseGet(() -> userRepository.save(User.builder()
                        .email("vendor@gmail.com")
                        .fullName("Chủ Chi Nhánh Cửa Hàng (Vendor)")
                        .password(defaultPassword)
                        .phone("0900000004")
                        .enabled(true)
                        .roles(Set.of(roleMap.get(RoleName.VENDOR), roleMap.get(RoleName.USER)))
                        .build()));

        // Khách hàng thông thường (User)
        User user = userRepository.findByEmail("user@gmail.com")
                .orElseGet(() -> userRepository.save(User.builder()
                        .email("user@gmail.com")
                        .fullName("Khách Mua Sách (User)")
                        .password(defaultPassword)
                        .phone("0900000005")
                        .enabled(true)
                        .roles(Set.of(roleMap.get(RoleName.USER)))
                        .build()));

        // 3. Tạo hoặc cập nhật chi nhánh cửa hàng
        Shop shopChiNhanh1 = shopRepository.findByOwner(vendor).orElseGet(() -> Shop.builder().owner(vendor).build());
        shopChiNhanh1.setName("Hiệu Sách Cũ Thủ Đức - Chi Nhánh 1");
        shopChiNhanh1.setDescription("Chuyên thu mua và bán lại sách cũ, giáo trình sinh viên giá tốt");
        shopChiNhanh1.setAddress("Số 1 Võ Văn Ngân, TP. Thủ Đức, TP. Hồ Chí Minh");
        shopChiNhanh1.setPhone("0900000004");
        shopChiNhanh1.setActive(true);
        shopChiNhanh1 = shopRepository.save(shopChiNhanh1);

        // 4. Nạp trọn bộ hơn 90 đầu sách thật phong phú và 7 danh mục lớn
        extendedBookDataSeeder.seedAllBooks(shopChiNhanh1);

        // 5. Tạo hoặc cập nhật đối tác vận chuyển mẫu (chuẩn tiếng Việt Unicode)
        saveOrUpdateCarrier("Giao Hàng Nhanh (GHN)", new BigDecimal("25000"), "Giao hàng tiêu chuẩn 2-3 ngày toàn quốc");
        saveOrUpdateCarrier("Viettel Post", new BigDecimal("30000"), "Mạng lưới bưu cục rộng khắp cả nước");
        saveOrUpdateCarrier("Hỏa Tốc Nội Thành (Giao trong 2h)", new BigDecimal("45000"), "Chỉ áp dụng trong khu vực nội thành có chi nhánh shop");

        // 6. Tạo chiết khấu mẫu cho shop chi nhánh 1
        if (!appCommissionRepository.existsByShop(shopChiNhanh1)) {
            appCommissionRepository.save(AppCommission.builder()
                    .shop(shopChiNhanh1)
                    .commissionRate(new BigDecimal("5.00"))
                    .note("Mức chiết khấu ưu đãi cho chi nhánh sáng lập chuỗi")
                    .build());
        }

        // 7. Tạo hoặc cập nhật các mã giảm giá mẫu (chuẩn tiếng Việt Unicode)
        saveOrUpdateVoucher("SACHCU10", "Giảm 10% cho đơn từ 100.000 VNĐ", "PERCENTAGE",
                new BigDecimal("10"), new BigDecimal("100000"), new BigDecimal("30000"), 100);
        saveOrUpdateVoucher("FREESHIP", "Miễn phí vận chuyển (Tối đa 30.000 VNĐ)", "FREE_SHIPPING",
                new BigDecimal("30000"), new BigDecimal("150000"), new BigDecimal("30000"), 50);
        saveOrUpdateVoucher("CHAOBAN", "Giảm ngay 20.000 VNĐ cho khách hàng mới", "FIXED_AMOUNT",
                new BigDecimal("20000"), new BigDecimal("50000"), new BigDecimal("20000"), 200);

        // 8. Tạo đơn hàng mẫu cho Admin Dashboard
        if (orderRepository.count() == 0) {
            Book sampleBook = bookRepository.findAll().stream().findFirst().orElse(null);
            if (sampleBook != null) {
                Order order1 = Order.builder()
                        .orderCode("ORD" + System.currentTimeMillis())
                        .customer(user)
                        .shop(shopChiNhanh1)
                        .shipper(shipper)
                        .status(OrderStatus.DELIVERED)
                        .paymentMethod(PaymentMethod.COD)
                        .paymentStatus(PaymentStatus.COMPLETED)
                        .subtotal(sampleBook.getPrice())
                        .shippingFee(new BigDecimal("25000"))
                        .discountAmount(BigDecimal.ZERO)
                        .totalAmount(sampleBook.getPrice().add(new BigDecimal("25000")))
                        .recipientName(user.getFullName())
                        .recipientPhone(user.getPhone())
                        .shippingAddress("123 Lê Văn Việt, Tăng Nhơn Phú B, TP. Thủ Đức")
                        .note("Giao giờ hành chính")
                        .createdAt(LocalDateTime.now().minusDays(1))
                        .build();
                orderRepository.save(order1);
            }
        }
    }

    private void saveOrUpdateCarrier(String name, BigDecimal fee, String desc) {
        ShippingCarrier carrier = shippingCarrierRepository.findByName(name).orElse(null);
        if (carrier == null) {
            carrier = ShippingCarrier.builder()
                    .name(name)
                    .shippingFee(fee)
                    .description(desc)
                    .active(true)
                    .build();
        } else {
            carrier.setShippingFee(fee);
            carrier.setDescription(desc);
            carrier.setActive(true);
        }
        shippingCarrierRepository.save(carrier);
    }

    private void saveOrUpdateVoucher(String code, String title, String type, BigDecimal value,
                                    BigDecimal minOrder, BigDecimal maxDiscount, Integer limit) {
        Voucher voucher = voucherRepository.findByCode(code).orElse(null);
        if (voucher == null) {
            voucher = Voucher.builder()
                    .code(code)
                    .title(title)
                    .discountType(type)
                    .discountValue(value)
                    .minOrderAmount(minOrder)
                    .maxDiscountAmount(maxDiscount)
                    .usageLimit(limit)
                    .usedCount(0)
                    .startDate(LocalDateTime.now().minusDays(1))
                    .endDate(LocalDateTime.now().plusMonths(3))
                    .active(true)
                    .build();
        } else {
            voucher.setTitle(title);
            voucher.setDiscountType(type);
            voucher.setDiscountValue(value);
            voucher.setMinOrderAmount(minOrder);
            voucher.setMaxDiscountAmount(maxDiscount);
            voucher.setActive(true);
        }
        voucherRepository.save(voucher);
    }
}
