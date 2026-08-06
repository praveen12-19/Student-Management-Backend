package com.college.sdm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IndustrialVisitDto {
    private Long id;
    private Long studentId;

    @NotBlank(message = "Visit location or company name is required")
    private String name;

    private String date;
    private String location;
}
