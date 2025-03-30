package com.internship.config;

import com.internship.entity.Role;
import com.internship.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        // Uygulama başlangıcında temel rolleri oluştur
        if (roleRepository.count() == 0) {
            List<Role> roles = Arrays.asList(
                createRole("ROLE_STUDENT", "Student role for internship applications"),
                createRole("ROLE_FACULTY_ADVISOR", "Faculty advisor role for reviewing applications"),
                createRole("ROLE_DEPARTMENT_COORDINATOR", "Department coordinator role for managing department-level processes"),
                createRole("ROLE_UNIVERSITY_COORDINATOR", "University coordinator role for final approvals"),
                createRole("ROLE_ADMIN", "Administrator role for system management"),
                createRole("ROLE_USER", "Default user role")
            );
            
            roleRepository.saveAll(roles);
            System.out.println("Roller başarıyla oluşturuldu.");
        }
    }

    private Role createRole(String name, String description) {
        Role role = new Role();
        role.setName(name);
        return role;
    }
} 