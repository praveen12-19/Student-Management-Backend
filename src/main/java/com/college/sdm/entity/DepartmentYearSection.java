package com.college.sdm.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "department_year_sections",
       uniqueConstraints = @UniqueConstraint(columnNames = {"department_id", "year", "section_name"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentYearSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(nullable = false)
    private Integer year; // 1-4

    @Column(name = "section_name", nullable = false)
    private String sectionName; // "A", "B", "C", ...
}
