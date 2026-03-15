package com.iuniverse.service;

public interface EmailService {
    void sendResetPasswordEmail(String toEmail, String resetLink);
}
