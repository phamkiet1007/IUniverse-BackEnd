package com.iuniverse.service.impl;

import com.iuniverse.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "EMAIL-SERVICE")
public class EmailServiceImpl implements EmailService {

    @Value("${spring.mail.username}")
    private String username;

    private final JavaMailSender mailSender;

    @Override
    @Async
    public void sendResetPasswordEmail(String toEmail, String otp) {
        try {
            log.info("Starting to send reset password email to: {}", toEmail);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(username); // Email gửi đi
            message.setTo(toEmail); // Email người nhận
            message.setSubject("[IUniverse] Yêu cầu đặt lại mật khẩu"); // Tiêu đề

            String emailText = "Xin chào,\n\n" +
                    "Bạn đã yêu cầu đặt lại mật khẩu. Dưới đây là mã xác nhận (OTP) của bạn:\n\n" +
                    "Mã OTP: " + otp + "\n\n" + // In mã OTP ra đây
                    "Mã này có hiệu lực trong vòng 5 phút.\n" +
                    "Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email này để bảo vệ tài khoản.\n\n" +
                    "Trân trọng,\nĐội ngũ IUniverse.";

            message.setText(emailText);

            mailSender.send(message);
            log.info("Reset password email sent successfully to: {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }
}