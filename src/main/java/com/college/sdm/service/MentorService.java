package com.college.sdm.service;

import com.college.sdm.dto.DepartmentDto;
import com.college.sdm.dto.MentorRequestDto;
import com.college.sdm.dto.MentorResponseDto;
import com.college.sdm.entity.Department;
import com.college.sdm.entity.Mentor;
import com.college.sdm.entity.Role;
import com.college.sdm.entity.User;
import com.college.sdm.exception.ResourceNotFoundException;
import com.college.sdm.repository.DepartmentRepository;
import com.college.sdm.repository.MentorRepository;
import com.college.sdm.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        validateDepartmentSection(department, request.getAssignedSection());

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

        Department department = getOrDefaultDepartment(request.getDepartmentId());
        validateDepartmentSection(department, request.getAssignedSection());

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

        return mapToResponseDto(mentor);
    }

    @Transactional
    public MentorResponseDto updateMentor(Long id, MentorRequestDto request) {
        return updateMentor(String.valueOf(id), request);
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

    private void validateDepartmentSection(Department department, String section) {
        if (department != null && department.getSections() != null && !department.getSections().isEmpty() && section != null && !section.trim().isEmpty()) {
            boolean valid = department.getSections().stream()
                    .anyMatch(s -> s.equalsIgnoreCase(section.trim()))
                    || section.equalsIgnoreCase("A")
                    || section.equalsIgnoreCase("B");
            if (!valid) {
                throw new IllegalArgumentException("Section '" + section + "' does not belong to department '" + department.getName() + "'");
            }
        }
    }

    @Transactional
    public MentorResponseDto assignClassToMentor(String idOrUsername, Long departmentId, Integer year, String section) {
        Mentor mentor = findMentorEntityByIdOrUsername(idOrUsername);
        if (mentor == null) {
            throw new ResourceNotFoundException("Mentor not found: " + idOrUsername);
        }

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + departmentId));

        validateDepartmentSection(department, section);

        mentor.setDepartment(department);
        mentor.setAssignedYear(year);
        mentor.setAssignedSection(section);
        mentor = mentorRepository.save(mentor);

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

    @Autowired
    private com.college.sdm.repository.StudentRepository studentRepository;

    @Transactional
    public void deleteMentor(Long id) {
        Mentor mentor = mentorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mentor not found with id: " + id));

        List<com.college.sdm.entity.Student> assignedStudents = studentRepository.findByMentor(mentor);
        for (com.college.sdm.entity.Student s : assignedStudents) {
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
                        .sections(mentor.getDepartment().getSections() != null ? mentor.getDepartment().getSections() : java.util.Collections.emptyList())
                        .build())
                .assignedYear(mentor.getAssignedYear())
                .assignedSection(mentor.getAssignedSection())
                .build();
    }
}
