package com.bookstore.bookstore.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Cấu hình Cloudinary để upload ảnh/video (ảnh sách, avatar, logo shop, ảnh/video review)
 * kèm cơ chế phục vụ file tĩnh /uploads/** khi lưu cục bộ.
 */
@Configuration
public class CloudinaryConfig implements WebMvcConfigurer {

    @Bean
    public Cloudinary cloudinary(@Value("${cloudinary.cloud-name:bookstore-local}") String cloudName,
                                 @Value("${cloudinary.api-key:local-mode}") String apiKey,
                                 @Value("${cloudinary.api-secret:local-secret}") String apiSecret) {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
        ));
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadDir = Paths.get("uploads").toAbsolutePath().normalize();
        String uploadPath = uploadDir.toUri().toString();
        if (!uploadPath.endsWith("/")) {
            uploadPath += "/";
        }
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath);
    }
}
