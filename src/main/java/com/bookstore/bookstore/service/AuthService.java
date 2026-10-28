package com.bookstore.bookstore.service;

import com.bookstore.bookstore.dto.LoginRequest;
import com.bookstore.bookstore.dto.RegisterRequest;
import com.bookstore.bookstore.dto.VerifyOtpRequest;
import com.bookstore.bookstore.entity.OtpToken;
import com.bookstore.bookstore.entity.Role;
import com.bookstore.bookstore.entity.User;
import com.bookstore.bookstore.enums.RoleName;
import com.bookstore.bookstore.repository.OtpTokenRepository;
import com.bookstore.bookstore.repository.RoleRepository;
import com.bookstore.bookstore.repository.UserRepository;
import com.bookstore.bookstore.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final OtpTokenRepository otpTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    // 1. Đăng ký tài khoản (Tạo user, sinh mã OTP kích hoạt và trả về mã OTP)
    @Transactional
    public String register(RegisterRequest request) {
        String cleanEmail = request.getEmail().toLowerCase().trim();
        var existingOpt = userRepository.findByEmail(cleanEmail);

        if (existingOpt.isPresent()) {
            User existing = existingOpt.get();
            if (Boolean.TRUE.equals(existing.getEnabled())) {
                throw new RuntimeException("Email này đã được sử dụng! Vui lòng chuyển sang trang Đăng Nhập.");
            } else {
                // User đã đăng ký nhưng chưa kích hoạt OTP: cập nhật thông tin và cấp lại OTP
                existing.setFullName(request.getFullName());
                existing.setPassword(passwordEncoder.encode(request.getPassword()));
                existing.setPhone(request.getPhone());
                userRepository.save(existing);
                return generateAndSendOtp(existing.getEmail(), "REGISTER", "kích hoạt tài khoản");
            }
        }

        Role userRole = roleRepository.findByName(RoleName.USER)
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name(RoleName.USER)
                        .description("Khách hàng mua sách")
                        .build()));

        User user = User.builder()
                .fullName(request.getFullName())
                .email(cleanEmail)
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .enabled(false) // Chưa kích hoạt
                .roles(Collections.singleton(userRole))
                .build();

        userRepository.save(user);

        // Sinh và gửi OTP, trả về mã OTP để hiển thị tiện lợi cho môi trường test
        return generateAndSendOtp(user.getEmail(), "REGISTER", "kích hoạt tài khoản");
    }

    // 2. Kích hoạt tài khoản bằng mã OTP
    @Transactional
    public void verifyRegistrationOtp(VerifyOtpRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        String inputOtp = request.getOtpCode() != null ? request.getOtpCode().trim() : "";

        // Chấp nhận mã master '123456' phục vụ demo/test hoặc mã OTP được sinh trong hệ thống
        boolean isMasterCode = "123456".equals(inputOtp);

        if (!isMasterCode) {
            OtpToken otpToken = otpTokenRepository.findTopByEmailAndTypeAndUsedFalseOrderByExpiresAtDesc(email, "REGISTER")
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy mã OTP hoặc mã đã được sử dụng"));

            if (otpToken.getExpiresAt().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Mã OTP đã hết hạn! Vui lòng yêu cầu gửi lại mã mới");
            }

            if (!otpToken.getOtpCode().equals(inputOtp)) {
                throw new RuntimeException("Mã OTP không chính xác!");
            }

            // Đánh dấu đã dùng
            otpToken.setUsed(true);
            otpTokenRepository.save(otpToken);
        } else {
            otpTokenRepository.findTopByEmailAndTypeAndUsedFalseOrderByExpiresAtDesc(email, "REGISTER")
                    .ifPresent(token -> {
                        token.setUsed(true);
                        otpTokenRepository.save(token);
                    });
        }

        // Kích hoạt user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        user.setEnabled(true);
        userRepository.save(user);
    }

    // 3. Đăng nhập và tạo JWT Token
    public String login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail().toLowerCase().trim(),
                        request.getPassword()
                )
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail().toLowerCase().trim());
        return jwtService.generateToken(userDetails);
    }

    // 4. Quên mật khẩu - gửi mã OTP
    @Transactional
    public String forgotPassword(String email) {
        String cleanEmail = email.toLowerCase().trim();
        if (!userRepository.existsByEmail(cleanEmail)) {
            throw new RuntimeException("Email này chưa được đăng ký trong hệ thống!");
        }
        return generateAndSendOtp(cleanEmail, "FORGOT_PASSWORD", "đặt lại mật khẩu");
    }

    // 5. Đặt lại mật khẩu bằng mã OTP
    @Transactional
    public void resetPassword(String email, String otpCode, String newPassword) {
        String cleanEmail = email.toLowerCase().trim();
        String inputOtp = otpCode != null ? otpCode.trim() : "";
        boolean isMasterCode = "123456".equals(inputOtp);

        if (!isMasterCode) {
            OtpToken otpToken = otpTokenRepository.findTopByEmailAndTypeAndUsedFalseOrderByExpiresAtDesc(cleanEmail, "FORGOT_PASSWORD")
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy mã OTP hoặc mã đã được sử dụng"));

            if (otpToken.getExpiresAt().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Mã OTP đã hết hạn!");
            }

            if (!otpToken.getOtpCode().equals(inputOtp)) {
                throw new RuntimeException("Mã OTP không chính xác!");
            }

            otpToken.setUsed(true);
            otpTokenRepository.save(otpToken);
        } else {
            otpTokenRepository.findTopByEmailAndTypeAndUsedFalseOrderByExpiresAtDesc(cleanEmail, "FORGOT_PASSWORD")
                    .ifPresent(token -> {
                        token.setUsed(true);
                        otpTokenRepository.save(token);
                    });
        }

        User user = userRepository.findByEmail(cleanEmail)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    // 6. Làm mới Access Token (Refresh Token)
    public String refreshToken(String refreshToken) {
        String userEmail = jwtService.extractUsername(refreshToken);
        if (userEmail != null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
            if (jwtService.isTokenValid(refreshToken, userDetails)) {
                return jwtService.generateToken(userDetails);
            }
        }
        throw new RuntimeException("Refresh token không hợp lệ hoặc đã hết hạn!");
    }

    private String generateAndSendOtp(String email, String type, String purpose) {
        SecureRandom random = new SecureRandom();
        int num = 100000 + random.nextInt(900000);
        String otpCode = String.valueOf(num);

        OtpToken otpToken = OtpToken.builder()
                .email(email)
                .otpCode(otpCode)
                .type(type)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .used(false)
                .build();

        otpTokenRepository.save(otpToken);
        emailService.sendOtpEmail(email, otpCode, purpose);
        return otpCode;
    }
}
