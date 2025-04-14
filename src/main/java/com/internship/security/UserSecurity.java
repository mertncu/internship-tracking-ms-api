package com.internship.security;

import com.internship.entity.Internship;
import com.internship.entity.Role;
import com.internship.entity.User;
import com.internship.repository.InternshipRepository;
import com.internship.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserSecurity {

    private final UserRepository userRepository;
    private final InternshipRepository internshipRepository;
    private static final Logger logger = LoggerFactory.getLogger(UserSecurity.class);

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmailWithRoles(authentication.getName())
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
        try {
            User currentUser = getCurrentUser();
            logger.debug("Checking access to internship ID: {} for user: {}", internshipId, currentUser.getEmail());
            
            // Artık koleksiyonlar Set tipinde olduğu için birden fazla koleksiyonu aynı anda fetch edebiliriz
            Internship internship = internshipRepository.findByIdWithDocumentsAndApprovals(internshipId)
                    .orElseThrow(() -> {
                        logger.warn("Internship not found with ID: {}", internshipId);
                        return new RuntimeException("Internship not found with ID: " + internshipId);
                    });

            // Öğrenciler sadece kendi stajlarına erişebilir
            if (hasRole("STUDENT")) {
                boolean hasAccess = internship.getStudent().getId().equals(currentUser.getId());
                logger.debug("Student access check: {}", hasAccess);
                return hasAccess;
            }

            // Danışmanlar sadece kendilerine atanmış stajlara erişebilir
            if (hasRole("FACULTY_ADVISOR")) {
                boolean hasAccess = internship.getAdvisor() != null && 
                                  internship.getAdvisor().getId().equals(currentUser.getId());
                logger.debug("Faculty advisor access check: {}", hasAccess);
                return hasAccess;
            }

            // Koordinatörler tüm stajlara erişebilir
            boolean hasAccess = hasRole("DEPARTMENT_COORDINATOR") || hasRole("UNIVERSITY_COORDINATOR");
            logger.debug("Coordinator access check: {}", hasAccess);
            return hasAccess;
        } catch (Exception e) {
            logger.error("Error checking access to internship ID: {}", internshipId, e);
            return false;
        }
    }
} 