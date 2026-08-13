package com.college.sdm.dto;

import com.college.sdm.entity.FatherDetail;
import com.college.sdm.entity.MotherDetail;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentResponseDto {
    private Long id;
    private String image;
    private String name;
    private String registerNumber;
    private Integer year;
    private String section;
    private DepartmentDto department;
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
    private String emailId;
    private String studentNumber;
    private String currentAddress;
    private String permanentAddress;
    private String studentType; // Hosteller/DayScholar/BlackTag
    private MentorResponseDto mentor;
    private FatherDetail fatherDetail;
    private MotherDetail motherDetail;

    private Integer totalLeavesTaken;
    private Integer od;
    private Integer lateComing;
    private String leaveDetailsJson;

    private TenthDetailDto tenthDetail;
    private TwelfthDetailDto twelfthDetail;
    private AdmissionDetailDto admissionDetail;
    private List<SiblingDto> siblings;
    private List<CertificateDto> certificates;
    private List<SemesterRecordDto> semesterRecords;
    private List<ExtraActivityDto> extraActivities;
    private List<InternshipDto> internships;
    private List<IndustrialVisitDto> industrialVisits;
    private List<IndisciplinaryActivityDto> indisciplinaryActivities;
}
