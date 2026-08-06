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

    @NotBlank(message = "School name is required")
    private String schoolName;

    @NotBlank(message = "Medium of instruction is required")
    private String medium;

    @NotBlank(message = "Group is required")
    private String group;

    @NotNull(message = "Total marks is required")
    private Double totalMarks;

    @NotNull(message = "Percentage is required")
    private Double percentage;

    @NotNull(message = "Cutoff is required")
    private Double cutoff;

    @NotNull(message = "Physics marks is required")
    private Double physicsMarks;

    @NotNull(message = "Chemistry marks is required")
    private Double chemistryMarks;

    @NotNull(message = "Maths marks is required")
    private Double mathsMarks;

    @NotNull(message = "Year of passing is required")
    private Integer yearOfPassing;
}
