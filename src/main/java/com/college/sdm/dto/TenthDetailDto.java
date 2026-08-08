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

    private String schoolName;

    private String medium;

    private Double totalMarks;

    private Double percentage;

    private Integer yearOfPassing;
}
