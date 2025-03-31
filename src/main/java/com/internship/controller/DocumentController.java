package com.internship.controller;

import com.internship.entity.Document;
import com.internship.repository.DocumentRepository;
import com.internship.security.UserSecurity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Tag(name = "Documents")
public class DocumentController {

    private final DocumentRepository documentRepository;
    private final UserSecurity userSecurity;

    @Operation(summary = "Staj belgelerini listele")
    @GetMapping("/internship/{internshipId}")
    public ResponseEntity<List<Document>> getDocumentsByInternship(@PathVariable Long internshipId) {
        if (!userSecurity.canAccessInternship(internshipId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Document> documents = documentRepository.findByInternshipId(internshipId);
        return ResponseEntity.ok(documents);
    }

    @Operation(summary = "Belge indir")
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Belge bulunamadı"));

//        if (!userSecurity.canAccessInternship(document.getInternship().getId())) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
//        }

        try {
            Path filePath = Paths.get(document.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                throw new RuntimeException("Dosya bulunamadı: " + document.getFilePath());
            }

            String contentType = determineContentType(document.getFileName());
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getFileName() + "\"")
                    .body(resource);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Hatalı URL: " + e.getMessage());
        }
    }

    @Operation(summary = "Belge sil")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteDocument(@PathVariable Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Belge bulunamadı"));
//
//        if (!userSecurity.canAccessInternship(document.getInternship().getId())) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
//        }

        try {
            // Dosyayı fiziksel olarak silmeye çalış
            Path filePath = Paths.get(document.getFilePath());
            java.nio.file.Files.deleteIfExists(filePath);
            
            // Veritabanından belgeyi sil
            documentRepository.delete(document);
            
            return ResponseEntity.ok(Map.of("message", "Belge başarıyla silindi"));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Dosya silinemedi: " + e.getMessage()));
        }
    }
    
    private String determineContentType(String fileName) {
        String lowercaseName = fileName.toLowerCase();
        if (lowercaseName.endsWith(".pdf")) {
            return "application/pdf";
        } else if (lowercaseName.endsWith(".doc")) {
            return "application/msword";
        } else if (lowercaseName.endsWith(".docx")) {
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        } else if (lowercaseName.endsWith(".jpg") || lowercaseName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowercaseName.endsWith(".png")) {
            return "image/png";
        } else {
            return "application/octet-stream";
        }
    }
} 