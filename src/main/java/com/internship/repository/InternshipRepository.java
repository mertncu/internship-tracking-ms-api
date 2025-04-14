package com.internship.repository;

import com.internship.entity.Internship;
import com.internship.entity.InternshipStatus;
import com.internship.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InternshipRepository extends JpaRepository<Internship, Long> {
    List<Internship> findByStudent(User student);
    List<Internship> findByAdvisor(User advisor);
    List<Internship> findByStatus(InternshipStatus status);
    
    // İlişkili entity'leri tek seferde çekmek için optimize edilmiş sorgular
    
    @Query("SELECT i FROM Internship i LEFT JOIN FETCH i.documents WHERE i.id = :id")
    Optional<Internship> findByIdWithDocuments(@Param("id") Long id);
    
    @Query("SELECT i FROM Internship i LEFT JOIN FETCH i.approvals WHERE i.id = :id")
    Optional<Internship> findByIdWithApprovals(@Param("id") Long id);
    
    @Query("SELECT DISTINCT i FROM Internship i LEFT JOIN FETCH i.documents WHERE i.student = :student")
    List<Internship> findByStudentWithDocuments(@Param("student") User student);
    
    @Query("SELECT DISTINCT i FROM Internship i LEFT JOIN FETCH i.approvals WHERE i.student = :student")
    List<Internship> findByStudentWithApprovals(@Param("student") User student);
    
    @Query("SELECT DISTINCT i FROM Internship i LEFT JOIN FETCH i.documents WHERE i.advisor = :advisor")
    List<Internship> findByAdvisorWithDocuments(@Param("advisor") User advisor);
    
    @Query("SELECT DISTINCT i FROM Internship i LEFT JOIN FETCH i.approvals WHERE i.advisor = :advisor")
    List<Internship> findByAdvisorWithApprovals(@Param("advisor") User advisor);
    
    // Çoklu koleksiyon kullanımında MultipleBagFetchException hatası almamak için
    // her koleksiyon için ayrı ayrı sorgular kullanıyoruz - artık Set tipinde olduğu için sorun yok
    @Query("SELECT DISTINCT i FROM Internship i LEFT JOIN FETCH i.student LEFT JOIN FETCH i.advisor")
    List<Internship> findAllWithBasicDetails();
    
    @Query("SELECT DISTINCT i FROM Internship i LEFT JOIN FETCH i.documents")
    List<Internship> findAllWithDocuments();
    
    @Query("SELECT DISTINCT i FROM Internship i LEFT JOIN FETCH i.approvals")
    List<Internship> findAllWithApprovals();
    
    // Koleksiyonlar artık Set olduğu için birden fazla koleksiyonu birlikte fetch edebiliriz
    @Query("SELECT DISTINCT i FROM Internship i LEFT JOIN FETCH i.student LEFT JOIN FETCH i.advisor " +
           "LEFT JOIN FETCH i.documents LEFT JOIN FETCH i.approvals")
    List<Internship> findAllWithDetails();
    
    /**
     * Set olarak tanımlanan koleksiyonları tek seferde çekebiliriz
     */
    @Query("SELECT DISTINCT i FROM Internship i LEFT JOIN FETCH i.documents LEFT JOIN FETCH i.approvals WHERE i.id = :id")
    Optional<Internship> findByIdWithDocumentsAndApprovals(@Param("id") Long id);
    
    // Öğrenci için tüm ilişkili verileri çeken metot
    @Query("SELECT DISTINCT i FROM Internship i LEFT JOIN FETCH i.documents LEFT JOIN FETCH i.approvals " +
           "WHERE i.student = :student")
    List<Internship> findByStudentWithAllDetails(@Param("student") User student);
    
    // Danışman için tüm ilişkili verileri çeken metot
    @Query("SELECT DISTINCT i FROM Internship i LEFT JOIN FETCH i.documents LEFT JOIN FETCH i.approvals " +
           "WHERE i.advisor = :advisor")
    List<Internship> findByAdvisorWithAllDetails(@Param("advisor") User advisor);
} 