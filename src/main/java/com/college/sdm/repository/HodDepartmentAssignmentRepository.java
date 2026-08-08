package com.college.sdm.repository;

import com.college.sdm.entity.HodDepartmentAssignment;
import com.college.sdm.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface HodDepartmentAssignmentRepository extends JpaRepository<HodDepartmentAssignment, Long> {
    List<HodDepartmentAssignment> findByHod(User hod);
    List<HodDepartmentAssignment> findByHodId(Long hodId);
    boolean existsByHodIdAndDepartmentId(Long hodId, Long departmentId);

    @Modifying
    @Query("DELETE FROM HodDepartmentAssignment h WHERE h.hod.id = :hodId")
    void deleteByHodId(@Param("hodId") Long hodId);

    @Modifying
    @Query("DELETE FROM HodDepartmentAssignment h WHERE h.department.id = :departmentId")
    void deleteByDepartmentId(@Param("departmentId") Long departmentId);
}
