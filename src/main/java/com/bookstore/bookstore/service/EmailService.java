package com.bookstore.bookstore.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendOtpEmail(String toEmail, String otpCode, String purpose) {
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
            // Khi chưa cấu hình Gmail thật, in OTP ra console để test
            System.out.println("==================================================");
            System.out.println("GỬI EMAIL THẤT BẠI (Chưa có cấu hình Gmail thật)");
            System.out.println("GỬI TỚI: " + toEmail);
            System.out.println("MÃ OTP CỦA BẠN LÀ: " + otpCode);
            System.out.println("MỤC ĐÍCH: " + purpose);
            System.out.println("==================================================");
        }
    }
}
