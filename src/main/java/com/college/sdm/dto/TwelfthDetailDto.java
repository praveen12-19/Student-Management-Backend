package com.college.sdm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TwelfthDetailDto {
    private Long id;
    private Long studentId;

    private String schoolName;

    private String medium;

    private String group;

    private Double totalMarks;

    private Double percentage;

    private Double cutoff;

    private Double physicsMarks;

    private Double chemistryMarks;

    private Double mathsMarks;

    private Integer yearOfPassing;
}
