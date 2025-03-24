package com.internship.controller;

import com.internship.dto.InternshipRequest;
import com.internship.entity.Internship;
import com.internship.entity.InternshipStatus;
import com.internship.entity.User;
import com.internship.service.InternshipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Internship Applications")
@RestController
@RequestMapping("/api/internships")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class InternshipController {

    private final InternshipService internshipService;

    @Operation(summary = "Yeni staj başvurusu oluştur")
    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Internship> createInternship(
            @Valid @RequestBody InternshipRequest request,
            @AuthenticationPrincipal User student) {
        return ResponseEntity.ok(internshipService.createInternship(request, student));
    }

    @Operation(summary = "Staj durumunu güncelle")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADVISOR')")
    public ResponseEntity<Internship> updateStatus(
            @PathVariable Long id,
            @RequestParam InternshipStatus status,
            @RequestParam(required = false) String rejectionReason) {
        return ResponseEntity.ok(internshipService.updateInternshipStatus(id, status, rejectionReason));
    }

    @Operation(summary = "Staja danışman ata")
    @PutMapping("/{id}/advisor")
    @PreAuthorize("hasRole('ADVISOR')")
    public ResponseEntity<Internship> assignAdvisor(
            @PathVariable Long id,
            @AuthenticationPrincipal User advisor) {
        return ResponseEntity.ok(internshipService.assignAdvisor(id, advisor));
    }

    @Operation(summary = "Öğrencinin stajlarını listele")
    @GetMapping("/student")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADVISOR')")
    public ResponseEntity<List<Internship>> getStudentInternships(
            @AuthenticationPrincipal User student) {
        return ResponseEntity.ok(internshipService.getStudentInternships(student));
    }

    @Operation(summary = "Danışmanın stajlarını listele")
    @GetMapping("/advisor")
    @PreAuthorize("hasRole('ADVISOR')")
    public ResponseEntity<List<Internship>> getAdvisorInternships(
            @AuthenticationPrincipal User advisor) {
        return ResponseEntity.ok(internshipService.getAdvisorInternships(advisor));
    }

    @Operation(summary = "Duruma göre stajları listele")
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ADVISOR')")
    public ResponseEntity<List<Internship>> getInternshipsByStatus(
            @PathVariable InternshipStatus status) {
        return ResponseEntity.ok(internshipService.getInternshipsByStatus(status));
    }

    @Operation(summary = "Staj detaylarını getir")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADVISOR', 'ADMIN')")
    public ResponseEntity<Internship> getInternship(@PathVariable Long id) {
        return ResponseEntity.ok(internshipService.getInternshipById(id));
    }

    @Operation(summary = "Staj belgesi yükle")
    @PostMapping("/{id}/document")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Void> uploadDocument(
            @PathVariable Long id,
            @RequestParam String documentPath) {
        internshipService.uploadDocument(id, documentPath);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Staj başvurusunu sil")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteInternship(@PathVariable Long id) {
        internshipService.deleteInternship(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{internshipId}/assign-advisor/{advisorId}")
    @Operation(summary = "Staja danışman ata (Bölüm Koordinatörü)")
    @PreAuthorize("hasRole('ROLE_DEPARTMENT_COORDINATOR')")
    public ResponseEntity<Internship> assignAdvisorByCoordinator(
            @PathVariable Long internshipId,
            @PathVariable Long advisorId) {
        return ResponseEntity.ok(internshipService.assignAdvisorById(internshipId, advisorId));
    }
} 