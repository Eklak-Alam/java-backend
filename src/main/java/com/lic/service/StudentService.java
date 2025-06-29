package com.lic.service;

import com.lic.dto.StudentDTO;
import com.lic.entities.Student;
import com.lic.exception.DuplicatePanException;
import com.lic.exception.ResourceNotFoundException;
import com.lic.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;

    @Transactional
    public Student createStudent(StudentDTO studentDTO) {
        String panNumber = studentDTO.getPanNumber().toUpperCase();

        if (studentRepository.existsByPanNumber(panNumber)) {
            throw new DuplicatePanException("Student with PAN " + panNumber + " already exists");
        }

        Student student = new Student();
        student.setSrNo(studentDTO.getSrNo());
        student.setName(studentDTO.getName());
        student.setPanNumber(panNumber);
        student.setLicRegdNumber(studentDTO.getLicRegdNumber());
        student.setBranch(studentDTO.getBranch());
        student.setStartDate(studentDTO.getStartDate());
        student.setEndDate(studentDTO.getEndDate());

        return studentRepository.save(student);
    }

    @Transactional
    public List<Student> createStudents(List<StudentDTO> studentDTOs) {
        List<Student> students = studentDTOs.stream()
                .filter(dto -> !studentRepository.existsByPanNumber(dto.getPanNumber().toUpperCase()))
                .map(dto -> {
                    Student student = new Student();
                    student.setSrNo(dto.getSrNo());
                    student.setName(dto.getName());
                    student.setPanNumber(dto.getPanNumber().toUpperCase());
                    student.setLicRegdNumber(dto.getLicRegdNumber());
                    student.setBranch(dto.getBranch());
                    student.setStartDate(dto.getStartDate());
                    student.setEndDate(dto.getEndDate());
                    return student;
                })
                .collect(Collectors.toList());

        return studentRepository.saveAll(students);
    }

    public Student getStudentByPan(String panNumber) {
        return studentRepository.findByPanNumber(panNumber.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with PAN: " + panNumber));
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public List<StudentDTO> getAllStudentDTOs() {
        return studentRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public StudentDTO convertToDTO(Student student) {
        StudentDTO dto = new StudentDTO();
        dto.setSrNo(student.getSrNo());
        dto.setName(student.getName());
        dto.setPanNumber(student.getPanNumber());
        dto.setLicRegdNumber(student.getLicRegdNumber());
        dto.setBranch(student.getBranch());
        dto.setStartDate(student.getStartDate());
        dto.setEndDate(student.getEndDate());
        return dto;
    }


    // In StudentService.java

    @Transactional
    public Student updateStudent(String panNumber, StudentDTO studentDTO) {
        Student existingStudent = getStudentByPan(panNumber);

        // Update all fields except PAN (which is our identifier)
        existingStudent.setSrNo(studentDTO.getSrNo());
        existingStudent.setName(studentDTO.getName());
        existingStudent.setLicRegdNumber(studentDTO.getLicRegdNumber());
        existingStudent.setBranch(studentDTO.getBranch());
        existingStudent.setStartDate(studentDTO.getStartDate());
        existingStudent.setEndDate(studentDTO.getEndDate());

        return studentRepository.save(existingStudent);
    }

    @Transactional
    public void deleteStudent(String panNumber) {
        Student student = getStudentByPan(panNumber);
        studentRepository.delete(student);
    }
}