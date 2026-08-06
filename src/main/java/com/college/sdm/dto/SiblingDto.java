package com.college.sdm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SiblingDto {
    private Long id;
    private Long studentId;

    @NotBlank(message = "Sibling name is required")
    private String name;

    private String occupation;
    private String qualification;
    private String emailId;
    private String mobileNumber;
}
