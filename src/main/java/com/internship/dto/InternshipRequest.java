package com.internship.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;

@Data
public class InternshipRequest {
    @NotBlank(message = "Şirket adı boş bırakılamaz")
    private String companyName;

    @NotBlank(message = "Şirket adresi boş bırakılamaz")
    private String companyAddress;

    @NotBlank(message = "Şirket telefonu boş bırakılamaz")
    private String companyPhone;

    @NotNull(message = "Başlangıç tarihi boş bırakılamaz")
    @FutureOrPresent(message = "Başlangıç tarihi bugün veya gelecekte olmalıdır")
    private LocalDate startDate;

    @NotNull(message = "Bitiş tarihi boş bırakılamaz")
    @FutureOrPresent(message = "Bitiş tarihi bugün veya gelecekte olmalıdır")
    private LocalDate endDate;

    @NotNull(message = "Çalışma günü sayısı boş bırakılamaz")
    @Positive(message = "Çalışma günü sayısı pozitif olmalıdır")
    private Integer workDays;

    @NotNull(message = "Sigorta desteği belirtilmelidir")
    private Boolean insuranceSupport;

    private String description;
} 