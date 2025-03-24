package com.internship.service.impl;

import com.internship.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendPasswordResetEmail(String to, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Şifre Sıfırlama İsteği");
        message.setText("Şifrenizi sıfırlamak için aşağıdaki linke tıklayın:\n\n" +
                "http://localhost:8080/api/auth/reset-password?token=" + token);
        
        mailSender.send(message);
    }
} 