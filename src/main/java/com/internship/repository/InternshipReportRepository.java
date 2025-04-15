package com.internship.repository;

import com.internship.entity.InternshipReport;
import com.internship.entity.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InternshipReportRepository extends JpaRepository<InternshipReport, Long> {
    
    List<InternshipReport> findByInternshipId(Long internshipId);
    
    List<InternshipReport> findByInternshipIdAndStatus(Long internshipId, ReportStatus status);
    
    Optional<InternshipReport> findFirstByInternshipIdOrderByUploadedAtDesc(Long internshipId);
    
    void deleteByInternshipId(Long internshipId);
    
    @Query("SELECT r FROM InternshipReport r JOIN FETCH r.internship WHERE r.id = :id")
    Optional<InternshipReport> findByIdWithInternship(@Param("id") Long id);
    
    @Query("SELECT r FROM InternshipReport r JOIN FETCH r.internship WHERE r.internship.id = :internshipId")
    List<InternshipReport> findByInternshipIdWithInternship(@Param("internshipId") Long internshipId);
    
    @Query("SELECT r FROM InternshipReport r JOIN FETCH r.internship WHERE r.internship.id = :internshipId AND r.status = :status")
    List<InternshipReport> findByInternshipIdAndStatusWithInternship(@Param("internshipId") Long internshipId, @Param("status") ReportStatus status);
} 