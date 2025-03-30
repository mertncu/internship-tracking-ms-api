package com.internship.service;

import com.internship.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User createUser(User user);
    User updateUser(Long id, User user);
    void deleteUser(Long id);
    User getUserById(Long id);
    List<User> getAllUsers();
    List<User> getUsersByRole(String role);
    User updateUserRoles(Long userId, List<String> roleNames);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    void initiatePasswordReset(String email);
    void resetPassword(String token, String newPassword);
} 