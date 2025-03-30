package com.internship.controller;

import com.internship.dto.AuthResponse;
import com.internship.dto.LoginRequest;
import com.internship.dto.SignUpRequest;
import com.internship.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Kullanıcı kaydı", description = "Yeni kullanıcı kaydı oluşturur")
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody SignUpRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @Operation(summary = "Kullanıcı girişi", description = "Kullanıcı girişi yaparak JWT token alır")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "Şifre sıfırlama e-postası", description = "Kullanıcıya şifre sıfırlama e-postası gönderir")
    @PostMapping("/password-reset-request")
    public ResponseEntity<Void> sendPasswordResetEmail(@RequestParam String email) {
        authService.sendPasswordResetEmail(email);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Şifre sıfırlama", description = "Şifre sıfırlama token'ı ile yeni şifre belirler")
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestParam String token, @RequestParam String password) {
        authService.resetPassword(token, password);
        return ResponseEntity.ok().build();
    }
} 