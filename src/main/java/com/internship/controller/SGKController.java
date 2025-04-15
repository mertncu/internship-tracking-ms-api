package com.internship.controller;

import com.internship.entity.SGKDeclaration;
import com.internship.security.UserSecurity;
import com.internship.service.SGKService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "SGK Declarations")
@RestController
@RequestMapping("/api/sgk")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class SGKController {

    private final SGKService sgkService;
    private final UserSecurity userSecurity;
    private static final Logger logger = LoggerFactory.getLogger(SGKController.class);

    @Operation(summary = "SGK bildirimi oluştur")
    @PostMapping("/declarations/{internshipId}")
    @PreAuthorize("hasRole('UNIVERSITY_COORDINATOR')")
    public ResponseEntity<?> createDeclaration(
            @PathVariable Long internshipId,
            @RequestParam String declarationNumber) {
        
        logger.info("Creating SGK declaration for internship ID: {} with number: {}", internshipId, declarationNumber);
        
        if (!userSecurity.canAccessInternship(internshipId)) {
            logger.warn("Access denied for user to create SGK declaration for internship ID: {}", internshipId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        try {
            SGKDeclaration declaration = sgkService.createDeclaration(internshipId, declarationNumber);
            Map<String, Object> response = simplifyDeclaration(declaration);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalStateException e) {
            logger.error("Failed to create SGK declaration: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            logger.error("Error creating SGK declaration", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "SGK bildirimini görüntüle")
    @GetMapping("/declarations/internship/{internshipId}")
    @PreAuthorize("hasRole('STUDENT') or hasRole('FACULTY_ADVISOR') or hasRole('DEPARTMENT_COORDINATOR') or hasRole('UNIVERSITY_COORDINATOR')")
    public ResponseEntity<?> getDeclarationByInternshipId(@PathVariable Long internshipId) {
        
        logger.info("Getting SGK declaration for internship ID: {}", internshipId);
        
        if (!userSecurity.canAccessInternship(internshipId)) {
            logger.warn("Access denied for user to view SGK declaration for internship ID: {}", internshipId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return sgkService.getDeclarationByInternshipId(internshipId)
                .map(declaration -> {
                    Map<String, Object> response = simplifyDeclaration(declaration);
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "SGK bildirimini bildirim numarasına göre görüntüle")
    @GetMapping("/declarations/number/{declarationNumber}")
    @PreAuthorize("hasRole('UNIVERSITY_COORDINATOR')")
    public ResponseEntity<?> getDeclarationByNumber(@PathVariable String declarationNumber) {
        
        logger.info("Getting SGK declaration by number: {}", declarationNumber);
        
        return sgkService.getDeclarationByNumber(declarationNumber)
                .map(declaration -> {
                    Map<String, Object> response = simplifyDeclaration(declaration);
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "SGK bildirimini güncelle")
    @PutMapping("/declarations/{declarationId}")
    @PreAuthorize("hasRole('UNIVERSITY_COORDINATOR')")
    public ResponseEntity<?> updateDeclaration(
            @PathVariable Long declarationId,
            @RequestParam boolean isActive,
            @RequestParam(required = false) String notes) {
        
        logger.info("Updating SGK declaration ID: {}, active: {}", declarationId, isActive);
        
        try {
            SGKDeclaration updatedDeclaration = sgkService.updateDeclaration(declarationId, isActive, notes);
            Map<String, Object> response = simplifyDeclaration(updatedDeclaration);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error updating SGK declaration", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "SGK bildirimini sil")
    @DeleteMapping("/declarations/{internshipId}")
    @PreAuthorize("hasRole('UNIVERSITY_COORDINATOR')")
    public ResponseEntity<Void> deleteDeclaration(@PathVariable Long internshipId) {
        
        logger.info("Deleting SGK declaration for internship ID: {}", internshipId);
        
        if (!userSecurity.canAccessInternship(internshipId)) {
            logger.warn("Access denied for user to delete SGK declaration for internship ID: {}", internshipId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        try {
            sgkService.deleteDeclaration(internshipId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error deleting SGK declaration", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "SGK bildirim belgesi oluştur")
    @GetMapping("/declarations/{internshipId}/document")
    @PreAuthorize("hasRole('UNIVERSITY_COORDINATOR')")
    public ResponseEntity<Map<String, String>> generateDeclarationDocument(@PathVariable Long internshipId) {
        
        logger.info("Generating SGK declaration document for internship ID: {}", internshipId);
        
        if (!userSecurity.canAccessInternship(internshipId)) {
            logger.warn("Access denied for user to generate SGK declaration document for internship ID: {}", internshipId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        try {
            String documentPath = sgkService.generateDeclarationDocument(internshipId);
            
            Map<String, String> response = new HashMap<>();
            response.put("documentPath", documentPath);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error generating SGK declaration document", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "SGK bildiriminin işlenip işlenmediğini kontrol et")
    @GetMapping("/declarations/{internshipId}/status")
    @PreAuthorize("hasRole('STUDENT') or hasRole('FACULTY_ADVISOR') or hasRole('DEPARTMENT_COORDINATOR') or hasRole('UNIVERSITY_COORDINATOR')")
    public ResponseEntity<Map<String, Boolean>> isDeclarationProcessed(@PathVariable Long internshipId) {
        
        logger.info("Checking SGK declaration status for internship ID: {}", internshipId);
        
        if (!userSecurity.canAccessInternship(internshipId)) {
            logger.warn("Access denied for user to check SGK declaration status for internship ID: {}", internshipId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        boolean isProcessed = sgkService.isDeclarationProcessed(internshipId);
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("processed", isProcessed);
        
        return ResponseEntity.ok(response);
    }
    
    // Helper method to simplify SGK Declaration for JSON response
    private Map<String, Object> simplifyDeclaration(SGKDeclaration declaration) {
        Map<String, Object> result = new HashMap<>();
        result.put("id", declaration.getId());
        result.put("internshipId", declaration.getInternship().getId());
        result.put("declarationNumber", declaration.getDeclarationNumber());
        result.put("startDate", declaration.getStartDate());
        result.put("endDate", declaration.getEndDate());
        result.put("isActive", declaration.getIsActive());
        result.put("notes", declaration.getNotes());
        
        // Internship bilgilerini sadeleştirme
        Map<String, Object> internshipInfo = new HashMap<>();
        internshipInfo.put("id", declaration.getInternship().getId());
        internshipInfo.put("companyName", declaration.getInternship().getCompanyName());
        internshipInfo.put("startDate", declaration.getInternship().getStartDate());
        internshipInfo.put("endDate", declaration.getInternship().getEndDate());
        internshipInfo.put("status", declaration.getInternship().getStatus().name());
        internshipInfo.put("studentId", declaration.getInternship().getStudent().getId());
        
        result.put("internship", internshipInfo);
        return result;
    }
} 