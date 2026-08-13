package com.college.sdm.repository;

import com.college.sdm.entity.Department;
import com.college.sdm.entity.Mentor;
import com.college.sdm.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByRegisterNumber(String registerNumber);
    
    List<Student> findByDepartmentIn(List<Department> departments);

    List<Student> findByDepartment(Department department);

    List<Student> findByDepartmentAndYear(Department department, Integer year);

    List<Student> findByMentor(Mentor mentor);

    long countByDepartmentIdAndYearAndSection(Long departmentId, Integer year, String section);

    @Query("SELECT s FROM Student s WHERE s.department = :department AND s.year = :year AND s.mentor IS NULL")
    List<Student> findUnassignedStudents(@Param("department") Department department, @Param("year") Integer year);

    @Query("SELECT s FROM Student s WHERE s.department IN :departments AND " +
           "(LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(s.registerNumber) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Student> searchStudentsForHod(@Param("departments") List<Department> departments, @Param("query") String query);

    @Query("SELECT s FROM Student s WHERE s.mentor = :mentor AND " +
           "(LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(s.registerNumber) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Student> searchStudentsForMentor(@Param("mentor") Mentor mentor, @Param("query") String query);

    @Query("SELECT s FROM Student s WHERE " +
           "LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(s.registerNumber) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Student> searchStudentsGlobal(@Param("query") String query);
}
