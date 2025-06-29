package com.lic.controller;

import com.lic.config.AdminConfig;
import com.lic.dto.StudentDTO;
import com.lic.dto.UploadResponse;
import com.lic.entities.Student;
import com.lic.entities.User;
import com.lic.security.JwtTokenProvider;
import com.lic.service.ExcelService;
import com.lic.service.StudentService;
import com.lic.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "https://balajitraining.in"}, allowCredentials = "true")
public class AdminController {

    private final AdminConfig adminConfig;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final ExcelService excelService;
    private final StudentService studentService;
    private final UserService userService;

    @PostMapping(value = "/students/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponse> uploadStudents(@RequestParam("file") MultipartFile file) {
        try {
            List<StudentDTO> studentDTOs = excelService.processFile(file);
            List<Student> savedStudents = studentService.createStudents(studentDTOs);

            List<StudentDTO> responseDTOs = savedStudents.stream()
                    .map(studentService::convertToDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(
                    new UploadResponse(
                            "Students uploaded successfully",
                            savedStudents.size(),
                            responseDTOs
                    )
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new UploadResponse(e.getMessage(), 0, null));
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(new UploadResponse("File processing error: " + e.getMessage(), 0, null));
        }
    }

    @PostMapping("/students")
    public ResponseEntity<StudentDTO> createStudent(@Valid @RequestBody StudentDTO studentDTO) {
        Student savedStudent = studentService.createStudent(studentDTO);
        return ResponseEntity.ok(studentService.convertToDTO(savedStudent));
    }

    // In AdminController.java
    @PutMapping("/students/{pan}")
    public ResponseEntity<StudentDTO> updateStudent(
            @PathVariable String pan,
            @Valid @RequestBody StudentDTO studentDTO) {
        Student updatedStudent = studentService.updateStudent(pan, studentDTO);
        return ResponseEntity.ok(studentService.convertToDTO(updatedStudent));
    }

    @DeleteMapping("/students/{pan}")
    public ResponseEntity<Void> deleteStudent(@PathVariable String pan) {
        studentService.deleteStudent(pan);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/students")
    public ResponseEntity<List<StudentDTO>> getAllStudents() {
        return ResponseEntity.ok(studentService.getAllStudentDTOs());
    }

    @GetMapping("/students/{pan}")
    public ResponseEntity<StudentDTO> getStudentByPan(@PathVariable String pan) {
        Student student = studentService.getStudentByPan(pan);
        return ResponseEntity.ok(studentService.convertToDTO(student));
    }

    @PostMapping("/promote/{username}")
    public ResponseEntity<User> promoteToAdmin(@PathVariable String username) {
        User promotedUser = userService.promoteToAdmin(username);
        return ResponseEntity.ok(promotedUser);
    }

    // DTO Records
    record AdminLoginRequest(String username, String password) {

    }

    record JwtAuthenticationResponse(String token) {

    }
}
