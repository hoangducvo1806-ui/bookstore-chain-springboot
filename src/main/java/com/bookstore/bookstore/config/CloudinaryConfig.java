package com.bookstore.bookstore.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình Cloudinary để upload ảnh/video (ảnh sách, avatar, logo shop, ảnh/video review).
 * Thông tin key đặt trong application-local.properties (KHÔNG commit lên GitHub).
 * TODO (TV3): viết CloudinaryService (upload, delete) dùng chung cho cả nhóm.
 */
@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary(@Value("${cloudinary.cloud-name:your_cloud_name}") String cloudName,
                                 @Value("${cloudinary.api-key:your_api_key}") String apiKey,
                                 @Value("${cloudinary.api-secret:your_api_secret}") String apiSecret) {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
        ));
    }
}
