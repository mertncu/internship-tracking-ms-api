package com.internship.repository;

import com.internship.entity.ApplicationWorkingDays;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ApplicationWorkingDaysRepository extends JpaRepository<ApplicationWorkingDays, Long> {
    
    List<ApplicationWorkingDays> findByInternshipId(Long internshipId);
    
    // Tarihe göre filtreleme kaldırıldı, bu işlev servis katmanında yapılacak
    
    @Query("SELECT COUNT(w) FROM ApplicationWorkingDays w WHERE w.internship.id = ?1")
    int countWorkingDaysByInternshipId(Long internshipId);
    
    void deleteByInternshipId(Long internshipId);
} 