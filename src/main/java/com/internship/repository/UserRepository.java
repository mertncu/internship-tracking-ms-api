package com.internship.repository;

import com.internship.entity.Role;
import com.internship.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByRoles(Role role);
    List<User> findByFacultyAdvisor(User advisor);
    
    // İlişkili entity'leri tek seferde çekmek için optimize edilmiş sorgular
    
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.id = :id")
    Optional<User> findByIdWithRoles(@Param("id") Long id);
    
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.email = :email")
    Optional<User> findByEmailWithRoles(@Param("email") String email);
    
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.roles r WHERE r = :role")
    List<User> findByRolesWithDetails(@Param("role") Role role);
    
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.studentInternships WHERE u.facultyAdvisor = :advisor")
    List<User> findByFacultyAdvisorWithInternships(@Param("advisor") User advisor);
    
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.roles LEFT JOIN FETCH u.studentInternships WHERE u.id = :id")
    Optional<User> findByIdWithRolesAndInternships(@Param("id") Long id);
} 