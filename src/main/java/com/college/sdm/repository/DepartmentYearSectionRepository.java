package com.college.sdm.repository;

import com.college.sdm.entity.DepartmentYearSection;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DepartmentYearSectionRepository extends JpaRepository<DepartmentYearSection, Long> {
    List<DepartmentYearSection> findByDepartmentIdAndYear(Long departmentId, Integer year);
    List<DepartmentYearSection> findByDepartmentId(Long departmentId);
    Optional<DepartmentYearSection> findByDepartmentIdAndYearAndSectionName(Long departmentId, Integer year, String sectionName);
    boolean existsByDepartmentIdAndYearAndSectionName(Long departmentId, Integer year, String sectionName);
    void deleteByDepartmentIdAndYearAndSectionName(Long departmentId, Integer year, String sectionName);
}
