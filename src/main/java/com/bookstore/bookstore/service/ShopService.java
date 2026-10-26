package com.bookstore.bookstore.service;

import com.bookstore.bookstore.dto.ShopRegisterDto;
import com.bookstore.bookstore.dto.VendorDashboardStats;
import com.bookstore.bookstore.entity.Shop;
import com.bookstore.bookstore.entity.User;
import org.springframework.web.multipart.MultipartFile;

public interface ShopService {

    Shop registerShop(User user, ShopRegisterDto dto, MultipartFile logoFile, MultipartFile bannerFile);

    Shop updateShop(User user, ShopRegisterDto dto, MultipartFile logoFile, MultipartFile bannerFile);

    Shop getShopByOwner(User user);

    boolean hasShop(User user);

    VendorDashboardStats getDashboardStats(User vendor);
}
