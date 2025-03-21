package com.internship.service.impl;

import com.internship.dto.UserDto;
import com.internship.entity.PasswordResetToken;
import com.internship.entity.Role;
import com.internship.entity.User;
import com.internship.repository.PasswordResetTokenRepository;
import com.internship.repository.RoleRepository;
import com.internship.repository.UserRepository;
import com.internship.service.EmailService;
import com.internship.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;

    public UserServiceImpl(UserRepository userRepository,
                         RoleRepository roleRepository,
                         @Lazy PasswordEncoder passwordEncoder,
                         PasswordResetTokenRepository passwordResetTokenRepository,
                         EmailService emailService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.emailService = emailService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
    }

    @Override
    public User createUser(UserDto userDto) {
        if (existsByEmail(userDto.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .firstName(userDto.getFirstName())
                .lastName(userDto.getLastName())
                .email(userDto.getEmail())
                .password(passwordEncoder.encode(userDto.getPassword()))
                .phoneNumber(userDto.getPhoneNumber())
                .build();

        Set<Role> roles = new HashSet<>();
        if (userDto.getRoles() != null && !userDto.getRoles().isEmpty()) {
            userDto.getRoles().forEach(roleName -> {
                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
                roles.add(role);
            });
        } else {
            Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new RuntimeException("Default role (ROLE_USER) not found"));
            roles.add(userRole);
        }

        user.setRoles(roles);
        return userRepository.save(user);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    @Override
    public User updateUserRoles(Long id, List<String> roleNames) {
        User user = getUserById(id);
        Set<Role> roles = new HashSet<>();
        
        roleNames.forEach(roleName -> {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
            roles.add(role);
        });

        user.setRoles(roles);
        return userRepository.save(user);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public void initiatePasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Bu email adresi ile kayıtlı kullanıcı bulunamadı."));

        // Varolan tüm token'ları iptal et
        List<PasswordResetToken> existingTokens = passwordResetTokenRepository.findByUserEmail(email);
        existingTokens.forEach(token -> {
            token.setUsed(true);
            passwordResetTokenRepository.save(token);
        });

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
} 