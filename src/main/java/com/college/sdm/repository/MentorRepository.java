package com.college.sdm.repository;

import com.college.sdm.entity.Mentor;
import com.college.sdm.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MentorRepository extends JpaRepository<Mentor, Long> {
    Optional<Mentor> findByUser(User user);
    Optional<Mentor> findByUserId(Long userId);
    List<Mentor> findByDepartmentId(Long departmentId);
    List<Mentor> findByDepartment(com.college.sdm.entity.Department department);
    long countByDepartmentIdAndAssignedYearAndAssignedSection(Long departmentId, Integer assignedYear, String assignedSection);
}
