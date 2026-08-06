package com.college.sdm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentorRequestDto {

    @NotBlank(message = "Username is required")
    private String username;

    private String password;

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Department ID is required")
    private Long departmentId;

    private Integer assignedYear;

    private String assignedSection; // Class/Section
}
