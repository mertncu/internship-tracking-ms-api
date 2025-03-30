package com.internship.service.impl;

import com.internship.entity.*;
import com.internship.repository.ApplicationApprovalRepository;
import com.internship.repository.InternshipRepository;
import com.internship.repository.UserRepository;
import com.internship.service.ApprovalService;
import com.internship.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ApprovalServiceImpl implements ApprovalService {
    private final ApplicationApprovalRepository approvalRepository;
    private final InternshipRepository internshipRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Override
    public ApplicationApproval createApproval(Long internshipId, Long approverId, InternshipStatus status, String comment) {
        Internship internship = internshipRepository.findById(internshipId)
                .orElseThrow(() -> new RuntimeException("Internship not found"));
        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new RuntimeException("Approver not found"));

        ApplicationApproval approval = ApplicationApproval.builder()
                .internship(internship)
                .approver(approver)
                .status(status)
                .comment(comment)
                .actionDate(LocalDateTime.now())
                .build();

        internship.setStatus(status);
        internshipRepository.save(internship);

        notificationService.createNotification(
            internship.getStudent(),
            "Internship Status Updated",
            "Your internship status has been updated to: " + status + (comment != null ? ". Comment: " + comment : ""),
            NotificationType.STATUS_UPDATE
        );

        return approvalRepository.save(approval);
    }

    @Override
    public ApplicationApproval updateApproval(Long approvalId, InternshipStatus status, String comment) {
        ApplicationApproval approval = approvalRepository.findById(approvalId)
                .orElseThrow(() -> new RuntimeException("Approval not found"));

        approval.setStatus(status);
        approval.setComment(comment);
        approval.setActionDate(LocalDateTime.now());

        Internship internship = approval.getInternship();
        internship.setStatus(status);
        internshipRepository.save(internship);

        notificationService.createNotification(
            internship.getStudent(),
            "Internship Status Updated",
            "Your internship status has been updated to: " + status + (comment != null ? ". Comment: " + comment : ""),
            NotificationType.STATUS_UPDATE
        );

        return approvalRepository.save(approval);
    }

    @Override
    public List<ApplicationApproval> getApprovalsByInternship(Long internshipId) {
        return approvalRepository.findByInternshipIdOrderByActionDateDesc(internshipId);
    }

    @Override
    public List<ApplicationApproval> getApprovalsByApprover(User approver) {
        return approvalRepository.findByApproverOrderByActionDateDesc(approver);
    }

    @Override
    public List<ApplicationApproval> getApprovalsByInternshipId(Long internshipId) {
        return approvalRepository.findByInternshipIdOrderByActionDateDesc(internshipId);
    }

    @Override
    public List<ApplicationApproval> getApprovalsByApproverId(Long approverId) {
        return approvalRepository.findByApproverIdOrderByActionDateDesc(approverId);
    }

    @Override
    public ApplicationApproval getLatestApproval(Long internshipId) {
        return approvalRepository.findFirstByInternshipIdOrderByActionDateDesc(internshipId)
                .orElseThrow(() -> new RuntimeException("No approval found for internship: " + internshipId));
    }

    @Override
    public List<ApplicationApproval> getApprovalsByRoleAndStatus(Long internshipId, String approverRole, InternshipStatus status) {
        return approvalRepository.findByInternshipIdAndStatusOrderByActionDateDesc(internshipId, status)
            .stream()
            .filter(approval -> approval.getApprover().getRole().equals(approverRole))
            .collect(Collectors.toList());
    }

    @Override
    public boolean hasApprovalFromRole(Long internshipId, String approverRole) {
        return approvalRepository.findByInternshipIdOrderByActionDateDesc(internshipId)
            .stream()
            .anyMatch(approval -> approval.getApprover().getRole().equals(approverRole));
    }
} 