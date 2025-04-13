package com.internship.controller;

import com.internship.dto.ApprovalRequest;
import com.internship.entity.ApplicationApproval;
import com.internship.entity.Internship;
import com.internship.entity.InternshipStatus;
import com.internship.entity.NotificationType;
import com.internship.entity.User;
import com.internship.exception.ResourceNotFoundException;
import com.internship.security.UserSecurity;
import com.internship.service.ApprovalService;
import com.internship.service.InternshipService;
import com.internship.service.NotificationService;
import com.internship.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/approvals")
@RequiredArgsConstructor
@Tag(name = "Approval Controller", description = "API endpoints for managing internship approvals")
public class ApprovalController {
    private static final Logger logger = LoggerFactory.getLogger(ApprovalController.class);
    
    private final ApprovalService approvalService;
    private final InternshipService internshipService;
    private final UserService userService;
    private final UserSecurity userSecurity;
    private final NotificationService notificationService;

    @Operation(summary = "Approve an internship")
    @PostMapping("/{internshipId}/approve")
    @PreAuthorize("hasRole('FACULTY_ADVISOR') or hasRole('DEPARTMENT_COORDINATOR') or hasRole('UNIVERSITY_COORDINATOR')")
    public ResponseEntity<Map<String, Object>> approveInternship(
            @PathVariable Long internshipId,
            @RequestBody ApprovalRequest request) {
        
        logger.info("Approving internship with ID: {}", internshipId);
        User currentUser = userSecurity.getCurrentUser();
        
        // Check if internship exists
        try {
            internshipService.getInternshipById(internshipId);
        } catch (Exception e) {
            logger.error("Internship not found with ID: {}", internshipId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createResponse(false, "Internship not found"));
        }
        
        // Check if the user has permission to approve this internship
        if (!userSecurity.canAccessInternship(internshipId)) {
            logger.error("User {} does not have permission to approve internship {}", currentUser.getId(), internshipId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(createResponse(false, "You don't have permission to approve this internship"));
        }
        
        // Determine the new status based on the user's role
        InternshipStatus newStatus;
        if (userSecurity.hasRole("FACULTY_ADVISOR")) {
            newStatus = InternshipStatus.ADVISOR_APPROVED;
        } else if (userSecurity.hasRole("DEPARTMENT_COORDINATOR") || userSecurity.hasRole("UNIVERSITY_COORDINATOR")) {
            newStatus = InternshipStatus.COORDINATOR_APPROVED;
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(createResponse(false, "Invalid role for approval"));
        }
        
        try {
            ApplicationApproval approval = approvalService.createApproval(
                    internshipId, 
                    currentUser.getId(), 
                    newStatus, 
                    request.getComment());
            
            logger.info("Internship {} approved with status {}", internshipId, newStatus);
            
            Map<String, Object> approvalData = new HashMap<>();
            approvalData.put("id", approval.getId());
            approvalData.put("status", approval.getStatus());
            approvalData.put("comment", approval.getComment());
            approvalData.put("actionDate", approval.getActionDate());
            approvalData.put("approverId", approval.getApprover().getId());
            approvalData.put("internshipId", internshipId);
            
            return ResponseEntity.ok(createResponse(true, "Internship approved successfully", approvalData));
        } catch (Exception e) {
            logger.error("Error approving internship: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponse(false, "Error approving internship: " + e.getMessage()));
        }
    }
    
    @Operation(summary = "Reject an internship")
    @PostMapping("/{internshipId}/reject")
    @PreAuthorize("hasRole('FACULTY_ADVISOR') or hasRole('DEPARTMENT_COORDINATOR') or hasRole('UNIVERSITY_COORDINATOR')")
    public ResponseEntity<Map<String, Object>> rejectInternship(
            @PathVariable Long internshipId,
            @RequestBody ApprovalRequest request) {
        
        logger.info("Rejecting internship with ID: {}", internshipId);
        User currentUser = userSecurity.getCurrentUser();
        
        // Validation checks
        try {
            internshipService.getInternshipById(internshipId);
        } catch (Exception e) {
            logger.error("Internship not found with ID: {}", internshipId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createResponse(false, "Internship not found"));
        }
        
        if (!userSecurity.canAccessInternship(internshipId)) {
            logger.error("User {} does not have permission to reject internship {}", currentUser.getId(), internshipId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(createResponse(false, "You don't have permission to reject this internship"));
        }
        
        if (request.getComment() == null || request.getComment().trim().isEmpty()) {
            logger.error("Rejection reason is required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(createResponse(false, "Rejection reason is required"));
        }
        
        try {
            ApplicationApproval approval = approvalService.createApproval(
                    internshipId, 
                    currentUser.getId(), 
                    InternshipStatus.REJECTED, 
                    request.getComment());
            
            logger.info("Internship {} rejected", internshipId);
            
            Map<String, Object> approvalData = new HashMap<>();
            approvalData.put("id", approval.getId());
            approvalData.put("status", approval.getStatus());
            approvalData.put("comment", approval.getComment());
            approvalData.put("actionDate", approval.getActionDate());
            approvalData.put("approverId", approval.getApprover().getId());
            approvalData.put("internshipId", internshipId);
            
            return ResponseEntity.ok(createResponse(true, "Internship rejected successfully", approvalData));
        } catch (Exception e) {
            logger.error("Error rejecting internship: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponse(false, "Error rejecting internship: " + e.getMessage()));
        }
    }
    
    @Operation(summary = "Request revision for an internship")
    @PostMapping("/{internshipId}/request-revision")
    @PreAuthorize("hasRole('FACULTY_ADVISOR') or hasRole('DEPARTMENT_COORDINATOR') or hasRole('UNIVERSITY_COORDINATOR')")
    public ResponseEntity<Map<String, Object>> requestRevision(
            @PathVariable Long internshipId,
            @RequestBody ApprovalRequest request) {
        
        logger.info("Requesting revision for internship with ID: {}", internshipId);
        User currentUser = userSecurity.getCurrentUser();
        
        // Validation checks
        try {
            internshipService.getInternshipById(internshipId);
        } catch (Exception e) {
            logger.error("Internship not found with ID: {}", internshipId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createResponse(false, "Internship not found"));
        }
        
        if (!userSecurity.canAccessInternship(internshipId)) {
            logger.error("User {} does not have permission to request revision for internship {}", currentUser.getId(), internshipId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(createResponse(false, "You don't have permission to request revision for this internship"));
        }
        
        if (request.getComment() == null || request.getComment().trim().isEmpty()) {
            logger.error("Revision details are required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(createResponse(false, "Revision details are required"));
        }
        
        try {
            ApplicationApproval approval = approvalService.createApproval(
                    internshipId, 
                    currentUser.getId(), 
                    InternshipStatus.PENDING, 
                    request.getComment());
            
            logger.info("Revision requested for internship {}", internshipId);
            
            Map<String, Object> approvalData = new HashMap<>();
            approvalData.put("id", approval.getId());
            approvalData.put("status", approval.getStatus());
            approvalData.put("comment", approval.getComment());
            approvalData.put("actionDate", approval.getActionDate());
            approvalData.put("approverId", approval.getApprover().getId());
            approvalData.put("internshipId", internshipId);
            
            return ResponseEntity.ok(createResponse(true, "Revision requested successfully", approvalData));
        } catch (Exception e) {
            logger.error("Error requesting revision: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponse(false, "Error requesting revision: " + e.getMessage()));
        }
    }
    
    @Operation(summary = "Get all approvals for an internship")
    @GetMapping("/internship/{internshipId}")
    @PreAuthorize("hasRole('STUDENT') or hasRole('FACULTY_ADVISOR') or hasRole('DEPARTMENT_COORDINATOR') or hasRole('UNIVERSITY_COORDINATOR')")
    public ResponseEntity<List<Map<String, Object>>> getApprovalsByInternship(@PathVariable Long internshipId) {
        logger.info("Getting approvals for internship with ID: {}", internshipId);
        
        if (!userSecurity.canAccessInternship(internshipId)) {
            logger.error("User does not have permission to view approvals for internship {}", internshipId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        try {
            List<ApplicationApproval> approvals = approvalService.getApprovalsByInternship(internshipId);
            List<Map<String, Object>> approvalDtos = approvals.stream()
                .map(approval -> {
                    Map<String, Object> approvalDto = new HashMap<>();
                    approvalDto.put("id", approval.getId());
                    approvalDto.put("status", approval.getStatus());
                    approvalDto.put("comment", approval.getComment());
                    approvalDto.put("actionDate", approval.getActionDate());
                    
                    Map<String, Object> approverDto = new HashMap<>();
                    approverDto.put("id", approval.getApprover().getId());
                    approverDto.put("firstName", approval.getApprover().getFirstName());
                    approverDto.put("lastName", approval.getApprover().getLastName());
                    approvalDto.put("approver", approverDto);
                    
                    return approvalDto;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(approvalDtos);
        } catch (Exception e) {
            logger.error("Error retrieving approvals: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @Operation(summary = "Get pending approvals for current user")
    @GetMapping("/pending")
    @PreAuthorize("hasRole('FACULTY_ADVISOR') or hasRole('DEPARTMENT_COORDINATOR') or hasRole('UNIVERSITY_COORDINATOR')")
    public ResponseEntity<List<Map<String, Object>>> getPendingApprovals() {
        logger.info("Getting pending approvals for current user");
        User currentUser = userSecurity.getCurrentUser();
        
        try {
            List<ApplicationApproval> approvals = approvalService.getApprovalsByApprover(currentUser);
            List<Map<String, Object>> approvalDtos = approvals.stream()
                .map(approval -> {
                    Map<String, Object> approvalDto = new HashMap<>();
                    approvalDto.put("id", approval.getId());
                    approvalDto.put("status", approval.getStatus());
                    approvalDto.put("comment", approval.getComment());
                    approvalDto.put("actionDate", approval.getActionDate());
                    
                    Map<String, Object> internshipDto = new HashMap<>();
                    Internship internship = approval.getInternship();
                    internshipDto.put("id", internship.getId());
                    internshipDto.put("companyName", internship.getCompanyName());
                    internshipDto.put("startDate", internship.getStartDate());
                    internshipDto.put("endDate", internship.getEndDate());
                    internshipDto.put("status", internship.getStatus());
                    
                    Map<String, Object> studentDto = new HashMap<>();
                    User student = internship.getStudent();
                    studentDto.put("id", student.getId());
                    studentDto.put("firstName", student.getFirstName());
                    studentDto.put("lastName", student.getLastName());
                    
                    internshipDto.put("student", studentDto);
                    approvalDto.put("internship", internshipDto);
                    
                    return approvalDto;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(approvalDtos);
        } catch (Exception e) {
            logger.error("Error retrieving pending approvals: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    private Map<String, Object> createResponse(boolean success, String message) {
        return createResponse(success, message, null);
    }
    
    private Map<String, Object> createResponse(boolean success, String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", message);
        if (data != null) {
            response.put("data", data);
        }
        return response;
    }
} 