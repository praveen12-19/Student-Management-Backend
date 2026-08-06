package com.college.sdm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenthDetailDto {
    private Long id;
    private Long studentId;

    @NotBlank(message = "School name is required")
    private String schoolName;

    @NotBlank(message = "Medium of instruction is required")
    private String medium;

    @NotNull(message = "Total marks is required")
    private Double totalMarks;

    @NotNull(message = "Percentage is required")
    private Double percentage;

    @NotNull(message = "Year of passing is required")
    private Integer yearOfPassing;
}
