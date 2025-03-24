package com.internship.service;

public interface EmailService {
    void sendPasswordResetEmail(String to, String token);
} 