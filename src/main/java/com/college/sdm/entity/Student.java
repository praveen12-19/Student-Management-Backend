package com.college.sdm.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "students")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "image_path")
    private String image; // File path

    @Column(nullable = false)
    private String name;

    @Column(name = "register_number", unique = true, nullable = false)
    private String registerNumber;

    @Column(nullable = false)
    private Integer year;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(name = "academic_year", nullable = false)
    private String academicYear;

    private LocalDate dob;

    private String gender;

    @Column(name = "aadhar_number")
    private String aadharNumber;

    @Column(name = "pan_card")
    private String panCard;

    @Column(name = "linkedin_url")
    private String linkedinUrl;

    @Column(name = "laptop_having")
    private Boolean laptopHaving;

    @Column(name = "blood_group")
    private String bloodGroup;

    @Column(name = "languages_known")
    private String languagesKnown;

    private String community;

    @Column(name = "email_id")
    private String emailId;

    @Column(name = "student_number")
    private String studentNumber;

    @Column(name = "current_address", length = 1000)
    private String currentAddress;

    @Column(name = "permanent_address", length = 1000)
    private String permanentAddress;

    @Column(name = "student_type")
    private String studentType; // Hosteller/DayScholar/BlackTag

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id")
    private Mentor mentor;

    @Embedded
    private FatherDetail fatherDetail;

    @Embedded
    private MotherDetail motherDetail;

    // Leave & attendance simple fields
    @Column(name = "total_leaves_taken", columnDefinition = "integer default 0")
    @Builder.Default
    private Integer totalLeavesTaken = 0;

    @Column(name = "od", columnDefinition = "integer default 0")
    @Builder.Default
    private Integer od = 0;

    @Column(name = "late_coming", columnDefinition = "integer default 0")
    @Builder.Default
    private Integer lateComing = 0;

    // Bidirectional child relationships
    @OneToOne(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private TenthDetail tenthDetail;

    @OneToOne(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private TwelfthDetail twelfthDetail;

    @OneToOne(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private AdmissionDetail admissionDetail;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private java.util.List<Sibling> siblings = new java.util.ArrayList<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private java.util.List<Certificate> certificates = new java.util.ArrayList<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private java.util.List<SemesterRecord> semesterRecords = new java.util.ArrayList<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private java.util.List<ExtraActivity> extraActivities = new java.util.ArrayList<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private java.util.List<Internship> internships = new java.util.ArrayList<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private java.util.List<IndustrialVisit> industrialVisits = new java.util.ArrayList<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private java.util.List<IndisciplinaryActivity> indisciplinaryActivities = new java.util.ArrayList<>();
}
