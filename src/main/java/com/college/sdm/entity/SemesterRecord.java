package com.college.sdm.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "semester_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SemesterRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "semester_number", nullable = false)
    private Integer semesterNumber;

    @Column(name = "year_number", nullable = false)
    private Integer yearNumber;

    @Column(name = "mentor_name")
    private String mentorName;

    @Column(name = "first_hour_test_marks")
    private Double firstHourTestMarks;

    private Double cat1;
    
    private Double cat2;

    private Double model;

    private String grade;

    private Double gpa;

    @Column(name = "cgpa_till_now")
    private Double cgpaTillNow;

    @Column(name = "history_of_arrears")
    private Integer historyOfArrears;

    @Column(name = "standing_arrears")
    private Integer standingArrears;
}
