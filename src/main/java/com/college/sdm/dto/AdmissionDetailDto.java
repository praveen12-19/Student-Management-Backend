package com.college.sdm.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdmissionDetailDto {
    private Long id;
    private Long studentId;
    private String scholarships;
}
