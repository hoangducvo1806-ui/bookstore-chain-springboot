package com.bookstore.bookstore.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Định nghĩa metadata cho các thuộc tính cấu hình tự tạo trong application.properties.
 * Giúp IDE (Spring Tool Suite / Eclipse / IntelliJ) nhận diện và không báo cảnh báo vàng ⚠.
 */
@Configuration
public class AppProperties {

    @Configuration
    @ConfigurationProperties(prefix = "cloudinary")
    @Data
    public static class CloudinaryProps {
        private String cloudName;
        private String apiKey;
        private String apiSecret;
    }

    @Configuration
    @ConfigurationProperties(prefix = "jwt")
    @Data
    public static class JwtProps {
        private String secret;
        private long accessTokenExpiration = 3600000;
        private long refreshTokenExpiration = 604800000;
    }

    @Configuration
    @ConfigurationProperties(prefix = "vnpay")
    @Data
    public static class VnpayProps {
        private String tmnCode;
        private String hashSecret;
        private String payUrl;
        private String returnUrl;
    }

    @Configuration
    @ConfigurationProperties(prefix = "otp")
    @Data
    public static class OtpProps {
        private int expirationMinutes = 5;
    }
}
