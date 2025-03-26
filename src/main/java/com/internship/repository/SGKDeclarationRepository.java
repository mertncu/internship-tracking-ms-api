package com.internship.repository;

import com.internship.entity.SGKDeclaration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SGKDeclarationRepository extends JpaRepository<SGKDeclaration, Long> {
    
    Optional<SGKDeclaration> findByInternshipId(Long internshipId);
    
    Optional<SGKDeclaration> findByDeclarationNumber(String declarationNumber);
    
    boolean existsByInternshipId(Long internshipId);
    
    void deleteByInternshipId(Long internshipId);
} 