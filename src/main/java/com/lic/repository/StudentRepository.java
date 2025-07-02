package com.lic.repository;

import com.lic.dto.StudentDTO;
import com.lic.entities.Student;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    // Find student by PAN number (exact match)
    Optional<Student> findByPanNumber(String panNumber);

    // Check if PAN number exists (case-sensitive)
    boolean existsByPanNumber(String panNumber);

    // Get all PAN numbers (for bulk duplicate checking)
    @Query("SELECT s.panNumber FROM Student s")
    List<String> findAllPanNumbers();

    @Modifying
    @Query("UPDATE Student s SET s.lastUpload = FALSE WHERE s.lastUpload = TRUE")
    @Transactional
        // Add this annotation
    void resetLastUploadFlag();

    @Query("SELECT s FROM Student s WHERE s.lastUpload = TRUE")
    List<Student> findLastUploadedStudents();

}