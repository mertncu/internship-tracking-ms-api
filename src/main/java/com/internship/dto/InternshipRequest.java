package com.internship.dto;

import com.internship.entity.InternshipType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class InternshipRequest {
    @NotBlank(message = "Şirket adı zorunludur")
    private String companyName;
    
    @NotBlank(message = "Şirket adresi zorunludur")
    private String companyAddress;
    
    @NotBlank(message = "Şirket telefon numarası zorunludur")
    private String companyPhone;
    
    @NotNull(message = "Başlangıç tarihi zorunludur")
    private LocalDate startDate;
    
    @NotNull(message = "Bitiş tarihi zorunludur")
    private LocalDate endDate;
    
    @NotNull(message = "Çalışma günleri zorunludur")
    private Integer workDays;
    
    private String description;
    
    @NotNull(message = "Staj tipi zorunludur")
    private InternshipType type;
    
    private Boolean isPaid;
    
    private Boolean insuranceSupport;
    
    private Boolean parentalInsuranceCoverage;
    
    private String companyIBAN;

    private String bankName;
    
    private String bankBranch;
} 