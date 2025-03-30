package com.internship.service.impl;

import com.internship.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendPasswordResetEmail(String email, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(email);
        message.setSubject("Şifre Sıfırlama Talebi");
        message.setText("Şifrenizi sıfırlamak için aşağıdaki linke tıklayın:\n\n" +
                "http://localhost:8080/reset-password?token=" + token);
        mailSender.send(message);
    }

    @Override
    public void sendWelcomeEmail(String email, String firstName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(email);
        message.setSubject("Hoş Geldiniz");
        message.setText("Sayın " + firstName + ",\n\n" +
                "Staj Takip Sistemine hoş geldiniz. Sistemi kullanmaya başlayabilirsiniz.");
        mailSender.send(message);
    }

    @Override
    public void sendNotificationEmail(String email, String subject, String messageText) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(email);
        message.setSubject(subject);
        message.setText(messageText);
        mailSender.send(message);
    }
} 