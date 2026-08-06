package com.college.sdm.repository;

import com.college.sdm.entity.IndisciplinaryActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IndisciplinaryActivityRepository extends JpaRepository<IndisciplinaryActivity, Long> {
    List<IndisciplinaryActivity> findByStudentId(Long studentId);
}
