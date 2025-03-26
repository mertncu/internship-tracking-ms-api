package com.internship.service;

import com.internship.entity.InternshipStatus;

public interface NotificationService {
    void notifyAdvisorNewApplication(Long internshipId, Long advisorId);


    void notifyStudentStatusChange(Long internshipId, Long studentId, InternshipStatus newStatus);

    void notifyDepartmentCoordinator(Long internshipId, Long coordinatorId);

    void notifyUniversityCoordinator(Long internshipId, Long coordinatorId);

    void notifyStudentSGKDeclaration(Long internshipId, Long studentId, String declarationNumber);

    void notifyRevisionRequest(Long internshipId, Long studentId, String comments);

    void sendBulkNotification(String role, String message);
} 