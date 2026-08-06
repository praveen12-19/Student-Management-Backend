package com.college.sdm.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SemesterRecordDto {
    private Long id;

    @NotNull(message = "Semester number is required")
    @Min(value = 1)
    @Max(value = 8)
    private Integer semesterNumber;

    @NotNull(message = "Year number is required")
    @Min(value = 1)
    @Max(value = 4)
    private Integer yearNumber;

    private String mentorName;
    private Long studentId;
    private Double firstHourTestMarks;
    private Double cat1;
    private Double cat2;
    private Double model;
    private String grade;
    private Double gpa;
    private Double cgpaTillNow;
    private Integer historyOfArrears;
    private Integer standingArrears;
}
