package com.college.sdm.repository;

import com.college.sdm.entity.TenthDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TenthDetailRepository extends JpaRepository<TenthDetail, Long> {
    Optional<TenthDetail> findByStudentId(Long studentId);
}
