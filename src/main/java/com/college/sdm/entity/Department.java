package com.college.sdm.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "departments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "department_sections", joinColumns = @JoinColumn(name = "department_id"))
    @Column(name = "section_name")
    @Builder.Default
    private java.util.List<String> sections = new java.util.ArrayList<>();
}
