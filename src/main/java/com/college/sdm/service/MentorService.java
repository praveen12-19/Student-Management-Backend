package com.college.sdm.service;

import com.college.sdm.dto.DepartmentDto;
import com.college.sdm.dto.MentorRequestDto;
import com.college.sdm.dto.MentorResponseDto;
import com.college.sdm.entity.Department;
import com.college.sdm.entity.DepartmentYearSection;
import com.college.sdm.entity.Mentor;
import com.college.sdm.entity.Role;
import com.college.sdm.entity.Student;
import com.college.sdm.entity.User;
import com.college.sdm.exception.ResourceNotFoundException;
import com.college.sdm.repository.DepartmentRepository;
import com.college.sdm.repository.DepartmentYearSectionRepository;
import com.college.sdm.repository.MentorRepository;
import com.college.sdm.repository.StudentRepository;
import com.college.sdm.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MentorService {

    @Autowired
    private MentorRepository mentorRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private DepartmentYearSectionRepository departmentYearSectionRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private StudentService studentService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<MentorResponseDto> getAllMentors() {
        return mentorRepository.findAll().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public List<MentorResponseDto> getMentorsByDepartment(Long departmentId) {
        return mentorRepository.findByDepartmentId(departmentId).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public MentorResponseDto getMentorById(Long id) {
        Mentor mentor = mentorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mentor not found with id: " + id));
        return mapToResponseDto(mentor);
    }

    @Transactional
    public MentorResponseDto createMentor(MentorRequestDto request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + request.getUsername());
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required for new mentor");
        }

        Department department = getOrDefaultDepartment(request.getDepartmentId());
        validateDepartmentSection(department, request.getAssignedYear(), request.getAssignedSection());

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .role(Role.ROLE_MENTOR)
                .build();
        user = userRepository.save(user);

        Mentor mentor = Mentor.builder()
                .user(user)
                .department(department)
                .assignedYear(request.getAssignedYear())
                .assignedSection(request.getAssignedSection())
                .build();
        mentor = mentorRepository.save(mentor);

        return mapToResponseDto(mentor);
    }

    public Mentor findMentorEntityByIdOrUsername(String idOrUsername) {
        if (idOrUsername == null || idOrUsername.trim().isEmpty()) return null;

        String clean = idOrUsername.trim();

        // 1. Check exact username lookup first
        User user = userRepository.findByUsername(clean).orElse(null);
        if (user != null) {
            Mentor mentor = mentorRepository.findByUser(user).orElse(null);
            if (mentor != null) return mentor;
        }

        // 2. Check exact numeric ID lookup if clean string is purely digits
        if (clean.matches("\\d+")) {
            try {
                Long numericId = Long.parseLong(clean);
                Mentor mentor = mentorRepository.findById(numericId).orElse(null);
                if (mentor != null) return mentor;
            } catch (Exception ignored) {}
        }

        // 3. Fallback scan across all mentors by username, ID, or name
        return mentorRepository.findAll().stream()
                .filter(m -> m.getUser().getUsername().equalsIgnoreCase(clean)
                          || String.valueOf(m.getId()).equals(clean)
                          || m.getUser().getName().equalsIgnoreCase(clean))
                .findFirst()
                .orElse(null);
    }

    @Transactional
    public MentorResponseDto updateMentor(String idOrUsername, MentorRequestDto request) {
        Mentor mentor = findMentorEntityByIdOrUsername(idOrUsername);
        if (mentor == null) {
            throw new ResourceNotFoundException("Mentor not found: " + idOrUsername);
        }

        Department oldDept = mentor.getDepartment();
        Integer oldYear = mentor.getAssignedYear();
        String oldSection = mentor.getAssignedSection();

        Department department = getOrDefaultDepartment(request.getDepartmentId());
        validateDepartmentSection(department, request.getAssignedYear(), request.getAssignedSection());

        User user = mentor.getUser();
        user.setUsername(request.getUsername());
        user.setName(request.getName());
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        userRepository.save(user);

        mentor.setDepartment(department);
        mentor.setAssignedYear(request.getAssignedYear());
        mentor.setAssignedSection(request.getAssignedSection());
        mentor = mentorRepository.save(mentor);

        reconcileStudentsForMentorScopeChange(mentor, oldDept, oldYear, oldSection);

        return mapToResponseDto(mentor);
    }

    @Transactional
    public MentorResponseDto updateMentor(Long id, MentorRequestDto request) {
        return updateMentor(String.valueOf(id), request);
    }

    private void reconcileStudentsForMentorScopeChange(
            Mentor mentor, Department oldDept, Integer oldYear, String oldSection) {

        // 1. Detach students currently attached to this mentor who no longer match mentor's NEW scope
        List<Student> currentlyAssigned = studentRepository.findByMentor(mentor);
        for (Student s : currentlyAssigned) {
            boolean deptMatches = s.getDepartment() != null && mentor.getDepartment() != null
                    && s.getDepartment().getId().equals(mentor.getDepartment().getId());
            boolean yearsMatches = studentService.yearsMatch(s.getYear(), mentor.getAssignedYear());
            boolean sectionsMatches = (mentor.getAssignedSection() == null || mentor.getAssignedSection().trim().isEmpty())
                    || studentService.sectionsMatch(mentor.getAssignedSection(), s.getSection());

            boolean stillMatches = deptMatches && yearsMatches && sectionsMatches;
            if (!stillMatches) {
                s.setMentor(null);
                studentRepository.save(s);
            }
        }

        // 2. Pick up unassigned students matching mentor's NEW scope
        List<Student> candidates;
        if (mentor.getDepartment() != null && mentor.getAssignedYear() != null) {
            candidates = studentRepository.findByDepartmentAndYear(mentor.getDepartment(), mentor.getAssignedYear());
        } else if (mentor.getDepartment() != null) {
            candidates = studentRepository.findByDepartment(mentor.getDepartment());
        } else {
            candidates = java.util.Collections.emptyList();
        }

        for (Student s : candidates) {
            boolean yearsMatches = mentor.getAssignedYear() == null || studentService.yearsMatch(s.getYear(), mentor.getAssignedYear());
            boolean sectionsMatches = (mentor.getAssignedSection() == null || mentor.getAssignedSection().trim().isEmpty())
                    || studentService.sectionsMatch(mentor.getAssignedSection(), s.getSection());
            if (yearsMatches && sectionsMatches && s.getMentor() == null) {
                s.setMentor(mentor);
                studentRepository.save(s);
            }
        }
    }

    private Department getOrDefaultDepartment(Long departmentId) {
        if (departmentId != null && departmentId > 0) {
            Department dept = departmentRepository.findById(departmentId).orElse(null);
            if (dept != null) {
                return dept;
            }
        }
        List<Department> all = departmentRepository.findAll();
        if (!all.isEmpty()) {
            return all.get(0);
        }
        throw new ResourceNotFoundException("Department not found with id: " + departmentId);
    }

    private void validateDepartmentSection(Department department, Integer year, String section) {
        if (department != null && year != null && section != null && !section.trim().isEmpty()) {
            String cleanSec = section.trim().toUpperCase();
            if (!departmentYearSectionRepository.existsByDepartmentIdAndYearAndSectionName(department.getId(), year, cleanSec)) {
                DepartmentYearSection dys = DepartmentYearSection.builder()
                        .department(department)
                        .year(year)
                        .sectionName(cleanSec)
                        .build();
                departmentYearSectionRepository.save(dys);
            }
        }
    }

    @Transactional
    public MentorResponseDto assignClassToMentor(String idOrUsername, Long departmentId, Integer year, String section) {
        Mentor mentor = findMentorEntityByIdOrUsername(idOrUsername);
        if (mentor == null) {
            throw new ResourceNotFoundException("Mentor not found: " + idOrUsername);
        }

        Department oldDept = mentor.getDepartment();
        Integer oldYear = mentor.getAssignedYear();
        String oldSection = mentor.getAssignedSection();

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + departmentId));

        validateDepartmentSection(department, year, section);

        mentor.setDepartment(department);
        mentor.setAssignedYear(year);
        mentor.setAssignedSection(section);
        mentor = mentorRepository.save(mentor);

        reconcileStudentsForMentorScopeChange(mentor, oldDept, oldYear, oldSection);

        return mapToResponseDto(mentor);
    }

    @Transactional
    public MentorResponseDto assignClassToMentor(Long id, Long departmentId, Integer year, String section) {
        return assignClassToMentor(String.valueOf(id), departmentId, year, section);
    }

    public MentorResponseDto findMentorByIdOrUsername(String idOrUsername) {
        Mentor mentor = findMentorEntityByIdOrUsername(idOrUsername);
        return mentor != null ? mapToResponseDto(mentor) : null;
    }

    @Transactional
    public void deleteMentor(Long id) {
        Mentor mentor = mentorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mentor not found with id: " + id));

        List<Student> assignedStudents = studentRepository.findByMentor(mentor);
        for (Student s : assignedStudents) {
            s.setMentor(null);
            studentRepository.save(s);
        }

        User user = mentor.getUser();
        mentorRepository.delete(mentor);
        if (user != null) {
            userRepository.delete(user);
        }
    }

    public MentorResponseDto mapToResponseDto(Mentor mentor) {
        if (mentor == null) return null;
        return MentorResponseDto.builder()
                .id(mentor.getId())
                .username(mentor.getUser().getUsername())
                .name(mentor.getUser().getName())
                .department(DepartmentDto.builder()
                        .id(mentor.getDepartment().getId())
                        .name(mentor.getDepartment().getName())
                        .build())
                .assignedYear(mentor.getAssignedYear())
                .assignedSection(mentor.getAssignedSection())
                .build();
    }
}
