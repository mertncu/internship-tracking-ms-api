package com.internship.service.impl;

import com.internship.dto.InternshipRequest;
import com.internship.entity.Document;
import com.internship.entity.Internship;
import com.internship.entity.NotificationType;
import com.internship.entity.User;
import com.internship.repository.DocumentRepository;
import com.internship.repository.InternshipRepository;
import com.internship.repository.UserRepository;
import com.internship.service.InternshipService;
import com.internship.service.NotificationService;
import com.internship.security.UserSecurity;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;
import com.internship.exception.ResourceNotFoundException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class InternshipServiceImpl implements InternshipService {
    private final InternshipRepository internshipRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final UserSecurity userSecurity;
    private final DocumentRepository documentRepository;
    private static final Logger logger = LoggerFactory.getLogger(InternshipServiceImpl.class);

    @Override
    public Internship createInternship(InternshipRequest request) {
        User student = userSecurity.getCurrentUser();
        
        Internship internship = Internship.builder()
                .student(student)
                .companyName(request.getCompanyName())
                .companyAddress(request.getCompanyAddress())
                .companyPhone(request.getCompanyPhone())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .workDays(request.getWorkDays())
                .description(request.getDescription())
                .type(request.getType())
                .isPaid(request.getIsPaid() != null ? request.getIsPaid() : false)
                .insuranceSupport(request.getInsuranceSupport() != null ? request.getInsuranceSupport() : false)
                .parentalInsuranceCoverage(request.getParentalInsuranceCoverage() != null ? request.getParentalInsuranceCoverage() : false)
                .build();

        // Eğer ücretli staj ise banka bilgilerini ekle
        if (Boolean.TRUE.equals(request.getIsPaid())) {
            internship.setCompanyIBAN(request.getCompanyIBAN());
            internship.setBankName(request.getBankName());
            internship.setBankBranch(request.getBankBranch());
        }
        
        return internshipRepository.save(internship);
    }

    @Override
    public Internship getInternshipById(Long id) {
        return internshipRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Internship not found with id: " + id));
    }

    @Override
    public List<Internship> getAllInternships() {
        return internshipRepository.findAll();
    }

    @Override
    public List<Internship> getInternshipsByStudent(User student) {
        return internshipRepository.findByStudent(student);
    }

    @Override
    public List<Internship> getInternshipsByAdvisor(User advisor) {
        return internshipRepository.findByAdvisor(advisor);
    }

    @Override
    public Internship assignAdvisor(Long internshipId, Long advisorId) {
        Internship internship = internshipRepository.findById(internshipId)
                .orElseThrow(() -> new ResourceNotFoundException("Internship not found with id: " + internshipId));
        
        User advisor = userRepository.findById(advisorId)
                .orElseThrow(() -> new ResourceNotFoundException("Advisor not found with id: " + advisorId));

        internship.setAdvisor(advisor);
        return internshipRepository.save(internship);
    }

    @Override
    @Transactional
    public String uploadDocument(Long internshipId, MultipartFile file, String documentType) {
        logger.info("Attempting to upload document for internship ID: {} with type: {}", internshipId, documentType);
        
        if (file == null || file.isEmpty()) {
            logger.error("File is null or empty for internship ID: {}", internshipId);
            throw new IllegalArgumentException("File cannot be null or empty");
        }

        try {
            Internship internship = internshipRepository.findById(internshipId)
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

            String uploadDir = createUploadDirectory(internshipId, documentType);
            String newFileName = generateUniqueFileName(internshipId, fileName);
            String filePath = saveFile(file, uploadDir, newFileName);
            
            Document document = Document.builder()
                    .internship(internship)
                    .fileName(fileName)
                    .fileType(documentType)
                    .filePath(filePath)
                    .uploadedAt(LocalDateTime.now())
                    .build();

            documentRepository.save(document);
            logger.info("Document successfully uploaded and saved for internship ID: {}", internshipId);

            notificationService.sendNotification(
                internship.getStudent().getId(),
                "A new document has been uploaded for your internship",
                NotificationType.DOCUMENT_UPLOAD
            );

            return filePath;

        } catch (IOException e) {
            logger.error("Failed to save file for internship ID: {}", internshipId, e);
            throw new RuntimeException("Failed to save file: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error while uploading document for internship ID: {}", internshipId, e);
            throw new RuntimeException("Failed to upload document: " + e.getMessage());
        }
    }

    private boolean isValidFileType(String fileType) {
        return fileType != null && (
            fileType.equals("application/pdf") ||
            fileType.equals("application/msword") ||
            fileType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
        );
    }

    private String createUploadDirectory(Long internshipId, String documentType) throws IOException {
        String uploadDir = String.format("uploads/internship_%d/%s", internshipId, documentType);
        Files.createDirectories(Paths.get(uploadDir));
        return uploadDir;
    }

    private String generateUniqueFileName(Long internshipId, String originalFilename) {
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        return String.format("%d_%s%s", internshipId, UUID.randomUUID().toString(), extension);
    }

    private String saveFile(MultipartFile file, String uploadDir, String newFileName) throws IOException {
        Path targetLocation = Paths.get(uploadDir, newFileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        return targetLocation.toString();
    }

    private void updateInternshipDocuments(Internship internship, Document document) {
        if (internship.getDocuments() == null) {
            internship.setDocuments(new ArrayList<>());
        }
        internship.getDocuments().add(document);
        internshipRepository.save(internship);
    }

    private void notifyAdvisor(Internship internship, String documentType) {
        if (internship.getAdvisor() != null) {
            notificationService.createNotification(
                internship.getAdvisor(),
                "Belge Yüklendi: " + documentType,
                internship.getStudent().getFirstName() + " " + internship.getStudent().getLastName() +
                " stajı için yeni bir " + documentType + " belgesi yükledi.",
                NotificationType.DOCUMENT_UPLOAD
            );
        }
    }

    @Override
    public Internship updateInternship(Internship internship) {
        // İlgili internship'in var olduğunu kontrol et
        getInternshipById(internship.getId());
        
        // Güncellenmiş internship'i kaydet
        return internshipRepository.save(internship);
    }

    @Override
    public void deleteInternship(Long id) {
        Internship internship = getInternshipById(id);
        
        if (internship.getAdvisor() != null) {
            notificationService.createNotification(
                internship.getAdvisor(),
                "Internship Deleted",
                "An internship has been deleted: " + internship.getStudent().getFirstName() + "'s internship",
                NotificationType.STATUS_UPDATE
            );
        }
        
        internshipRepository.delete(internship);
    }
} 