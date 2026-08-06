package com.college.sdm.repository;

import com.college.sdm.entity.ExtraActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ExtraActivityRepository extends JpaRepository<ExtraActivity, Long> {
    List<ExtraActivity> findByStudentId(Long studentId);
}
