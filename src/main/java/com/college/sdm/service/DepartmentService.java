package com.college.sdm.service;

import com.college.sdm.dto.DepartmentDto;
import com.college.sdm.entity.Department;
import com.college.sdm.entity.DepartmentYearSection;
import com.college.sdm.entity.HodDepartmentAssignment;
import com.college.sdm.entity.Role;
import com.college.sdm.entity.User;
import com.college.sdm.exception.ResourceNotFoundException;
import com.college.sdm.repository.DepartmentRepository;
import com.college.sdm.repository.DepartmentYearSectionRepository;
import com.college.sdm.repository.HodDepartmentAssignmentRepository;
import com.college.sdm.repository.MentorRepository;
import com.college.sdm.repository.StudentRepository;
import com.college.sdm.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HodDepartmentAssignmentRepository hodDepartmentAssignmentRepository;

    @Autowired
    private DepartmentYearSectionRepository departmentYearSectionRepository;

    @Autowired
    private MentorRepository mentorRepository;

    @Autowired
    private StudentRepository studentRepository;

    public List<DepartmentDto> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public DepartmentDto getDepartmentById(Long id) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
        return mapToDto(dept);
    }

    @Transactional
    public DepartmentDto createDepartment(DepartmentDto departmentDto, String currentUsername) {
        Department dept = Department.builder()
                .name(departmentDto.getName())
                .build();
        dept = departmentRepository.save(dept);

        // Auto-assign this department to the creating HOD if applicable
        User creator = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUsername));
        
        if (creator.getRole() == Role.ROLE_HOD) {
            HodDepartmentAssignment assignment = HodDepartmentAssignment.builder()
                    .hod(creator)
                    .department(dept)
                    .build();
            hodDepartmentAssignmentRepository.save(assignment);
        }

        return mapToDto(dept);
    }

    public DepartmentDto updateDepartment(Long id, DepartmentDto departmentDto) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
        dept.setName(departmentDto.getName());
        dept = departmentRepository.save(dept);
        return mapToDto(dept);
    }

    @Transactional
    public void deleteDepartment(Long id) {
        if (!departmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Department not found with id: " + id);
        }
        hodDepartmentAssignmentRepository.deleteByDepartmentId(id);
        List<DepartmentYearSection> sections = departmentYearSectionRepository.findByDepartmentId(id);
        departmentYearSectionRepository.deleteAll(sections);
        departmentRepository.deleteById(id);
    }

    public List<String> getSectionsForDeptAndYear(Long deptId, Integer year) {
        List<String> list = departmentYearSectionRepository.findByDepartmentIdAndYear(deptId, year)
                .stream()
                .map(DepartmentYearSection::getSectionName)
                .collect(Collectors.toList());
        if (list.isEmpty()) {
            return List.of("A", "B", "C");
        }
        return list;
    }

    @Transactional
    public void addSectionToDeptAndYear(Long deptId, Integer year, String sectionName) {
        Department dept = departmentRepository.findById(deptId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + deptId));
        String cleanSection = sectionName.trim().toUpperCase();
        if (cleanSection.isEmpty()) {
            throw new IllegalArgumentException("Section name cannot be empty");
        }
        if (departmentYearSectionRepository.existsByDepartmentIdAndYearAndSectionName(deptId, year, cleanSection)) {
            return; // Already exists
        }
        DepartmentYearSection dys = DepartmentYearSection.builder()
                .department(dept)
                .year(year)
                .sectionName(cleanSection)
                .build();
        departmentYearSectionRepository.save(dys);
    }

    @Transactional
    public void removeSectionFromDeptAndYear(Long deptId, Integer year, String sectionName) {
        String cleanSection = sectionName.trim().toUpperCase();
        long mentorCount = mentorRepository.countByDepartmentIdAndAssignedYearAndAssignedSection(deptId, year, cleanSection);
        long studentCount = studentRepository.countByDepartmentIdAndYearAndSection(deptId, year, cleanSection);

        if (mentorCount > 0 || studentCount > 0) {
            throw new IllegalArgumentException(
                String.format("Cannot delete section: %d mentor(s) and %d student(s) are assigned to section %s in year %d", mentorCount, studentCount, cleanSection, year)
            );
        }

        departmentYearSectionRepository.deleteByDepartmentIdAndYearAndSectionName(deptId, year, cleanSection);
    }

    public DepartmentDto mapToDto(Department department) {
        if (department == null) return null;
        return DepartmentDto.builder()
                .id(department.getId())
                .name(department.getName())
                .build();
    }
}
