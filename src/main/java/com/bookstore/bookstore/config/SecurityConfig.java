package com.bookstore.bookstore.config;

import com.bookstore.bookstore.security.CustomUserDetailsService;
import com.bookstore.bookstore.security.JwtAuthenticationFilter;
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
                // Công khai: Guest, Tài nguyên tĩnh, Auth, Upload API, Error page
                .requestMatchers(
                        "/", "/home", "/books/**", "/categories/**", "/shops/**", "/search/**", "/payment/**",
                        "/auth/**", "/login", "/register", "/verify-otp", "/forgot-password", "/reset-password",
                        "/css/**", "/js/**", "/images/**", "/favicon.ico", "/ws/**", "/api/upload/**", "/error"
                ).permitAll()

                // Phân quyền theo role
                .requestMatchers("/admin/users/**").hasRole("ADMIN")
                .requestMatchers("/admin/**", "/manager/**").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers("/vendor/**").hasRole("VENDOR")
                .requestMatchers("/shipper/**").hasRole("SHIPPER")
                .requestMatchers("/user/**", "/cart/**", "/checkout/**", "/orders/**").authenticated()

                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.sendRedirect("/login");
                })
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
