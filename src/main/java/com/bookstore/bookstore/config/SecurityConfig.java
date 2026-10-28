package com.bookstore.bookstore.config;

import com.bookstore.bookstore.security.CustomUserDetailsService;
import com.bookstore.bookstore.security.JwtAuthenticationFilter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Công khai: Guest, Tài nguyên tĩnh, Auth, Upload API, Error page, Access Denied
                .requestMatchers(
                        "/", "/home", "/books/**", "/categories/**", "/shops/**", "/search/**", "/payment/**",
                        "/auth/**", "/login", "/register", "/verify-otp", "/forgot-password", "/reset-password",
                        "/logout", "/access-denied",
                        "/css/**", "/js/**", "/images/**", "/uploads/**", "/favicon.ico", "/ws/**", "/api/upload/**", "/error", "/api/ai/**", "/library", "/api/library/**"
                ).permitAll()

                // Phân quyền đặc biệt: User đã đăng nhập được vào form đăng ký Shop
                .requestMatchers("/vendor/register").authenticated()

                // Phân quyền theo role
                .requestMatchers("/admin/users/**").hasRole("ADMIN")
                .requestMatchers("/admin/**", "/manager/**").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers("/vendor/**").hasRole("VENDOR")
                .requestMatchers("/shipper/**").hasRole("SHIPPER")
                .requestMatchers("/user/**", "/cart/**", "/checkout/**", "/orders/**", "/chat", "/chat/**", "/api/chat/**", "/api/cart/add").authenticated()

                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    if (request.getRequestURI() != null && request.getRequestURI().startsWith("/api/")) {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write("{\"ok\":false,\"code\":\"LOGIN_REQUIRED\",\"loginUrl\":\"/login\",\"message\":\"Vui lòng đăng nhập để thực hiện thao tác này.\"}");
                    } else {
                        response.sendRedirect("/login");
                    }
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.sendRedirect("/access-denied");
                })
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .addLogoutHandler((request, response, authentication) -> {
                    Cookie cookie = new Cookie("jwt_token", "");
                    cookie.setPath("/");
                    cookie.setMaxAge(0);
                    cookie.setHttpOnly(true);
                    response.addCookie(cookie);

                    Cookie sessionCookie = new Cookie("JSESSIONID", "");
                    sessionCookie.setPath("/");
                    sessionCookie.setMaxAge(0);
                    response.addCookie(sessionCookie);

                    SecurityContextHolder.clearContext();
                })
                .logoutSuccessHandler((request, response, authentication) -> {
                    Cookie cookie = new Cookie("jwt_token", "");
                    cookie.setPath("/");
                    cookie.setMaxAge(0);
                    cookie.setHttpOnly(true);
                    response.addCookie(cookie);

                    Cookie sessionCookie = new Cookie("JSESSIONID", "");
                    sessionCookie.setPath("/");
                    sessionCookie.setMaxAge(0);
                    response.addCookie(sessionCookie);

                    SecurityContextHolder.clearContext();
                    response.sendRedirect("/?logout");
                })
                .deleteCookies("jwt_token", "JSESSIONID")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .permitAll()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
