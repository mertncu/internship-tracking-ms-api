package com.internship.repository;

import com.internship.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    
    List<Document> findByInternshipId(Long internshipId);
    
//    List<Document> findByInternshipIdAndFileType(Long internshipId, String fileType);
    
//    Optional<Document> findFirstByInternshipIdOrderByUploadedAtDesc(Long internshipId);
    
//    void deleteByInternshipId(Long internshipId);
} 