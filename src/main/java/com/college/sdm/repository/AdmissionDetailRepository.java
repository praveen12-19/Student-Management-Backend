package com.college.sdm.repository;

import com.college.sdm.entity.AdmissionDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AdmissionDetailRepository extends JpaRepository<AdmissionDetail, Long> {
    Optional<AdmissionDetail> findByStudentId(Long studentId);
}
