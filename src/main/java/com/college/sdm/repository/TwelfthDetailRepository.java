package com.college.sdm.repository;

import com.college.sdm.entity.TwelfthDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TwelfthDetailRepository extends JpaRepository<TwelfthDetail, Long> {
    Optional<TwelfthDetail> findByStudentId(Long studentId);
}
