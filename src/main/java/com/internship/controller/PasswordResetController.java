package com.internship.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/password-reset")
@RequiredArgsConstructor
@Tag(name = "Şifre Sıfırlama", description = "Şifre sıfırlama işlemleri")
public class PasswordResetController {
} 