package com.lic.controller;

import com.lic.dto.StudentDTO;
import com.lic.entities.Student;
import com.lic.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "https://balajitraining.in"}, allowCredentials = "true")
public class StudentController {

    private final StudentService studentService;

    @PostMapping("/details")
    public ResponseEntity<StudentDTO> getStudentByPan(@RequestBody PanRequest request) {
        Student student = studentService.getStudentByPan(request.getPanNumber());
        return ResponseEntity.ok(studentService.convertToDTO(student));
    }

    public static class PanRequest {

        private String panNumber;

        // Getters and setters
        public String getPanNumber() {
            return panNumber;
        }

        public void setPanNumber(String panNumber) {
            this.panNumber = panNumber;
        }
    }
}
