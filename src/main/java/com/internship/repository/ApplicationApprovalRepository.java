package com.internship.repository;

import com.internship.entity.ApplicationApproval;
import com.internship.entity.InternshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationApprovalRepository extends JpaRepository<ApplicationApproval, Long> {
    
    List<ApplicationApproval> findByInternshipIdOrderByActionDateDesc(Long internshipId);
    
    List<ApplicationApproval> findByApproverId(Long approverId);
    
    Optional<ApplicationApproval> findFirstByInternshipIdOrderByActionDateDesc(Long internshipId);
    
    @Query("SELECT a FROM ApplicationApproval a WHERE a.internshipId = ?1 AND a.approverRole = ?2 AND a.resultStatus = ?3")
    List<ApplicationApproval> findByInternshipAndRoleAndStatus(Long internshipId, String approverRole, InternshipStatus status);
    
    boolean existsByInternshipIdAndApproverRole(Long internshipId, String approverRole);
} 