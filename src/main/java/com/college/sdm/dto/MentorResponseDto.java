package com.college.sdm.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentorResponseDto {
    private Long id;
    private String username;
    private String name;
    private DepartmentDto department;
    private Integer assignedYear;
    private String assignedSection;
}
