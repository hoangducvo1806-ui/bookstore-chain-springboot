package com.bookstore.bookstore.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Cấu hình Spring Security (khung ban đầu).
 * TODO (TV3): thêm JwtAuthenticationFilter, xử lý đăng nhập JWT lưu trong HttpOnly Cookie,
 * trang lỗi 403/404, refresh token.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /** Mã hóa mật khẩu bằng BCrypt (yêu cầu bắt buộc của đề). */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.ignoringRequestMatchers("/ws/**", "/api/**", "/payment/**"))
            .authorizeHttpRequests(auth -> auth
                // Tài nguyên tĩnh + trang công khai (Guest)
                .requestMatchers("/", "/home", "/books/**", "/shops/**", "/search/**",
                        "/auth/**", "/error/**", "/ws/**",
                        "/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                // Phân quyền theo role
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/manager/**").hasAnyRole("MANAGER", "ADMIN")
                .requestMatchers("/vendor/**").hasRole("VENDOR")
                .requestMatchers("/shipper/**").hasRole("SHIPPER")
                .requestMatchers("/user/**", "/cart/**", "/checkout/**", "/orders/**")
                    .hasAnyRole("USER", "VENDOR")
                // TODO (TV3): đổi thành .authenticated() khi đã có JWT filter
                .anyRequest().permitAll()
            )
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable());

        return http.build();
    }
}
