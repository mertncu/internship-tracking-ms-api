package com.internship.service;

import com.internship.dto.InternshipRequest;
import com.internship.entity.Internship;
import com.internship.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface InternshipService {
    Internship createInternship(InternshipRequest request);
    Internship getInternshipById(Long id);
    List<Internship> getAllInternships();
    List<Internship> getInternshipsByStudent(User student);
    List<Internship> getInternshipsByAdvisor(User advisor);
    Internship assignAdvisor(Long internshipId, Long advisorId);
    String uploadDocument(Long internshipId, MultipartFile file, String documentType);
    Internship updateInternship(Internship internship);
    void deleteInternship(Long id);
} 