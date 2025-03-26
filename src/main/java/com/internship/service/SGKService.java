package com.internship.service;

import com.internship.entity.SGKDeclaration;

import java.util.Optional;

public interface SGKService {
    SGKDeclaration createDeclaration(Long internshipId, String declarationNumber);

    Optional<SGKDeclaration> getDeclarationByInternshipId(Long internshipId);

    Optional<SGKDeclaration> getDeclarationByNumber(String declarationNumber);

    SGKDeclaration updateDeclaration(Long declarationId, boolean isProcessed, String processNotes);

    void deleteDeclaration(Long internshipId);

    String generateDeclarationDocument(Long internshipId);

    boolean isDeclarationProcessed(Long internshipId);
} 