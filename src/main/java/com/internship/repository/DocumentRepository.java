package com.internship.repository;

import com.internship.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    
    List<Document> findByInternshipId(Long internshipId);
    
    List<Document> findByInternshipIdAndFileType(Long internshipId, String fileType);
    
    Optional<Document> findFirstByInternshipIdOrderByUploadedAtDesc(Long internshipId);
    
    void deleteByInternshipId(Long internshipId);
    
    @Query("SELECT d FROM Document d JOIN FETCH d.internship WHERE d.internship.id = :internshipId")
    List<Document> findByInternshipIdWithInternship(@Param("internshipId") Long internshipId);
    
    @Query("SELECT d FROM Document d JOIN FETCH d.internship WHERE d.id = :id")
    Optional<Document> findByIdWithInternship(@Param("id") Long id);
    
    @Query("SELECT d FROM Document d JOIN FETCH d.internship WHERE d.internship.id = :internshipId AND d.fileType = :fileType")
    List<Document> findByInternshipIdAndFileTypeWithInternship(@Param("internshipId") Long internshipId, @Param("fileType") String fileType);
} 