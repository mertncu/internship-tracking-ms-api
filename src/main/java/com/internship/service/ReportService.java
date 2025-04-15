package com.internship.service;

import com.internship.entity.InternshipReport;
import com.internship.entity.ReportStatus;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface ReportService {
    InternshipReport uploadReport(Long internshipId, String title, MultipartFile file, String description);
    
    List<InternshipReport> getReportsByInternshipId(Long internshipId);
    
    List<InternshipReport> getReportsByInternshipIdAndStatus(Long internshipId, ReportStatus status);
    
    Optional<InternshipReport> getLatestReportByInternshipId(Long internshipId);
    
    Optional<InternshipReport> getReportById(Long reportId);
    
    InternshipReport updateReportStatus(Long reportId, ReportStatus status, String feedback, Integer grade);
    
    void deleteReport(Long reportId);
    
    String downloadReportFile(Long reportId);
} 