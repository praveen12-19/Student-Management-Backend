package com.college.sdm.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "indisciplinary_activities")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IndisciplinaryActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(nullable = false, length = 1000)
    private String description;

    private String date; // or LocalDate

    @Column(name = "added_by")
    private String addedBy; // username + role
}
