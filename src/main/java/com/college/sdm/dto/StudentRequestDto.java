package com.college.sdm.dto;

import com.college.sdm.entity.FatherDetail;
import com.college.sdm.entity.MotherDetail;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentRequestDto {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Register number is required")
    private String registerNumber;

    @NotNull(message = "Year is required")
    private Integer year;

    @NotNull(message = "Department ID is required")
    private Long departmentId;

    @NotBlank(message = "Academic year is required")
    private String academicYear;

    private LocalDate dob;
    private String gender;
    private String aadharNumber;
    private String panCard;
    private String linkedinUrl;
    private Boolean laptopHaving;
    private String bloodGroup;
    private String languagesKnown;
    private String community;

    @Email(message = "Invalid email format")
    private String emailId;

    private String studentNumber;
    private String currentAddress;
    private String permanentAddress;
    private String studentType; // Hosteller/DayScholar/BlackTag
    private Long mentorId; // optional/null

    private FatherDetail fatherDetail;
    private MotherDetail motherDetail;

    private String image;

    private Integer totalLeavesTaken;
    private Integer od;
    private Integer lateComing;
    private String leaveDetailsJson;
}
