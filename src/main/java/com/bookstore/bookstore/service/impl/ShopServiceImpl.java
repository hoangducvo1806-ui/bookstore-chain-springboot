package com.bookstore.bookstore.service.impl;

import com.bookstore.bookstore.dto.ShopRegisterDto;
import com.bookstore.bookstore.dto.VendorDashboardStats;
import com.bookstore.bookstore.entity.AppCommission;
import com.bookstore.bookstore.entity.Role;
import com.bookstore.bookstore.entity.Shop;
import com.bookstore.bookstore.entity.User;
import com.bookstore.bookstore.enums.OrderStatus;
import com.bookstore.bookstore.enums.RoleName;
import com.bookstore.bookstore.repository.*;
import com.bookstore.bookstore.service.CloudinaryService;
import com.bookstore.bookstore.service.ShopService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShopServiceImpl implements ShopService {

    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BookRepository bookRepository;
    private final OrderRepository orderRepository;
    private final AppCommissionRepository appCommissionRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional
    public Shop registerShop(User user, ShopRegisterDto dto, MultipartFile logoFile, MultipartFile bannerFile) {
        if (shopRepository.existsByOwner(user)) {
            throw new IllegalArgumentException("Tài khoản của bạn đã sở hữu một cửa hàng!");
        }

        if (shopRepository.existsByName(dto.getName().trim())) {
            throw new IllegalArgumentException("Tên cửa hàng '" + dto.getName().trim() + "' đã được sử dụng!");
        }

        String logoUrl = null;
        if (logoFile != null && !logoFile.isEmpty()) {
            try {
                logoUrl = cloudinaryService.uploadImage(logoFile, "shops/logos");
            } catch (Exception e) {
                log.warn("Lỗi upload logo shop: {}", e.getMessage());
            }
        }

        String bannerUrl = null;
        if (bannerFile != null && !bannerFile.isEmpty()) {
            try {
                bannerUrl = cloudinaryService.uploadImage(bannerFile, "shops/banners");
            } catch (Exception e) {
                log.warn("Lỗi upload banner shop: {}", e.getMessage());
            }
        }

        Shop shop = Shop.builder()
                .owner(user)
                .name(dto.getName().trim())
                .phone(dto.getPhone().trim())
                .address(dto.getAddress().trim())
                .description(dto.getDescription())
                .bio(dto.getBio())
                .logoUrl(logoUrl)
                .bannerUrl(bannerUrl)
                .active(true)
                .build();

        shop = shopRepository.save(shop);

        // Khởi tạo mức chiết khấu mặc định 5% cho cửa hàng mới
        if (!appCommissionRepository.existsByShop(shop)) {
            appCommissionRepository.save(AppCommission.builder()
                    .shop(shop)
                    .commissionRate(new BigDecimal("5.00"))
                    .note("Mức chiết khấu mặc định khi mở chi nhánh mới")
                    .build());
        }

        // Tự động gán quyền VENDOR cho User
        Role vendorRole = roleRepository.findByName(RoleName.VENDOR)
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name(RoleName.VENDOR)
                        .description("Chủ cửa hàng chi nhánh bán sách cũ")
                        .build()));

        user.getRoles().add(vendorRole);
        userRepository.save(user);

        log.info("Người dùng {} đã đăng ký cửa hàng thành công: {}", user.getEmail(), shop.getName());
        return shop;
    }

    @Override
    @Transactional
    public Shop updateShop(User user, ShopRegisterDto dto, MultipartFile logoFile, MultipartFile bannerFile) {
        Shop shop = getShopByOwner(user);

        if (!shop.getName().equalsIgnoreCase(dto.getName().trim()) && shopRepository.existsByName(dto.getName().trim())) {
            throw new IllegalArgumentException("Tên cửa hàng '" + dto.getName().trim() + "' đã tồn tại!");
        }

        shop.setName(dto.getName().trim());
        shop.setPhone(dto.getPhone().trim());
        shop.setAddress(dto.getAddress().trim());
        shop.setDescription(dto.getDescription());
        shop.setBio(dto.getBio());

        if (logoFile != null && !logoFile.isEmpty()) {
            try {
                String logoUrl = cloudinaryService.uploadImage(logoFile, "shops/logos");
                shop.setLogoUrl(logoUrl);
            } catch (Exception e) {
                log.warn("Lỗi upload logo: {}", e.getMessage());
            }
        }

        if (bannerFile != null && !bannerFile.isEmpty()) {
            try {
                String bannerUrl = cloudinaryService.uploadImage(bannerFile, "shops/banners");
                shop.setBannerUrl(bannerUrl);
            } catch (Exception e) {
                log.warn("Lỗi upload banner: {}", e.getMessage());
            }
        }

        return shopRepository.save(shop);
    }

    @Override
    @Transactional(readOnly = true)
    public Shop getShopByOwner(User user) {
        return shopRepository.findByOwner(user)
                .orElseThrow(() -> new IllegalArgumentException("Bạn chưa đăng ký cửa hàng!"));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasShop(User user) {
        if (user == null) return false;
        return shopRepository.existsByOwner(user);
    }

    @Override
    @Transactional(readOnly = true)
    public VendorDashboardStats getDashboardStats(User vendor) {
        Shop shop = getShopByOwner(vendor);

        BigDecimal deliveredRevenue = orderRepository.calculateShopDeliveredRevenue(shop);
        BigDecimal totalRevenue = orderRepository.calculateShopTotalRevenue(shop);

        long totalOrders = orderRepository.countByShop(shop);
        long newOrders = orderRepository.countByShopAndStatus(shop, OrderStatus.NEW);
        long confirmedOrders = orderRepository.countByShopAndStatus(shop, OrderStatus.CONFIRMED);
        long shippingOrders = orderRepository.countByShopAndStatus(shop, OrderStatus.SHIPPING);
        long deliveredOrders = orderRepository.countByShopAndStatus(shop, OrderStatus.DELIVERED);
        long cancelledOrders = orderRepository.countByShopAndStatus(shop, OrderStatus.CANCELLED);

        long totalBooks = bookRepository.countByShop(shop);
        long lowStockBooks = bookRepository.countByShopAndStockLessThanEqual(shop, 2);

        Map<String, Long> statusCounts = new LinkedHashMap<>();
        for (OrderStatus st : OrderStatus.values()) {
            statusCounts.put(st.name(), orderRepository.countByShopAndStatus(shop, st));
        }

        return VendorDashboardStats.builder()
                .shopName(shop.getName())
                .shopAddress(shop.getAddress())
                .shopPhone(shop.getPhone())
                .totalDeliveredRevenue(deliveredRevenue != null ? deliveredRevenue : BigDecimal.ZERO)
                .totalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO)
                .totalOrders(totalOrders)
                .newOrdersCount(newOrders)
                .processingOrdersCount(confirmedOrders)
                .shippingOrdersCount(shippingOrders)
                .deliveredOrdersCount(deliveredOrders)
                .cancelledOrdersCount(cancelledOrders)
                .totalBooks(totalBooks)
                .lowStockBooks(lowStockBooks)
                .ordersByStatus(statusCounts)
                .build();
    }
}
