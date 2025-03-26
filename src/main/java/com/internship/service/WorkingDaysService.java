package com.internship.service;

import com.internship.entity.ApplicationWorkingDays;

import java.time.LocalDate;
import java.util.List;

public interface WorkingDaysService {
    List<ApplicationWorkingDays> saveWorkingDays(Long internshipId, List<ApplicationWorkingDays> workingDays);

    List<ApplicationWorkingDays> getWorkingDaysByInternshipId(Long internshipId);

    List<ApplicationWorkingDays> getWorkingDaysBetweenDates(Long internshipId, LocalDate startDate, LocalDate endDate);

    int calculateTotalWorkingDays(Long internshipId);

    List<ApplicationWorkingDays> updateWorkingDays(Long internshipId, List<ApplicationWorkingDays> workingDays);

    void deleteWorkingDays(Long internshipId);
} 