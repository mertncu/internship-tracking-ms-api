package com.internship.service;

import com.internship.dto.UserDto;
import com.internship.entity.User;

import java.util.List;

public interface UserService {
    User createUser(UserDto userDto);
    List<User> getAllUsers();
    User getUserById(Long id);
    void deleteUser(Long id);
    User updateUserRoles(Long id, List<String> roles);
    boolean existsByEmail(String email);
    void initiatePasswordReset(String email);
    void resetPassword(String token, String newPassword);
} 