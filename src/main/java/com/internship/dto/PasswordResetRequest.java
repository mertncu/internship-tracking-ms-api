package com.internship.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetRequest {
    private String newPassword;
}

@Data
class PasswordResetConfirmRequest {
    private String token;
    private String newPassword;
} 