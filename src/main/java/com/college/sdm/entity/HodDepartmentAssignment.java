package com.college.sdm.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "hod_department_assignments",
    uniqueConstraints = @UniqueConstraint(columnNames = {"hod_id", "department_id"})
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HodDepartmentAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hod_id", nullable = false)
    private User hod;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;
}
