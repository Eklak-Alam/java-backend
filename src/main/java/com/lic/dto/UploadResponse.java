package com.lic.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadResponse {

    private String message;
    private int count;
    private List<StudentDTO> students;
}
