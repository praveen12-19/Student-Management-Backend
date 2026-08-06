package com.college.sdm.repository;

import com.college.sdm.entity.Sibling;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SiblingRepository extends JpaRepository<Sibling, Long> {
    List<Sibling> findByStudentId(Long studentId);
}
