package com.internship.service;

import com.internship.dto.InternshipRequest;
import com.internship.entity.Internship;
import com.internship.entity.InternshipStatus;
import com.internship.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface InternshipService {
    Internship createInternship(InternshipRequest request, User student);
    Internship updateInternshipStatus(Long internshipId, InternshipStatus status, String rejectionReason);
    Internship assignAdvisor(Long internshipId, User advisor);
    Internship assignAdvisorById(Long internshipId, Long advisorId);
    List<Internship> getStudentInternships(User student);
    List<Internship> getAdvisorInternships(User advisor);
    List<Internship> getInternshipsByStatus(InternshipStatus status);
    Internship getInternshipById(Long id);
    void uploadDocument(Long internshipId, String documentPath);
    void deleteInternship(Long id);
} 