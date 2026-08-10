package com.college.sdm.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "extra_activities")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExtraActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(nullable = false)
    private String type; // NPTEL/OnlineCourse/IELTS/German/Japanese/Symposium/Conference

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String details;

    @Column(name = "certificate_path", length = 500)
    private String certificatePath;
}
