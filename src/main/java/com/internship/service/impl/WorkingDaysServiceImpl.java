package com.internship.service.impl;

import com.internship.entity.ApplicationWorkingDays;
import com.internship.entity.Internship;
import com.internship.repository.ApplicationWorkingDaysRepository;
import com.internship.repository.InternshipRepository;
import com.internship.service.WorkingDaysService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkingDaysServiceImpl implements WorkingDaysService {

    private final ApplicationWorkingDaysRepository workingDaysRepository;
    private final InternshipRepository internshipRepository;

    @Override
    public List<ApplicationWorkingDays> saveWorkingDays(Long internshipId, List<ApplicationWorkingDays> workingDays) {
        Internship internship = internshipRepository.findById(internshipId)
                .orElseThrow(() -> new EntityNotFoundException("Staj bulunamadı"));

        workingDays.forEach(day -> day.setInternship(internship));
        return workingDaysRepository.saveAll(workingDays);
    }

    @Override
    public List<ApplicationWorkingDays> getWorkingDaysByInternshipId(Long internshipId) {
        return workingDaysRepository.findByInternshipId(internshipId);
    }

    @Override
    public List<ApplicationWorkingDays> getWorkingDaysBetweenDates(Long internshipId, LocalDate startDate, LocalDate endDate) {
        // Tüm günleri getir ve servis katmanında filtrele
        // Not: Bu metot, staj günleri ile tarih aralığını kıyaslayacak şekilde değiştirilmeli
        // Şimdilik tüm günleri dönüyoruz, gerçek filtreleme uygulamanın ihtiyaçlarına göre yapılmalı
        return workingDaysRepository.findByInternshipId(internshipId);
    }

    @Override
    public int calculateTotalWorkingDays(Long internshipId) {
        return workingDaysRepository.countWorkingDaysByInternshipId(internshipId);
    }

    @Override
    @Transactional
    public List<ApplicationWorkingDays> updateWorkingDays(Long internshipId, List<ApplicationWorkingDays> workingDays) {
        // Önce mevcut günleri sil
        workingDaysRepository.deleteByInternshipId(internshipId);
        
        // Yeni günleri kaydet
        return saveWorkingDays(internshipId, workingDays);
    }

    @Override
    @Transactional
    public void deleteWorkingDays(Long internshipId) {
        workingDaysRepository.deleteByInternshipId(internshipId);
    }
} 