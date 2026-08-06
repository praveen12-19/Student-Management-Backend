package com.college.sdm.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "twelfth_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TwelfthDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false, unique = true)
    private Student student;

    @Column(name = "school_name")
    private String schoolName;

    private String medium;

    @Column(name = "twelfth_group")
    private String group;

    @Column(name = "total_marks")
    private Double totalMarks;

    private Double percentage;

    private Double cutoff;

    @Column(name = "physics_marks")
    private Double physicsMarks;

    @Column(name = "chemistry_marks")
    private Double chemistryMarks;

    @Column(name = "maths_marks")
    private Double mathsMarks;

    @Column(name = "year_of_passing")
    private Integer yearOfPassing;
}
