package com.bookstore.bookstore.config;

import com.bookstore.bookstore.entity.*;
import com.bookstore.bookstore.enums.BookCondition;
import com.bookstore.bookstore.enums.RoleName;
import com.bookstore.bookstore.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Configuration
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ShopRepository shopRepository;
    private final BookRepository bookRepository;
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
        userRepository.findByEmail("manager@gmail.com")
                .orElseGet(() -> userRepository.save(User.builder()
                        .email("manager@gmail.com")
                        .fullName("Quản Lý Chuỗi Cửa Hàng (Manager)")
                        .password(defaultPassword)
                        .phone("0900000002")
                        .enabled(true)
                        .roles(Set.of(roleMap.get(RoleName.MANAGER)))
                        .build()));

        // Shipper
        userRepository.findByEmail("shipper@gmail.com")
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
        userRepository.findByEmail("user@gmail.com")
                .orElseGet(() -> userRepository.save(User.builder()
                        .email("user@gmail.com")
                        .fullName("Khách Mua Sách (User)")
                        .password(defaultPassword)
                        .phone("0900000005")
                        .enabled(true)
                        .roles(Set.of(roleMap.get(RoleName.USER)))
                        .build()));

        // 3. Tạo một số danh mục sách mẫu
        Category vanHoc = createCategoryIfNotExist("Văn học Việt Nam", "van-hoc-viet-nam", "Tiểu thuyết, truyện ngắn, thơ ca Việt Nam qua các thời kỳ");
        Category kienThuc = createCategoryIfNotExist("Khoa học & Kỹ thuật", "khoa-hoc-ky-thuat", "Sách giáo trình đại học, công nghệ thông tin, toán lý hóa");
        Category lichSu = createCategoryIfNotExist("Lịch sử - Triết học", "lich-su-triet-hoc", "Sách nghiên cứu lịch sử cổ cận hiện đại và văn hóa");

        // 4. Tạo chi nhánh cửa hàng mẫu cho Vendor
        Shop shopChiNhanh1 = shopRepository.findByOwner(vendor)
                .orElseGet(() -> shopRepository.save(Shop.builder()
                        .owner(vendor)
                        .name("Hiệu Sách Cũ Thủ Đức - Chi Nhánh 1")
                        .description("Chuyên thu mua và bán lại sách cũ, giáo trình sinh viên giá tốt")
                        .address("Số 1 Võ Văn Ngân, TP. Thủ Đức, TP. Hồ Chí Minh")
                        .phone("0900000004")
                        .active(true)
                        .build()));

        // 5. Tạo một số sách cũ mẫu (Có cuốn soldQuantity > 10 để phục vụ trang Guest)
        createBookIfNotExist(shopChiNhanh1, vanHoc, "Số Đỏ (Bản in 1989)", "ISBN9780001", "Vũ Trọng Phụng",
                "NXB Văn Học", 1989, BookCondition.OLD, "Sách bìa mềm ố vàng theo thời gian, đủ trang, chữ rõ",
                new BigDecimal("120000"), new BigDecimal("65000"), 5, 25,
                "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=500");

        createBookIfNotExist(shopChiNhanh1, kienThuc, "Giáo trình Cấu Trúc Dữ Liệu & Giải Thuật", "ISBN9780002", "Nhiều Tác Giả",
                "NXB ĐHQG", 2021, BookCondition.VERY_GOOD, "Sách còn rất mới 90%, có ghi chú nhẹ bằng bút chì vài trang đầu",
                new BigDecimal("95000"), new BigDecimal("45000"), 8, 16,
                "https://images.unsplash.com/photo-1532012164546-f432f2e3777a?w=500");

        createBookIfNotExist(shopChiNhanh1, lichSu, "Việt Nam Sử Lược", "ISBN9780003", "Trần Trọng Kim",
                "NXB Văn Hóa Thông Tin", 2005, BookCondition.LIKE_NEW, "Bản bìa cứng, đẹp như mới 98%",
                new BigDecimal("180000"), new BigDecimal("110000"), 3, 12,
                "https://images.unsplash.com/photo-1497633762265-9d179a990aa6?w=500");

        createBookIfNotExist(shopChiNhanh1, vanHoc, "Dế Mèn Phiêu Lưu Ký (Ấn bản xưa)", "ISBN9780004", "Tô Hoài",
                "NXB Kim Đồng", 1995, BookCondition.ACCEPTABLE, "Bìa có sờn mép nhẹ, ruột sách lành lặn",
                new BigDecimal("70000"), new BigDecimal("35000"), 2, 4,
                "https://images.unsplash.com/photo-1512820790803-83ca734da794?w=500");
    }

    private Category createCategoryIfNotExist(String name, String slug, String description) {
        return categoryRepository.findBySlug(slug)
                .orElseGet(() -> categoryRepository.save(Category.builder()
                        .name(name)
                        .slug(slug)
                        .description(description)
                        .active(true)
                        .build()));
    }

    private void createBookIfNotExist(Shop shop, Category category, String title, String isbn, String author,
                                     String publisher, Integer publishYear, BookCondition condition, String conditionNote,
                                     BigDecimal originalPrice, BigDecimal price, Integer stock, Integer soldQuantity,
                                     String imageUrl) {
        if (!bookRepository.existsByIsbn(isbn)) {
            bookRepository.save(Book.builder()
                    .shop(shop)
                    .category(category)
                    .title(title)
                    .isbn(isbn)
                    .author(author)
                    .publisher(publisher)
                    .publishYear(publishYear)
                    .bookCondition(condition)
                    .conditionNote(conditionNote)
                    .originalPrice(originalPrice)
                    .price(price)
                    .stock(stock)
                    .soldQuantity(soldQuantity)
                    .mainImageUrl(imageUrl)
                    .active(true)
                    .build());
        }
    }
}
