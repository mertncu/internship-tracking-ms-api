package com.internship.service.impl;

import com.internship.entity.Internship;
import com.internship.entity.NotificationType;
import com.internship.entity.SGKDeclaration;
import com.internship.exception.ResourceNotFoundException;
import com.internship.repository.InternshipRepository;
import com.internship.repository.SGKDeclarationRepository;
import com.internship.service.NotificationService;
import com.internship.service.SGKService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class SGKServiceImpl implements SGKService {

    private final SGKDeclarationRepository sgkDeclarationRepository;
    private final InternshipRepository internshipRepository;
    private final NotificationService notificationService;
    
    private static final Logger logger = LoggerFactory.getLogger(SGKServiceImpl.class);

    @Override
    public SGKDeclaration createDeclaration(Long internshipId, String declarationNumber) {
        logger.info("Creating SGK declaration for internship ID: {} with number: {}", internshipId, declarationNumber);
        
        // Staj kaydını al
        Internship internship = internshipRepository.findById(internshipId)
                .orElseThrow(() -> {
                    logger.error("Internship not found with ID: {}", internshipId);
                    return new ResourceNotFoundException("Internship not found with id: " + internshipId);
                });
        
        // Daha önce SGK bildirimi var mı kontrol et
        if (sgkDeclarationRepository.existsByInternshipId(internshipId)) {
            logger.warn("SGK declaration already exists for internship ID: {}", internshipId);
            throw new IllegalStateException("SGK declaration already exists for this internship");
        }
        
        // SGK bildirimi oluştur
        SGKDeclaration declaration = SGKDeclaration.builder()
                .internship(internship)
                .declarationNumber(declarationNumber)
                .startDate(internship.getStartDate())
                .endDate(internship.getEndDate())
                .isActive(true)
                .notes("SGK bildirimi oluşturuldu")
                .build();
        
        SGKDeclaration savedDeclaration = sgkDeclarationRepository.save(declaration);
        logger.info("SGK declaration created with ID: {} for internship ID: {}", savedDeclaration.getId(), internshipId);
        
        // Öğrenciye bildirim gönder
        notificationService.sendNotification(
            internship.getStudent().getId(),
            "SGK bildiriminiz oluşturuldu",
            NotificationType.SYSTEM_NOTIFICATION
        );
        
        // Danışmana bildirim gönder
        if (internship.getAdvisor() != null) {
            notificationService.sendNotification(
                internship.getAdvisor().getId(),
                "Öğrenci " + internship.getStudent().getFirstName() + " için SGK bildirimi oluşturuldu",
                NotificationType.SYSTEM_NOTIFICATION
            );
        }
        
        return savedDeclaration;
    }

    @Override
    public Optional<SGKDeclaration> getDeclarationByInternshipId(Long internshipId) {
        logger.debug("Getting SGK declaration for internship ID: {}", internshipId);
        return sgkDeclarationRepository.findByInternshipId(internshipId);
    }

    @Override
    public Optional<SGKDeclaration> getDeclarationByNumber(String declarationNumber) {
        logger.debug("Getting SGK declaration by number: {}", declarationNumber);
        return sgkDeclarationRepository.findByDeclarationNumber(declarationNumber);
    }

    @Override
    public SGKDeclaration updateDeclaration(Long declarationId, boolean isActive, String notes) {
        logger.info("Updating SGK declaration ID: {}, active: {}", declarationId, isActive);
        
        SGKDeclaration declaration = sgkDeclarationRepository.findById(declarationId)
                .orElseThrow(() -> {
                    logger.error("SGK declaration not found with ID: {}", declarationId);
                    return new ResourceNotFoundException("SGK declaration not found with id: " + declarationId);
                });
        
        declaration.setIsActive(isActive);
        
        if (notes != null && !notes.isEmpty()) {
            declaration.setNotes(notes);
        }
        
        SGKDeclaration updatedDeclaration = sgkDeclarationRepository.save(declaration);
        logger.info("SGK declaration updated with ID: {}", updatedDeclaration.getId());
        
        // Öğrenciye bildirim gönder
        notificationService.sendNotification(
            updatedDeclaration.getInternship().getStudent().getId(),
            "SGK bildiriminiz güncellendi",
            NotificationType.SYSTEM_NOTIFICATION
        );
        
        return updatedDeclaration;
    }

    @Override
    public void deleteDeclaration(Long internshipId) {
        logger.info("Deleting SGK declaration for internship ID: {}", internshipId);
        
        if (!sgkDeclarationRepository.existsByInternshipId(internshipId)) {
            logger.warn("No SGK declaration found for internship ID: {}", internshipId);
            throw new ResourceNotFoundException("No SGK declaration found for internship with id: " + internshipId);
        }
        
        sgkDeclarationRepository.deleteByInternshipId(internshipId);
        logger.info("SGK declaration deleted for internship ID: {}", internshipId);
    }

    @Override
    public String generateDeclarationDocument(Long internshipId) {
        logger.info("Generating SGK declaration document for internship ID: {}", internshipId);
        
        Internship internship = internshipRepository.findById(internshipId)
                .orElseThrow(() -> {
                    logger.error("Internship not found with ID: {}", internshipId);
                    return new ResourceNotFoundException("Internship not found with id: " + internshipId);
                });
        
        SGKDeclaration declaration = sgkDeclarationRepository.findByInternshipId(internshipId)
                .orElseThrow(() -> {
                    logger.error("SGK declaration not found for internship ID: {}", internshipId);
                    return new ResourceNotFoundException("SGK declaration not found for internship with id: " + internshipId);
                });
        
        // Burada gerçek bir PDF oluşturma işlemi yapılabilir
        // Şimdilik sadece rastgele bir dosya adı döndürelim
        String documentPath = "uploads/sgk_declarations/sgk_" + internshipId + "_" + UUID.randomUUID() + ".pdf";
        logger.info("SGK declaration document generated at path: {}", documentPath);
        
        return documentPath;
    }

    @Override
    public boolean isDeclarationProcessed(Long internshipId) {
        logger.debug("Checking if SGK declaration is processed for internship ID: {}", internshipId);
        
        Optional<SGKDeclaration> declarationOpt = sgkDeclarationRepository.findByInternshipId(internshipId);
        
        if (declarationOpt.isPresent()) {
            SGKDeclaration declaration = declarationOpt.get();
            return declaration.getIsActive();
        }
        
        return false;
    }
} 