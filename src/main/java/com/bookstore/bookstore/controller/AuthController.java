package com.bookstore.bookstore.controller;

import com.bookstore.bookstore.dto.LoginRequest;
import com.bookstore.bookstore.dto.RegisterRequest;
import com.bookstore.bookstore.dto.VerifyOtpRequest;
import com.bookstore.bookstore.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final com.bookstore.bookstore.repository.UserRepository userRepository;

    // --- ĐĂNG NHẬP ---
    @GetMapping("/login")
    public String loginPage(Model model) {
        if (!model.containsAttribute("loginRequest")) {
            model.addAttribute("loginRequest", new LoginRequest());
        }
        return "auth/login";
    }

    @PostMapping("/login")
    public String handleLogin(@Valid @ModelAttribute("loginRequest") LoginRequest request,
                              BindingResult bindingResult,
                              HttpServletResponse response,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "auth/login";
        }

        try {
            String token = authService.login(request);

            // Lưu JWT token vào HttpOnly Cookie để Thymeleaf dùng
            Cookie cookie = new Cookie("jwt_token", token);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(7 * 24 * 60 * 60); // 7 ngày
            response.addCookie(cookie);

            // Điều hướng thông minh theo Vai trò (Role)
            var userOpt = userRepository.findByEmail(request.getEmail().toLowerCase().trim());
            if (userOpt.isPresent()) {
                com.bookstore.bookstore.entity.User user = userOpt.get();
                if (user.isAdmin() || user.isManager()) {
                    return "redirect:/admin/dashboard";
                }
                if (user.isVendor()) {
                    return "redirect:/vendor/books";
                }
            }

            return "redirect:/";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Email hoặc mật khẩu không chính xác hoặc tài khoản chưa kích hoạt!");
            return "redirect:/login";
        }
    }

    // --- ĐĂNG KÝ ---
    @GetMapping("/register")
    public String registerPage(Model model) {
        if (!model.containsAttribute("registerRequest")) {
            model.addAttribute("registerRequest", new RegisterRequest());
        }
        return "auth/register";
    }

    @PostMapping("/register")
    public String handleRegister(@Valid @ModelAttribute("registerRequest") RegisterRequest request,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        try {
            authService.register(request);
            redirectAttributes.addFlashAttribute("email", request.getEmail());
            redirectAttributes.addFlashAttribute("successMessage", "Đăng ký thành công! Vui lòng kiểm tra email để lấy mã OTP kích hoạt.");
            return "redirect:/verify-otp?email=" + request.getEmail();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/register";
        }
    }

    // --- XÁC THỰC OTP ---
    @GetMapping("/verify-otp")
    public String verifyOtpPage(@RequestParam(value = "email", required = false) String email, Model model) {
        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setEmail(email);
        model.addAttribute("verifyOtpRequest", request);
        return "auth/verify-otp";
    }

    @PostMapping("/verify-otp")
    public String handleVerifyOtp(@Valid @ModelAttribute("verifyOtpRequest") VerifyOtpRequest request,
                                  BindingResult bindingResult,
                                  RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "auth/verify-otp";
        }

        try {
            authService.verifyRegistrationOtp(request);
            redirectAttributes.addFlashAttribute("successMessage", "Kích hoạt tài khoản thành công! Bạn có thể đăng nhập ngay.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("email", request.getEmail());
            return "redirect:/verify-otp?email=" + request.getEmail();
        }
    }

    // --- ĐĂNG XUẤT ---
    @GetMapping("/logout")
    public String handleLogout(HttpServletResponse response) {
        Cookie cookie = new Cookie("jwt_token", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0); // Xóa cookie
        response.addCookie(cookie);
        return "redirect:/login";
    }

    // --- QUÊN MẬT KHẨU ---
    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String handleForgotPassword(@RequestParam("email") String email, RedirectAttributes redirectAttributes) {
        try {
            authService.forgotPassword(email);
            redirectAttributes.addFlashAttribute("email", email);
            redirectAttributes.addFlashAttribute("successMessage", "Mã OTP đặt lại mật khẩu đã được gửi đến email của bạn.");
            return "redirect:/reset-password?email=" + email;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/forgot-password";
        }
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam(value = "email", required = false) String email, Model model) {
        model.addAttribute("email", email);
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String handleResetPassword(@RequestParam("email") String email,
                                      @RequestParam("otpCode") String otpCode,
                                      @RequestParam("newPassword") String newPassword,
                                      RedirectAttributes redirectAttributes) {
        try {
            authService.resetPassword(email, otpCode, newPassword);
            redirectAttributes.addFlashAttribute("successMessage", "Đặt lại mật khẩu thành công! Bạn có thể đăng nhập ngay.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("email", email);
            return "redirect:/reset-password?email=" + email;
        }
    }

    // --- REFRESH TOKEN (API) ---
    @PostMapping("/auth/refresh-token")
    @ResponseBody
    public org.springframework.http.ResponseEntity<?> handleRefreshToken(@RequestParam("refreshToken") String refreshToken, HttpServletResponse response) {
        try {
            String newAccessToken = authService.refreshToken(refreshToken);
            Cookie cookie = new Cookie("jwt_token", newAccessToken);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(7 * 24 * 60 * 60);
            response.addCookie(cookie);
            return org.springframework.http.ResponseEntity.ok(java.util.Map.of("accessToken", newAccessToken));
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }
}
