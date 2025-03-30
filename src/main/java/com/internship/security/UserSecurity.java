package com.internship.security;

import com.internship.entity.Internship;
import com.internship.entity.Role;
import com.internship.entity.User;
import com.internship.repository.InternshipRepository;
import com.internship.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserSecurity {

    private final UserRepository userRepository;
    private final InternshipRepository internshipRepository;

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public boolean isCurrentUser(Long userId) {
        User currentUser = getCurrentUser();
        return currentUser.getId().equals(userId);
    }

    public boolean hasRole(String roleName) {
        String normalizedRoleName = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;
        User currentUser = getCurrentUser();
        return currentUser.getRoles().stream()
                .map(Role::getName)
                .anyMatch(role -> role.equals(normalizedRoleName));
    }

    public boolean hasAnyRole(String... roleNames) {
        User currentUser = getCurrentUser();
        return currentUser.getRoles().stream()
                .map(Role::getName)
                .anyMatch(role -> {
                    for (String roleName : roleNames) {
                        String normalizedRoleName = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;
                        if (role.equals(normalizedRoleName)) {
                            return true;
                        }
                    }
                    return false;
                });
    }

    public boolean canAccessInternship(Long internshipId) {
        User currentUser = getCurrentUser();
        Internship internship = internshipRepository.findById(internshipId)
                .orElseThrow(() -> new RuntimeException("Internship not found"));

        // Student can only access their own internships
        if (hasRole("STUDENT")) {
            return internship.getStudent().getId().equals(currentUser.getId());
        }

        // Advisor can only access internships they are assigned to
        if (hasRole("FACULTY_ADVISOR")) {
            return internship.getAdvisor() != null && internship.getAdvisor().getId().equals(currentUser.getId());
        }

        // Coordinator can access all internships
        return hasRole("DEPARTMENT_COORDINATOR") || hasRole("UNIVERSITY_COORDINATOR");
    }
} 