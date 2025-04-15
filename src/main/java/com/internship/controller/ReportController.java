package com.internship.controller;

import com.internship.entity.InternshipReport;
import com.internship.entity.ReportStatus;
import com.internship.security.UserSecurity;
import com.internship.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Tag(name = "Internship Reports")
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ReportController {

    private final ReportService reportService;
    private final UserSecurity userSecurity;
    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);

    @Operation(summary = "Staj raporu yükle")
    @PostMapping(value = "/{internshipId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> uploadReport(
            @PathVariable Long internshipId,
            @RequestParam("title") String title,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description) {
        
        logger.info("Uploading report for internship ID: {}, title: {}", internshipId, title);
        
        if (!userSecurity.canAccessInternship(internshipId)) {
            logger.warn("Access denied for user to upload report for internship ID: {}", internshipId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        try {
            InternshipReport report = reportService.uploadReport(internshipId, title, file, description);
            return ResponseEntity.status(HttpStatus.CREATED).body(report);
        } catch (Exception e) {
            logger.error("Error uploading report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Staj raporlarını listele")
    @GetMapping("/internship/{internshipId}")
    @PreAuthorize("hasRole('STUDENT') or hasRole('FACULTY_ADVISOR') or hasRole('DEPARTMENT_COORDINATOR') or hasRole('UNIVERSITY_COORDINATOR')")
    public ResponseEntity<?> getReportsByInternshipId(@PathVariable Long internshipId) {
        
        logger.info("Getting reports for internship ID: {}", internshipId);
        
        if (!userSecurity.canAccessInternship(internshipId)) {
            logger.warn("Access denied for user to view reports for internship ID: {}", internshipId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        List<InternshipReport> reports = reportService.getReportsByInternshipId(internshipId);
        return ResponseEntity.ok(reports);
    }

    @Operation(summary = "Staj raporlarını duruma göre listele")
    @GetMapping("/internship/{internshipId}/status/{status}")
    @PreAuthorize("hasRole('STUDENT') or hasRole('FACULTY_ADVISOR') or hasRole('DEPARTMENT_COORDINATOR') or hasRole('UNIVERSITY_COORDINATOR')")
    public ResponseEntity<?> getReportsByInternshipIdAndStatus(
            @PathVariable Long internshipId,
            @PathVariable ReportStatus status) {
        
        logger.info("Getting reports for internship ID: {} with status: {}", internshipId, status);
        
        if (!userSecurity.canAccessInternship(internshipId)) {
            logger.warn("Access denied for user to view reports for internship ID: {}", internshipId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        List<InternshipReport> reports = reportService.getReportsByInternshipIdAndStatus(internshipId, status);
        return ResponseEntity.ok(reports);
    }

    @Operation(summary = "En son yüklenen staj raporunu getir")
    @GetMapping("/internship/{internshipId}/latest")
    @PreAuthorize("hasRole('STUDENT') or hasRole('FACULTY_ADVISOR') or hasRole('DEPARTMENT_COORDINATOR') or hasRole('UNIVERSITY_COORDINATOR')")
    public ResponseEntity<?> getLatestReportByInternshipId(@PathVariable Long internshipId) {
        
        logger.info("Getting latest report for internship ID: {}", internshipId);
        
        if (!userSecurity.canAccessInternship(internshipId)) {
            logger.warn("Access denied for user to view reports for internship ID: {}", internshipId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        Optional<InternshipReport> reportOpt = reportService.getLatestReportByInternshipId(internshipId);
        if (reportOpt.isPresent()) {
            return ResponseEntity.ok(reportOpt.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Staj raporunu ID'ye göre getir")
    @GetMapping("/{reportId}")
    @PreAuthorize("hasRole('STUDENT') or hasRole('FACULTY_ADVISOR') or hasRole('DEPARTMENT_COORDINATOR') or hasRole('UNIVERSITY_COORDINATOR')")
    public ResponseEntity<?> getReportById(@PathVariable Long reportId) {
        
        logger.info("Getting report by ID: {}", reportId);
        
        Optional<InternshipReport> reportOpt = reportService.getReportById(reportId);
        if (reportOpt.isPresent()) {
            InternshipReport report = reportOpt.get();
            if (!userSecurity.canAccessInternship(report.getInternship().getId())) {
                logger.warn("Access denied for user to view report with ID: {}", reportId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.ok(report);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Staj rapor durumunu güncelle")
    @PutMapping("/{reportId}")
    @PreAuthorize("hasRole('FACULTY_ADVISOR') or hasRole('DEPARTMENT_COORDINATOR') or hasRole('UNIVERSITY_COORDINATOR')")
    public ResponseEntity<?> updateReportStatus(
            @PathVariable Long reportId,
            @RequestParam ReportStatus status,
            @RequestParam(required = false) String feedback,
            @RequestParam(required = false) Integer grade) {
        
        logger.info("Updating report ID: {} with status: {}", reportId, status);
        
        Optional<InternshipReport> reportOpt = reportService.getReportById(reportId);
        if (reportOpt.isPresent()) {
            InternshipReport report = reportOpt.get();
            if (!userSecurity.canAccessInternship(report.getInternship().getId())) {
                logger.warn("Access denied for user to update report with ID: {}", reportId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            try {
                InternshipReport updatedReport = reportService.updateReportStatus(reportId, status, feedback, grade);
                return ResponseEntity.ok(updatedReport);
            } catch (Exception e) {
                logger.error("Error updating report", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Staj raporunu sil")
    @DeleteMapping("/{reportId}")
    @PreAuthorize("hasRole('STUDENT') or hasRole('FACULTY_ADVISOR') or hasRole('DEPARTMENT_COORDINATOR') or hasRole('UNIVERSITY_COORDINATOR')")
    public ResponseEntity<?> deleteReport(@PathVariable Long reportId) {
        
        logger.info("Deleting report with ID: {}", reportId);
        
        Optional<InternshipReport> reportOpt = reportService.getReportById(reportId);
        if (reportOpt.isPresent()) {
            InternshipReport report = reportOpt.get();
            if (!userSecurity.canAccessInternship(report.getInternship().getId())) {
                logger.warn("Access denied for user to delete report with ID: {}", reportId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            try {
                reportService.deleteReport(reportId);
                return ResponseEntity.ok().build();
            } catch (Exception e) {
                logger.error("Error deleting report", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Staj rapor dosyasını indir")
    @GetMapping("/{reportId}/download")
    @PreAuthorize("hasRole('STUDENT') or hasRole('FACULTY_ADVISOR') or hasRole('DEPARTMENT_COORDINATOR') or hasRole('UNIVERSITY_COORDINATOR')")
    public ResponseEntity<?> downloadReportFile(@PathVariable Long reportId) {
        
        logger.info("Downloading report file for report ID: {}", reportId);
        
        Optional<InternshipReport> reportOpt = reportService.getReportById(reportId);
        if (reportOpt.isPresent()) {
            InternshipReport report = reportOpt.get();
            if (!userSecurity.canAccessInternship(report.getInternship().getId())) {
                logger.warn("Access denied for user to download report with ID: {}", reportId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            try {
                String filePath = reportService.downloadReportFile(reportId);
                Path path = Paths.get(filePath);
                Resource resource = new UrlResource(path.toUri());
                
                if (resource.exists() && resource.isReadable()) {
                    return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + report.getFileName() + "\"")
                            .contentType(MediaType.parseMediaType(report.getFileType()))
                            .body(resource);
                } else {
                    logger.error("File not found or not readable: {}", filePath);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                }
            } catch (MalformedURLException e) {
                logger.error("Error downloading report file", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }
} 