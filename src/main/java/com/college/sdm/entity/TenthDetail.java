package com.college.sdm.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tenth_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenthDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false, unique = true)
    private Student student;

    @Column(name = "school_name")
    private String schoolName;

    private String medium;

    @Column(name = "total_marks")
    private String totalMarks;

    private Double percentage;

    @Column(name = "year_of_passing")
    private Integer yearOfPassing;
}
