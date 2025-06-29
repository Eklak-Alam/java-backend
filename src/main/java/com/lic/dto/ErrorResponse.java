package com.lic.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    private String message;
    private LocalDateTime timestamp;
    private int status;
    private Map<String, String> validationErrors;

    public ErrorResponse(String message, LocalDateTime timestamp, int status) {
        this.message = message;
        this.timestamp = timestamp;
        this.status = status;
    }
}
