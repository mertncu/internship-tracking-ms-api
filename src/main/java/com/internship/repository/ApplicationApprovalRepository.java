package com.internship.repository;

import com.internship.entity.ApplicationApproval;
import com.internship.entity.InternshipStatus;
import com.internship.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationApprovalRepository extends JpaRepository<ApplicationApproval, Long> {
    List<ApplicationApproval> findByInternshipIdOrderByActionDateDesc(Long internshipId);
    List<ApplicationApproval> findByApproverOrderByActionDateDesc(User approver);
    List<ApplicationApproval> findByApproverIdOrderByActionDateDesc(Long approverId);
    Optional<ApplicationApproval> findFirstByInternshipIdOrderByActionDateDesc(Long internshipId);
    List<ApplicationApproval> findByInternshipIdAndStatusOrderByActionDateDesc(Long internshipId, InternshipStatus status);
    boolean existsByInternshipIdAndApproverId(Long internshipId, Long approverId);
} 