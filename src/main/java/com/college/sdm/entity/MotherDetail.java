package com.college.sdm.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MotherDetail {

    @Column(name = "mother_name")
    private String name;

    @Column(name = "mother_occupation")
    private String occupation;

    @Column(name = "mother_qualification")
    private String qualification;

    @Column(name = "mother_annual_income")
    private Double annualIncome;

    @Column(name = "mother_email_id")
    private String emailId;

    @Column(name = "mother_mobile_number")
    private String mobileNumber;
}
