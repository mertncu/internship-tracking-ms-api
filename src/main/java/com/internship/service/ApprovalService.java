package com.internship.service;

import com.internship.entity.ApplicationApproval;
import com.internship.entity.InternshipStatus;

import java.util.List;

public interface ApprovalService {
    ApplicationApproval createApproval(Long internshipId, Long approverId, String approverRole,InternshipStatus status, String comments, boolean isApproved);

    List<ApplicationApproval> getApprovalsByInternshipId(Long internshipId);

    List<ApplicationApproval> getApprovalsByApproverId(Long approverId);

    ApplicationApproval getLatestApproval(Long internshipId);

    List<ApplicationApproval> getApprovalsByRoleAndStatus(Long internshipId, String approverRole, InternshipStatus status);

    boolean hasApprovalFromRole(Long internshipId, String approverRole);

    ApplicationApproval updateApproval(Long approvalId, InternshipStatus newStatus, String comments);
} 