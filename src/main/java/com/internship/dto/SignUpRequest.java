package com.internship.dto;

import lombok.Data;

import java.util.List;

@Data
public class SignUpRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String phoneNumber;
    private List<String> roles;
} 