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

    @Transactional
    public MentorResponseDto updateMentor(Long id, MentorRequestDto request) {
        Mentor mentor = mentorRepository.findById(id).orElse(null);

        if (mentor == null) {
            User user = userRepository.findByUsername(request.getUsername()).orElse(null);
            if (user == null) {
                user = User.builder()
                        .username(request.getUsername())
                        .password(passwordEncoder.encode(request.getPassword() != null && !request.getPassword().isEmpty() ? request.getPassword() : "mentor123"))
                        .name(request.getName())
                        .role(Role.ROLE_MENTOR)
                        .build();
                user = userRepository.save(user);
            } else {
                user.setName(request.getName());
                if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
                    user.setPassword(passwordEncoder.encode(request.getPassword()));
                }
                userRepository.save(user);
            }

            Department department = getOrDefaultDepartment(request.getDepartmentId());

            mentor = Mentor.builder()
                    .user(user)
                    .department(department)
                    .assignedYear(request.getAssignedYear())
                    .assignedSection(request.getAssignedSection())
                    .build();
            mentor = mentorRepository.save(mentor);
            return mapToResponseDto(mentor);
        }

        User user = mentor.getUser();
        user.setUsername(request.getUsername());
        user.setName(request.getName());
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        userRepository.save(user);

        Department department = getOrDefaultDepartment(request.getDepartmentId());

        mentor.setDepartment(department);
        mentor.setAssignedYear(request.getAssignedYear());
        mentor.setAssignedSection(request.getAssignedSection());
        mentor = mentorRepository.save(mentor);

        return mapToResponseDto(mentor);
    }

    private Department getOrDefaultDepartment(Long departmentId) {
        if (departmentId != null) {
            Department dept = departmentRepository.findById(departmentId).orElse(null);
            if (dept != null) {
                return dept;
            }
        }
        return departmentRepository.findAll().stream().findFirst()
                .orElseGet(() -> departmentRepository.save(Department.builder().name("Computer Science & Engineering").build()));
    }

    @Transactional
    public MentorResponseDto assignClassToMentor(Long id, Long departmentId, Integer year, String section) {
        Mentor mentor = mentorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mentor not found with id: " + id));

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + departmentId));

        mentor.setDepartment(department);
        mentor.setAssignedYear(year);
        mentor.setAssignedSection(section);
        mentor = mentorRepository.save(mentor);

        return mapToResponseDto(mentor);
    }

    public MentorResponseDto findMentorByIdOrUsername(String idOrUsername) {
        if (idOrUsername == null || idOrUsername.trim().isEmpty()) return null;

        try {
            Long numericId = Long.parseLong(idOrUsername.replaceAll("\\D+", ""));
            Mentor mentor = mentorRepository.findById(numericId).orElse(null);
            if (mentor != null) return mapToResponseDto(mentor);
        } catch (Exception ignored) {}

        User user = userRepository.findByUsername(idOrUsername).orElse(null);
        if (user != null) {
            Mentor mentor = mentorRepository.findByUser(user).orElse(null);
            if (mentor != null) return mapToResponseDto(mentor);
        }

        return mentorRepository.findAll().stream()
                .filter(m -> m.getUser().getUsername().equalsIgnoreCase(idOrUsername) || String.valueOf(m.getId()).equals(idOrUsername))
                .findFirst()
                .map(this::mapToResponseDto)
                .orElse(null);
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
                        .build())
                .assignedYear(mentor.getAssignedYear())
                .assignedSection(mentor.getAssignedSection())
                .build();
    }
}
