package com.college.sdm.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificateDto {
    private Long id;
    private Long studentId;
    private String type; // SSLC / HSC / Community / Income / TC / FG / Nativity
    private String filePath;
}
