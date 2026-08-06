package com.college.sdm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IndisciplinaryActivityDto {
    private Long id;
    private Long studentId;

    @NotBlank(message = "Description is required")
    private String description;

    private String date;
    private String addedBy; // username + role
}
