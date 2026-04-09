package com.iuniverse.service.impl;

import com.iuniverse.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "EMAIL-SERVICE")
public class EmailServiceImpl implements EmailService {

    @Value("${spring.mail.username}")
    private String senderEmail;

    private final JavaMailSender mailSender;

    @Override
    @Async
    public void sendResetPasswordEmail(String toEmail, String otp) {
        try {
            log.info("Starting to send reset password email to: {}", toEmail);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(senderEmail); // Email gửi đi
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
            log.error("Failed to send forgot password email to {}: {}", toEmail, e.getMessage());
        }
    }

    @Override
    @Async
    public void sendOtpEmail(String email, String otp) {
        log.info("Bắt đầu gửi email OTP tới: {}", email);

        try {
            // Tạo một MimeMessage để có thể gửi email định dạng HTML
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail);
            helper.setTo(email);
            helper.setSubject("Xác thực tài khoản IUniverse LMS");

            // Thiết kế Template HTML cho email
            String htmlContent = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;'>"
                    + "<h2 style='color: #2c3e50; text-align: center;'>Chào mừng đến với IUniverse LMS!</h2>"
                    + "<p style='font-size: 16px; color: #333;'>Xin chào,</p>"
                    + "<p style='font-size: 16px; color: #333;'>Để hoàn tất quá trình đăng ký tài khoản, vui lòng sử dụng mã OTP dưới đây:</p>"
                    + "<div style='text-align: center; margin: 30px 0;'>"
                    + "  <span style='font-size: 32px; font-weight: bold; color: #e74c3c; letter-spacing: 8px; padding: 10px 20px; background-color: #f9ecec; border-radius: 5px;'>" + otp + "</span>"
                    + "</div>"
                    + "<p style='font-size: 16px; color: #333;'>Mã này có hiệu lực trong vòng <strong>5 phút</strong>.</p>"
                    + "<p style='font-size: 14px; color: #7f8c8d; border-top: 1px solid #eee; padding-top: 15px; mt-4'><em>Lưu ý: Không chia sẻ mã này cho bất kỳ ai. Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email.</em></p>"
                    + "</div>";

            helper.setText(htmlContent, true); // true = Bật chế độ HTML

            mailSender.send(message);
            log.info("Send email OTP successfully to: {}", email);

        } catch (MessagingException e) {
            log.error("Failed to send otp email to {}: {}", email, e.getMessage());
        }
    }
}