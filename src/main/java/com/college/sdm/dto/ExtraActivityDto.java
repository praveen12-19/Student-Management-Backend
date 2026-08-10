package com.college.sdm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExtraActivityDto {
    private Long id;
    private Long studentId;

    @NotBlank(message = "Activity type is required")
    private String type; // NPTEL/OnlineCourse/IELTS/German/Japanese/Symposium/Conference

    @NotBlank(message = "Activity name is required")
    private String name;

    private String details;
    private String certificatePath;
}
