package com.internship.service;

import com.internship.dto.AuthResponse;
import com.internship.dto.LoginRequest;
import com.internship.dto.SignUpRequest;

public interface AuthService {
    AuthResponse signup(SignUpRequest request);
    AuthResponse login(LoginRequest request);
    void sendPasswordResetEmail(String email);
    void resetPassword(String token, String password);
} 