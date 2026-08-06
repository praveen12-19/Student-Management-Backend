package com.college.sdm.repository;

import com.college.sdm.entity.IndustrialVisit;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IndustrialVisitRepository extends JpaRepository<IndustrialVisit, Long> {
    List<IndustrialVisit> findByStudentId(Long studentId);
}
