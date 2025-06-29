package com.lic.repository;

import com.lic.entities.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
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

    // Find students by LIC registration number
    List<Student> findByLicRegdNumber(String licRegdNumber);

    // Find students by branch name
    List<Student> findByBranch(String branch);

    // Find students by serial number range
    List<Student> findBySrNoBetween(Integer start, Integer end);

    // Custom query to find students created after a certain date
    @Query("SELECT s FROM Student s WHERE s.createdAt >= :date")
    List<Student> findStudentsCreatedAfter(Date date);

    // Count students by branch
    @Query("SELECT COUNT(s) FROM Student s WHERE s.branch = :branch")
    Long countByBranch(String branch);
}