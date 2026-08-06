package com.college.sdm.repository;

import com.college.sdm.entity.SemesterRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SemesterRecordRepository extends JpaRepository<SemesterRecord, Long> {
    List<SemesterRecord> findByStudentId(Long studentId);
}
