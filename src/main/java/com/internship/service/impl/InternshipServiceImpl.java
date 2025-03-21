package com.internship.service.impl;

import com.internship.dto.InternshipRequest;
import com.internship.entity.Internship;
import com.internship.entity.InternshipStatus;
import com.internship.entity.User;
import com.internship.repository.InternshipRepository;
import com.internship.repository.UserRepository;
import com.internship.service.InternshipService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class InternshipServiceImpl implements InternshipService {

    private final InternshipRepository internshipRepository;
    private final UserRepository userRepository;

    @Override
    public Internship createInternship(InternshipRequest request, User student) {
        Internship internship = Internship.builder()
                .student(student)
                .companyName(request.getCompanyName())
                .companyAddress(request.getCompanyAddress())
                .companyPhone(request.getCompanyPhone())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .workDays(request.getWorkDays())
                .insuranceSupport(request.getInsuranceSupport())
                .description(request.getDescription())
                .status(InternshipStatus.PENDING_ADVISOR_APPROVAL)
                .build();

        return internshipRepository.save(internship);
    }

    @Override
    public Internship updateInternshipStatus(Long internshipId, InternshipStatus status, String rejectionReason) {
        Internship internship = getInternshipById(internshipId);
        internship.setStatus(status);
        
        if (status == InternshipStatus.REJECTED && rejectionReason != null) {
            internship.setRejectionReason(rejectionReason);
        }

        return internshipRepository.save(internship);
    }

    @Override
    public Internship assignAdvisor(Long internshipId, User advisor) {
        Internship internship = getInternshipById(internshipId);
        internship.setFacultyAdvisor(advisor);
        return internshipRepository.save(internship);
    }

    @Override
    public List<Internship> getStudentInternships(User student) {
        return internshipRepository.findByStudent(student);
    }

    @Override
    public List<Internship> getAdvisorInternships(User advisor) {
        return internshipRepository.findByFacultyAdvisor(advisor);
    }

    @Override
    public List<Internship> getInternshipsByStatus(InternshipStatus status) {
        return internshipRepository.findByStatus(status);
    }

    @Override
    public Internship getInternshipById(Long id) {
        return internshipRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Staj bulunamadı"));
    }

    @Override
    public void uploadDocument(Long internshipId, String documentPath) {
        Internship internship = getInternshipById(internshipId);
        internship.setDocumentPath(documentPath);
        internship.setStatus(InternshipStatus.DOCUMENT_WAITING);
        internshipRepository.save(internship);
    }

    @Override
    public void deleteInternship(Long id) {
        if (!internshipRepository.existsById(id)) {
            throw new EntityNotFoundException("Staj bulunamadı: " + id);
        }
        internshipRepository.deleteById(id);
    }

    @Override
    public Internship assignAdvisorById(Long internshipId, Long advisorId) {
        Internship internship = getInternshipById(internshipId);
        User advisor = userRepository.findById(advisorId)
                .orElseThrow(() -> new EntityNotFoundException("Danışman bulunamadı: " + advisorId));

        // Danışmanın ROLE_FACULTY_ADVISOR rolüne sahip olduğunu kontrol et
        boolean isFacultyAdvisor = advisor.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_FACULTY_ADVISOR"));
        
        if (!isFacultyAdvisor) {
            throw new IllegalArgumentException("Seçilen kullanıcı danışman rolüne sahip değil");
        }

        internship.setFacultyAdvisor(advisor);
        internship.setStatus(InternshipStatus.PENDING_ADVISOR_APPROVAL);
        
        return internshipRepository.save(internship);
    }
} 