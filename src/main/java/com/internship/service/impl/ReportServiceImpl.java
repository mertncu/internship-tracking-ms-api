package com.internship.service.impl;

import com.internship.entity.Internship;
import com.internship.entity.InternshipReport;
import com.internship.entity.NotificationType;
import com.internship.entity.ReportStatus;
import com.internship.exception.ResourceNotFoundException;
import com.internship.repository.InternshipReportRepository;
import com.internship.repository.InternshipRepository;
import com.internship.service.NotificationService;
import com.internship.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportServiceImpl implements ReportService {

    private final InternshipRepository internshipRepository;
    private final InternshipReportRepository reportRepository;
    private final NotificationService notificationService;
    
    private static final Logger logger = LoggerFactory.getLogger(ReportServiceImpl.class);

    @Override
    public InternshipReport uploadReport(Long internshipId, String title, MultipartFile file, String description) {
        logger.info("Uploading report for internship ID: {}, title: {}", internshipId, title);
        
        if (file == null || file.isEmpty()) {
            logger.error("File is null or empty for internship ID: {}", internshipId);
            throw new IllegalArgumentException("File cannot be null or empty");
        }

        try {
            Internship internship = internshipRepository.findByIdWithDocumentsAndApprovals(internshipId)
                    .orElseThrow(() -> {
                        logger.error("Internship not found with ID: {}", internshipId);
                        return new ResourceNotFoundException("Internship not found with id: " + internshipId);
                    });

            String fileName = StringUtils.cleanPath(file.getOriginalFilename());
            if (fileName.contains("..")) {
                logger.error("Invalid file path sequence found in filename: {}", fileName);
                throw new IllegalArgumentException("Invalid file path sequence in filename");
            }

            String fileType = file.getContentType();
            if (!isValidFileType(fileType)) {
                logger.error("Invalid file type: {} for internship ID: {}", fileType, internshipId);
                throw new IllegalArgumentException("Invalid file type: " + fileType);
            }

            String uploadDir = createUploadDirectory(internshipId);
            String newFileName = generateUniqueFileName(internshipId, fileName);
            String filePath = saveFile(file, uploadDir, newFileName);

            InternshipReport report = InternshipReport.builder()
                    .internship(internship)
                    .title(title)
                    .fileName(fileName)
                    .fileType(fileType)
                    .filePath(filePath)
                    .description(description)
                    .status(ReportStatus.PENDING)
                    .uploadedAt(LocalDateTime.now())
                    .build();

            InternshipReport savedReport = reportRepository.save(report);
            logger.info("Report successfully uploaded and saved with ID: {} for internship ID: {}", savedReport.getId(), internshipId);

            // Öğrenciye bildirim gönder
            notificationService.sendNotification(
                internship.getStudent().getId(),
                "Raporunuz başarıyla yüklendi",
                NotificationType.DOCUMENT_UPLOAD
            );

            // Danışmana bildirim gönder
            if (internship.getAdvisor() != null) {
                notificationService.sendNotification(
                    internship.getAdvisor().getId(),
                    "Öğrenci " + internship.getStudent().getFirstName() + " yeni bir rapor yükledi: " + title,
                    NotificationType.DOCUMENT_UPLOAD
                );
            }

            return savedReport;

        } catch (IOException e) {
            logger.error("Failed to save file for internship ID: {}", internshipId, e);
            throw new RuntimeException("Failed to save file: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error while uploading report for internship ID: {}", internshipId, e);
            throw new RuntimeException("Failed to upload report: " + e.getMessage());
        }
    }

    private boolean isValidFileType(String fileType) {
        return fileType != null && (
            fileType.equals("application/pdf") ||
            fileType.equals("application/msword") ||
            fileType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
        );
    }

    private String createUploadDirectory(Long internshipId) throws IOException {
        String uploadDir = String.format("uploads/internship_%d/reports", internshipId);
        Files.createDirectories(Paths.get(uploadDir));
        return uploadDir;
    }

    private String generateUniqueFileName(Long internshipId, String originalFilename) {
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        return String.format("report_%d_%s%s", internshipId, UUID.randomUUID().toString(), extension);
    }

    private String saveFile(MultipartFile file, String uploadDir, String newFileName) throws IOException {
        Path targetLocation = Paths.get(uploadDir, newFileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        return targetLocation.toString();
    }

    @Override
    public List<InternshipReport> getReportsByInternshipId(Long internshipId) {
        logger.debug("Getting reports for internship ID: {}", internshipId);
        return reportRepository.findByInternshipIdWithInternship(internshipId);
    }

    @Override
    public List<InternshipReport> getReportsByInternshipIdAndStatus(Long internshipId, ReportStatus status) {
        logger.debug("Getting reports for internship ID: {} with status: {}", internshipId, status);
        return reportRepository.findByInternshipIdAndStatusWithInternship(internshipId, status);
    }

    @Override
    public Optional<InternshipReport> getLatestReportByInternshipId(Long internshipId) {
        logger.debug("Getting latest report for internship ID: {}", internshipId);
        return reportRepository.findFirstByInternshipIdOrderByUploadedAtDesc(internshipId);
    }

    @Override
    public Optional<InternshipReport> getReportById(Long reportId) {
        logger.debug("Getting report by ID: {}", reportId);
        return reportRepository.findByIdWithInternship(reportId);
    }

    @Override
    public InternshipReport updateReportStatus(Long reportId, ReportStatus status, String feedback, Integer grade) {
        logger.info("Updating report ID: {} with status: {}", reportId, status);
        
        InternshipReport report = reportRepository.findByIdWithInternship(reportId)
                .orElseThrow(() -> {
                    logger.error("Report not found with ID: {}", reportId);
                    return new ResourceNotFoundException("Report not found with id: " + reportId);
                });
        
        report.setStatus(status);
        
        if (feedback != null && !feedback.isEmpty()) {
            report.setAdvisorFeedback(feedback);
        }
        
        if (grade != null) {
            report.setGrade(grade);
        }
        
        InternshipReport updatedReport = reportRepository.save(report);
        logger.info("Report updated with ID: {}", updatedReport.getId());
        
        // Öğrenciye bildirim gönder
        notificationService.sendNotification(
            report.getInternship().getStudent().getId(),
            "Rapor durumu güncellendi: " + status,
            NotificationType.STATUS_UPDATE
        );
        
        return updatedReport;
    }

    @Override
    public void deleteReport(Long reportId) {
        logger.info("Deleting report with ID: {}", reportId);
        
        InternshipReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> {
                    logger.error("Report not found with ID: {}", reportId);
                    return new ResourceNotFoundException("Report not found with id: " + reportId);
                });
        
        try {
            // Dosyayı sistemden sil
            Path filePath = Paths.get(report.getFilePath());
            Files.deleteIfExists(filePath);
            
            // Raporu veritabanından sil
            reportRepository.delete(report);
            logger.info("Report deleted with ID: {}", reportId);
            
            // Bildirim gönder
            notificationService.sendNotification(
                report.getInternship().getStudent().getId(),
                "Raporunuz silindi",
                NotificationType.STATUS_UPDATE
            );
            
        } catch (IOException e) {
            logger.error("Failed to delete report file for report ID: {}", reportId, e);
            throw new RuntimeException("Failed to delete report file: " + e.getMessage());
        }
    }

    @Override
    public String downloadReportFile(Long reportId) {
        logger.info("Downloading report file for report ID: {}", reportId);
        
        InternshipReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> {
                    logger.error("Report not found with ID: {}", reportId);
                    return new ResourceNotFoundException("Report not found with id: " + reportId);
                });
        
        return report.getFilePath();
    }
} 