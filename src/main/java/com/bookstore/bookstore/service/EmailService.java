package com.bookstore.bookstore.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:your_email@gmail.com}")
    private String mailUsername;

    public void sendOtpEmail(String toEmail, String otpCode, String purpose) {
        if ("your_email@gmail.com".equalsIgnoreCase(mailUsername) || mailUsername == null || mailUsername.isBlank()) {
            log.info("==================================================");
            log.info("[CHẾ ĐỘ GIẢ LẬP TEST] Bỏ qua SMTP do chưa cấu hình Gmail thật");
            log.info("GỬI TỚI: {}", toEmail);
            log.info("MÃ OTP CỦA BẠN LÀ: {}", otpCode);
            log.info("MỤC ĐÍCH: {}", purpose);
            log.info("==================================================");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("[Chuỗi Cửa Hàng Sách Cũ] Mã xác thực OTP: " + otpCode);
            message.setText("Xin chào,\n\n"
                    + "Mã OTP của bạn để " + purpose + " là: " + otpCode + "\n"
                    + "Mã có hiệu lực trong vòng 5 phút. Vui lòng không chia sẻ mã này cho bất kỳ ai.\n\n"
                    + "Trân trọng,\nĐội ngũ Chuỗi Cửa Hàng Sách Cũ.");
            mailSender.send(message);
        } catch (Exception e) {
            log.warn("Lỗi gửi email: {}", e.getMessage());
        }
    }
}
