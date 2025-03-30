package com.internship.service;

public interface EmailService {
    void sendPasswordResetEmail(String email, String token);
    void sendWelcomeEmail(String email, String firstName);
    void sendNotificationEmail(String email, String subject, String message);
} 