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

        // 3. Tạo hoặc cập nhật lại đúng chuẩn Unicode tiếng Việt cho danh mục sách
        Category vanHoc = saveOrUpdateCategory("Văn học Việt Nam", "van-hoc-viet-nam", "Tiểu thuyết, truyện ngắn, thơ ca Việt Nam qua các thời kỳ");
        Category kienThuc = saveOrUpdateCategory("Khoa học & Kỹ thuật", "khoa-hoc-ky-thuat", "Sách giáo trình đại học, công nghệ thông tin, toán lý hóa");
        Category lichSu = saveOrUpdateCategory("Lịch sử - Triết học", "lich-su-triet-hoc", "Sách nghiên cứu lịch sử cổ cận hiện đại và văn hóa");
        Category kinhTe = saveOrUpdateCategory("Kinh tế & Khởi nghiệp", "kinh-te-khoi-nghiep", "Sách quản trị kinh doanh, marketing, bài học thành công");

        // 4. Tạo hoặc cập nhật lại chi nhánh cửa hàng với tên tiếng Việt chuẩn
        Shop shopChiNhanh1 = shopRepository.findByOwner(vendor).orElseGet(() -> Shop.builder().owner(vendor).build());
        shopChiNhanh1.setName("Hiệu Sách Cũ Thủ Đức - Chi Nhánh 1");
        shopChiNhanh1.setDescription("Chuyên thu mua và bán lại sách cũ, giáo trình sinh viên giá tốt");
        shopChiNhanh1.setAddress("Số 1 Võ Văn Ngân, TP. Thủ Đức, TP. Hồ Chí Minh");
        shopChiNhanh1.setPhone("0900000004");
        shopChiNhanh1.setActive(true);
        shopChiNhanh1 = shopRepository.save(shopChiNhanh1);

        // 5. Tạo hoặc cập nhật các sách cũ mẫu (sửa sạch lỗi dấu hỏi ? trong tên sách, tác giả)
        Book book1 = saveOrUpdateBook(shopChiNhanh1, vanHoc, "Số Đỏ (Bản in 1989)", "ISBN9780001", "Vũ Trọng Phụng",
                "NXB Văn Học", 1989, BookCondition.OLD, "Sách bìa mềm ố vàng theo thời gian, đủ trang, chữ rõ nét",
                new BigDecimal("120000"), new BigDecimal("65000"), 5, 25,
                "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=500");

        saveOrUpdateBook(shopChiNhanh1, kienThuc, "Giáo trình Cấu Trúc Dữ Liệu & Giải Thuật", "ISBN9780002", "Nhiều Tác Giả",
                "NXB ĐHQG", 2021, BookCondition.VERY_GOOD, "Sách còn rất mới 90%, có ghi chú nhẹ bằng bút chì vài trang đầu",
                new BigDecimal("95000"), new BigDecimal("45000"), 8, 16,
                "https://images.unsplash.com/photo-1532012164546-f432f2e3777a?w=500");

        saveOrUpdateBook(shopChiNhanh1, lichSu, "Việt Nam Sử Lược", "ISBN9780003", "Trần Trọng Kim",
                "NXB Văn Hóa Thông Tin", 2005, BookCondition.LIKE_NEW, "Bản bìa cứng, đẹp như mới 98%",
                new BigDecimal("180000"), new BigDecimal("110000"), 3, 12,
                "https://images.unsplash.com/photo-1497633762265-9d179a990aa6?w=500");

        saveOrUpdateBook(shopChiNhanh1, vanHoc, "Dế Mèn Phiêu Lưu Ký (Ấn bản xưa)", "ISBN9780004", "Tô Hoài",
                "NXB Kim Đồng", 1995, BookCondition.ACCEPTABLE, "Bìa có sờn mép nhẹ, ruột sách lành lặn",
                new BigDecimal("70000"), new BigDecimal("35000"), 2, 4,
                "https://images.unsplash.com/photo-1512820790803-83ca734da794?w=500");

        saveOrUpdateBook(shopChiNhanh1, kinhTe, "Quốc Gia Khởi Nghiệp", "ISBN9780005", "Dan Senor & Saul Singer",
                "NXB Thế Giới", 2018, BookCondition.GOOD, "Tình trạng tốt 85%, không rách, giấy còn sáng",
                new BigDecimal("150000"), new BigDecimal("75000"), 6, 18,
                "https://images.unsplash.com/photo-1589829085413-56de8ae18c73?w=500");

        // 6. Tạo hoặc cập nhật đối tác vận chuyển mẫu (chuẩn tiếng Việt Unicode)
        saveOrUpdateCarrier("Giao Hàng Nhanh (GHN)", new BigDecimal("25000"), "Giao hàng tiêu chuẩn 2-3 ngày toàn quốc");
        saveOrUpdateCarrier("Viettel Post", new BigDecimal("30000"), "Mạng lưới bưu cục rộng khắp cả nước");
        saveOrUpdateCarrier("Hỏa Tốc Nội Thành (Giao trong 2h)", new BigDecimal("45000"), "Chỉ áp dụng trong khu vực nội thành có chi nhánh shop");

        // 7. Tạo chiết khấu mẫu cho shop chi nhánh 1
        if (!appCommissionRepository.existsByShop(shopChiNhanh1)) {
            appCommissionRepository.save(AppCommission.builder()
                    .shop(shopChiNhanh1)
                    .commissionRate(new BigDecimal("5.00"))
                    .note("Mức chiết khấu ưu đãi cho chi nhánh sáng lập chuỗi")
                    .build());
        }

        // 8. Tạo hoặc cập nhật các mã giảm giá mẫu (chuẩn tiếng Việt Unicode)
        saveOrUpdateVoucher("SACHCU10", "Giảm 10% cho đơn từ 100.000 VNĐ", "PERCENTAGE",
                new BigDecimal("10"), new BigDecimal("100000"), new BigDecimal("30000"), 100);
        saveOrUpdateVoucher("FREESHIP", "Miễn phí vận chuyển (Tối đa 30.000 VNĐ)", "FREE_SHIPPING",
                new BigDecimal("30000"), new BigDecimal("150000"), new BigDecimal("30000"), 50);
        saveOrUpdateVoucher("CHAOBAN", "Giảm ngay 20.000 VNĐ cho khách hàng mới", "FIXED_AMOUNT",
                new BigDecimal("20000"), new BigDecimal("50000"), new BigDecimal("20000"), 200);

        // 9. Tạo đơn hàng mẫu cho Admin Dashboard
        if (orderRepository.count() == 0 && book1 != null) {
            Order order1 = Order.builder()
                    .orderCode("ORD" + System.currentTimeMillis())
                    .customer(user)
                    .shop(shopChiNhanh1)
                    .shipper(shipper)
                    .status(OrderStatus.DELIVERED)
                    .paymentMethod(PaymentMethod.COD)
                    .paymentStatus(PaymentStatus.COMPLETED)
                    .subtotal(new BigDecimal("65000"))
                    .shippingFee(new BigDecimal("25000"))
                    .discountAmount(BigDecimal.ZERO)
                    .totalAmount(new BigDecimal("90000"))
                    .recipientName(user.getFullName())
                    .recipientPhone(user.getPhone())
                    .shippingAddress("123 Lê Văn Việt, Tăng Nhơn Phú B, TP. Thủ Đức")
                    .note("Giao giờ hành chính")
                    .createdAt(LocalDateTime.now().minusDays(1))
                    .build();
            orderRepository.save(order1);
        }
    }

    private Category saveOrUpdateCategory(String name, String slug, String description) {
        Category category = categoryRepository.findBySlug(slug).orElse(null);
        if (category == null) {
            category = Category.builder()
                    .slug(slug)
                    .active(true)
                    .build();
        }
        category.setName(name);
        category.setDescription(description);
        return categoryRepository.save(category);
    }

    private Book saveOrUpdateBook(Shop shop, Category category, String title, String isbn, String author,
                                 String publisher, Integer publishYear, BookCondition condition, String conditionNote,
                                 BigDecimal originalPrice, BigDecimal price, Integer stock, Integer soldQuantity,
                                 String imageUrl) {
        Book book = bookRepository.findByIsbn(isbn).orElse(null);
        if (book == null) {
            book = Book.builder()
                    .shop(shop)
                    .category(category)
                    .isbn(isbn)
                    .originalPrice(originalPrice)
                    .price(price)
                    .stock(stock)
                    .soldQuantity(soldQuantity)
                    .mainImageUrl(imageUrl)
                    .active(true)
                    .build();
        }
        book.setTitle(title);
        book.setAuthor(author);
        book.setPublisher(publisher);
        book.setPublishYear(publishYear);
        book.setBookCondition(condition);
        book.setConditionNote(conditionNote);
        return bookRepository.save(book);
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
