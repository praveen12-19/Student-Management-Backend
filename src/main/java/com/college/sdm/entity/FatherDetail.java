package com.college.sdm.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FatherDetail {

    @Column(name = "father_name")
    private String name;

    @Column(name = "father_occupation")
    private String occupation;

    @Column(name = "father_qualification")
    private String qualification;

    @Column(name = "father_annual_income")
    private Double annualIncome;

    @Column(name = "father_email_id")
    private String emailId;

    @Column(name = "father_mobile_number")
    private String mobileNumber;
}
