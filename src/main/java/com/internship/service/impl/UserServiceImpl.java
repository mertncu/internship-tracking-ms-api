package com.internship.service.impl;

import com.internship.dto.UserDto;
import com.internship.entity.PasswordResetToken;
import com.internship.entity.Role;
import com.internship.entity.User;
import com.internship.exception.ResourceNotFoundException;
import com.internship.repository.PasswordResetTokenRepository;
import com.internship.repository.RoleRepository;
import com.internship.repository.UserRepository;
import com.internship.service.EmailService;
import com.internship.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
    }

    @Override
    @Transactional
    public User createUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        // Convert Set<Role> to List<Role> if needed
        if (user.getRoles() != null && user.getRoles() instanceof Set) {
            List<Role> roleList = new ArrayList<>(user.getRoles());
            user.setRoles(roleList);
        }
        
        // Ensure user has at least one role
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            Role defaultRole = roleRepository.findByName("STUDENT")
                    .orElseThrow(() -> new RuntimeException("Default role not found"));
            user.setRoles(Collections.singletonList(defaultRole));
        }

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User updateUser(Long id, User user) {
        User existingUser = getUserById(id);

        if (user.getFirstName() != null) {
            existingUser.setFirstName(user.getFirstName());
        }
        if (user.getLastName() != null) {
            existingUser.setLastName(user.getLastName());
        }
        if (user.getPassword() != null) {
            existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        if (user.getRoles() != null) {
            existingUser.setRoles(user.getRoles());
        }

        return userRepository.save(existingUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public List<User> getUsersByRole(String roleName) {
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));
        return userRepository.findByRoles(role);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public void initiatePasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Bu email adresi ile kayıtlı kullanıcı bulunamadı."));

        // Check existing tokens and invalidate them if needed
        List<PasswordResetToken> existingTokens = new ArrayList<>();
        Optional<PasswordResetToken> tokenOptional = passwordResetTokenRepository.findByUserEmail(email);
        if (tokenOptional.isPresent()) {
            PasswordResetToken token = tokenOptional.get();
            token.setUsed(true);
            passwordResetTokenRepository.save(token);
            existingTokens.add(token);
        }

        // Yeni token oluştur
        String token = UUID.randomUUID().toString();
        PasswordResetToken passwordResetToken = new PasswordResetToken();
        passwordResetToken.setToken(token);
        passwordResetToken.setUser(user);
        passwordResetToken.setExpiryDate(LocalDateTime.now().plusHours(24));
        passwordResetToken.setUsed(false);
        
        passwordResetTokenRepository.save(passwordResetToken);
        emailService.sendPasswordResetEmail(user.getEmail(), token);
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Geçersiz veya süresi dolmuş token."));

        if (passwordResetToken.isExpired()) {
            throw new RuntimeException("Token süresi dolmuş.");
        }

        if (passwordResetToken.isUsed()) {
            throw new RuntimeException("Bu token daha önce kullanılmış.");
        }

        User user = passwordResetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        passwordResetToken.setUsed(true);
        
        userRepository.save(user);
        passwordResetTokenRepository.save(passwordResetToken);
    }

    @Override
    @Transactional
    public User updateUserRoles(Long userId, List<String> roleNames) {
        User user = getUserById(userId);
        List<Role> roles = new ArrayList<>();

        for (String roleName : roleNames) {
            // Ensure role names have ROLE_ prefix
            String normalizedRoleName = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;
            Role role = roleRepository.findByName(normalizedRoleName)
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + normalizedRoleName));
            roles.add(role);
        }

        user.setRoles(roles);
        return userRepository.save(user);
    }
} 