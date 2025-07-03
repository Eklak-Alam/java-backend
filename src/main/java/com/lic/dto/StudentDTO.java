package com.lic.dto;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class StudentDTO {

    @NotNull(message = "Serial number is required")
    private String srNo;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "PAN number is required")
    @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$", message = "Invalid PAN number format")
    private String panNumber;

    private String licRegdNumber;

    @NotBlank(message = "Branch is required")
    private String branch;

    @NotBlank(message = "Start date is required")
    private String startDate;

    @NotBlank(message = "End date is required")
    private String endDate;
}
