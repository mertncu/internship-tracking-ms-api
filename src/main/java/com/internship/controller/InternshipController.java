package com.internship.controller;

import com.internship.dto.InternshipRequest;
import com.internship.entity.Internship;
import com.internship.entity.User;
import com.internship.security.UserSecurity;
import com.internship.service.InternshipService;
import com.internship.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "Internship Applications")
@RestController
@RequestMapping("/api/internships")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class InternshipController {

    private final InternshipService internshipService;
    private final UserService userService;
    private final UserSecurity userSecurity;

    @Operation(summary = "Yeni staj başvurusu oluştur")
    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Internship> createInternship(@Valid @RequestBody InternshipRequest request) {
        // Ücretli staj için banka bilgilerini kontrol et
        if (Boolean.TRUE.equals(request.getIsPaid()) && !request.isValidBankInfo()) {
            throw new IllegalArgumentException("Ücretli staj için banka bilgileri (IBAN, banka adı ve şube) zorunludur.");
        }
        
        Internship internship = internshipService.createInternship(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(internship);
    }

    @Operation(summary = "Staj detaylarını getir")
    @PreAuthorize("hasRole('STUDENT') or hasRole('FACULTY_ADVISOR') or hasRole('DEPARTMENT_COORDINATOR') or hasRole('UNIVERSITY_COORDINATOR')")
    @GetMapping("/{id}")
    public ResponseEntity<Internship> getInternship(@PathVariable Long id) {
        if (!userSecurity.canAccessInternship(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        Internship internship = internshipService.getInternshipById(id);
        return ResponseEntity.ok(internship);
    }

    @Operation(summary = "Öğrencinin stajlarını listele")
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Internship>> getStudentInternships(@PathVariable Long studentId) {
        User currentUser = userSecurity.getCurrentUser();
        User student = userService.getUserById(studentId);

        if (!userSecurity.hasAnyRole("DEPARTMENT_COORDINATOR", "UNIVERSITY_COORDINATOR") &&
            !currentUser.getId().equals(studentId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(internshipService.getInternshipsByStudent(student));
    }

    @Operation(summary = "Danışmanın stajlarını listele")
    @GetMapping("/advisor/{advisorId}")
    @PreAuthorize("hasRole('FACULTY_ADVISOR') or hasRole('DEPARTMENT_COORDINATOR') or hasRole('UNIVERSITY_COORDINATOR')")
    public ResponseEntity<List<Internship>> getAdvisorInternships(@PathVariable Long advisorId) {
        try {
            User advisor = userService.getUserById(advisorId);
            
            // Yetki kontrolü
            User currentUser = userSecurity.getCurrentUser();
            if (!userSecurity.hasAnyRole("DEPARTMENT_COORDINATOR", "UNIVERSITY_COORDINATOR") &&
                !currentUser.getId().equals(advisorId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            List<Internship> internships = internshipService.getInternshipsByAdvisor(advisor);
            return ResponseEntity.ok(internships);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('DEPARTMENT_COORDINATOR') or hasRole('UNIVERSITY_COORDINATOR')")
    public ResponseEntity<List<Internship>> getAllInternships() {
        return ResponseEntity.ok(internshipService.getAllInternships());
    }

    @Operation(summary = "Staja danışman ata")
    @PutMapping("/{id}/advisor/{advisorId}")
    @PreAuthorize("hasRole('DEPARTMENT_COORDINATOR') or hasRole('UNIVERSITY_COORDINATOR')")
    public ResponseEntity<Internship> assignAdvisor(@PathVariable Long id, @PathVariable Long advisorId) {
        return ResponseEntity.ok(internshipService.assignAdvisor(id, advisorId));
    }

    @Operation(
        summary = "Staj belgesi yükle", 
        description = "Staja ilişkin bir belge yükler. Dosya boyutu 10MB'dan küçük olmalı ve sadece PDF, DOC, DOCX, JPG, JPEG ve PNG formatlarında olmalıdır."
    )
    @PostMapping(value = "/{id}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadDocument(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "type", defaultValue = "GENEL") String documentType) {
        
        if (!userSecurity.canAccessInternship(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        try {
            if (file.isEmpty()) {
                return ResponseEntity
                    .badRequest()
                    .body(Collections.singletonMap("error", "Dosya boş olamaz"));
            }
            
            // Dosya boyutu kontrolü (10MB maksimum)
            if (file.getSize() > 10 * 1024 * 1024) {
                return ResponseEntity
                    .badRequest()
                    .body(Collections.singletonMap("error", "Dosya boyutu 10MB'dan küçük olmalıdır"));
            }
            
            String documentPath = internshipService.uploadDocument(id, file, documentType);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Dosya başarıyla yüklendi");
            response.put("path", documentPath);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @Operation(summary = "Staj başvurusunu sil")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('DEPARTMENT_COORDINATOR') or hasRole('UNIVERSITY_COORDINATOR')")
    public ResponseEntity<Void> deleteInternship(@PathVariable Long id) {
        internshipService.deleteInternship(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Staj başvurusunu güncelle")
    @PutMapping("/{id}")
    @PreAuthorize("@userSecurity.canAccessInternship(#id)")
    public ResponseEntity<Internship> updateInternship(
            @PathVariable Long id,
            @RequestBody InternshipRequest request) {
        
        Internship internship = internshipService.getInternshipById(id);
        
        // Temel bilgileri güncelle
        internship.setCompanyName(request.getCompanyName());
        internship.setCompanyAddress(request.getCompanyAddress());
        internship.setCompanyPhone(request.getCompanyPhone());
        internship.setStartDate(request.getStartDate());
        internship.setEndDate(request.getEndDate());
        internship.setWorkDays(request.getWorkDays());
        internship.setDescription(request.getDescription());
        internship.setType(request.getType());
        internship.setIsPaid(request.getIsPaid() != null ? request.getIsPaid() : false);
        internship.setInsuranceSupport(request.getInsuranceSupport() != null ? request.getInsuranceSupport() : false);
        internship.setParentalInsuranceCoverage(request.getParentalInsuranceCoverage() != null ? request.getParentalInsuranceCoverage() : false);
        
        // Banka bilgilerini güncelle
        if (request.getIsPaid()) {
            internship.setCompanyIBAN(request.getCompanyIBAN());
            internship.setBankName(request.getBankName());
            internship.setBankBranch(request.getBankBranch());
        } else {
            internship.setCompanyIBAN(null);
            internship.setBankName(null);
            internship.setBankBranch(null);
        }
        
        // Kaydet ve yanıt döndür
        Internship updatedInternship = internshipService.updateInternship(internship);
        return ResponseEntity.ok(updatedInternship);
    }
} 