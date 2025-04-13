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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class InternshipServiceImpl implements InternshipService {
    private final InternshipRepository internshipRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final UserSecurity userSecurity;
    private final DocumentRepository documentRepository;

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
        if (!advisor.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_FACULTY_ADVISOR"))) {
            throw new RuntimeException("User is not an advisor");
        }
        return internshipRepository.findByAdvisor(advisor);
    }

    @Override
    public Internship assignAdvisor(Long internshipId, Long advisorId) {
        Internship internship = getInternshipById(internshipId);
        User advisor = userRepository.findById(advisorId)
                .orElseThrow(() -> new RuntimeException("Advisor not found with id: " + advisorId));
        
        if (!advisor.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_FACULTY_ADVISOR"))) {
            throw new RuntimeException("User is not an advisor");
        }
        
        internship.setAdvisor(advisor);
        internshipRepository.save(internship);
        
        notificationService.createNotification(
            advisor,
            "New Internship Assignment",
            "You have been assigned as advisor for " + internship.getStudent().getFirstName() + "'s internship",
            NotificationType.ADVISOR_ASSIGNMENT
        );
        
        return internship;
    }

    @Override
    @Transactional
    public String uploadDocument(Long internshipId, MultipartFile file, String documentType) {
        // 1. Staj başvurusunun varlığını kontrol et
        Internship internship = internshipRepository.findById(internshipId)
                .orElseThrow(() -> new RuntimeException("Staj başvurusu bulunamadı: " + internshipId));

        // 2. Dosya kontrollerini yap
        validateFile(file);
        String originalFilename = getValidFileName(file);
        validateFileExtension(originalFilename);

        try {
            // 3. Dosyayı kaydet
            String uploadDir = createUploadDirectory(internshipId, documentType);
            String newFileName = generateUniqueFileName(internshipId, originalFilename);
            String filePath = saveFile(file, uploadDir, newFileName);

            // 4. Document entity'sini oluştur
            Document document = Document.builder()
                    .internship(internship)
                    .fileName(originalFilename)
                    .fileType(documentType)
                    .filePath(filePath)
                    .description("Staj için " + documentType + " belgesi")
                    .build();

            // 5. Document'ı kaydet
            document = documentRepository.save(document);

            // 6. Internship'i güncelle
            updateInternshipDocuments(internship, document);

            // 7. Danışmanı bilgilendir
            notifyAdvisor(internship, documentType);

            return document.getFilePath();
        } catch (Exception ex) {
            throw new RuntimeException("Belge yükleme işlemi başarısız: " + ex.getMessage(), ex);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Yüklenen dosya boş");
        }

        if (file.getSize() > 10 * 1024 * 1024) { // 10MB limit
            throw new RuntimeException("Dosya boyutu 10MB'dan küçük olmalıdır");
        }
    }

    private String getValidFileName(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.contains("..")) {
            throw new RuntimeException("Dosya adı geçersiz");
        }
        return originalFilename;
    }

    private void validateFileExtension(String fileName) {
        String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        List<String> allowedExtensions = List.of("pdf", "doc", "docx", "jpg", "jpeg", "png");
        
        if (!allowedExtensions.contains(fileExtension)) {
            throw new RuntimeException("Desteklenmeyen dosya türü. Sadece PDF, DOC, DOCX, JPG, JPEG ve PNG dosyaları yüklenebilir.");
        }
    }

    private String createUploadDirectory(Long internshipId, String documentType) {
        String uploadDir = "uploads/documents/" + internshipId + "/" + documentType.toLowerCase();
        File directory = new File(uploadDir);
        if (!directory.exists() && !directory.mkdirs()) {
            throw new RuntimeException("Klasör oluşturulamadı: " + uploadDir);
        }
        return uploadDir;
    }

    private String generateUniqueFileName(Long internshipId, String originalFilename) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String sanitizedFilename = originalFilename.replaceAll("[^a-zA-Z0-9.-]", "_");
        return internshipId + "_" + timestamp + "_" + sanitizedFilename;
    }

    private String saveFile(MultipartFile file, String uploadDir, String newFileName) throws IOException {
        Path targetLocation = Paths.get(uploadDir, newFileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        return uploadDir + "/" + newFileName;
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