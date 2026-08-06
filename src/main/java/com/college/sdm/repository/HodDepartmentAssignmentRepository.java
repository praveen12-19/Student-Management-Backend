package com.college.sdm.repository;

import com.college.sdm.entity.HodDepartmentAssignment;
import com.college.sdm.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HodDepartmentAssignmentRepository extends JpaRepository<HodDepartmentAssignment, Long> {
    List<HodDepartmentAssignment> findByHod(User hod);
    List<HodDepartmentAssignment> findByHodId(Long hodId);
    boolean existsByHodIdAndDepartmentId(Long hodId, Long departmentId);
    void deleteByHodId(Long hodId);
    void deleteByDepartmentId(Long departmentId);
}
