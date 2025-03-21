package com.internship.repository;

import com.internship.entity.Internship;
import com.internship.entity.InternshipStatus;
import com.internship.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InternshipRepository extends JpaRepository<Internship, Long> {
    List<Internship> findByStudent(User student);
    List<Internship> findByFacultyAdvisor(User advisor);
    List<Internship> findByStatus(InternshipStatus status);
    List<Internship> findByStudentAndStatus(User student, InternshipStatus status);
} 