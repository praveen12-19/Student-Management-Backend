package com.college.sdm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InternshipDto {
    private Long id;
    private Long studentId;

    @NotBlank(message = "Company/Internship name is required")
    private String name;

    private String date;
    private String location;
    private String domain;
    private String certificatePath;
}
