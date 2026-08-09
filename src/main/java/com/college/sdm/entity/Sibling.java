package com.college.sdm.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "siblings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sibling {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(nullable = false)
    private String name;

    private Integer age;

    private String occupation;

    private String qualification;

    @Column(name = "email_id")
    private String emailId;

    @Column(name = "mobile_number")
    private String mobileNumber;
}
