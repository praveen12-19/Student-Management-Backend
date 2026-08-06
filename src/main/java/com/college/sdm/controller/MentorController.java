package com.college.sdm.controller;

import com.college.sdm.dto.MentorRequestDto;
import com.college.sdm.dto.MentorResponseDto;
import com.college.sdm.entity.Department;
import com.college.sdm.entity.HodDepartmentAssignment;
import com.college.sdm.entity.Role;
import com.college.sdm.entity.User;
import com.college.sdm.exception.ResourceNotFoundException;
import com.college.sdm.repository.HodDepartmentAssignmentRepository;
import com.college.sdm.repository.UserRepository;
import com.college.sdm.service.MentorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/mentors")
@PreAuthorize("hasAnyRole('ADMIN', 'HOD')")
public class MentorController {

    @Autowired
    private MentorService mentorService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HodDepartmentAssignmentRepository hodDepartmentAssignmentRepository;

    @Autowired
    private com.college.sdm.repository.DepartmentRepository departmentRepository;

    @Autowired
    private com.college.sdm.service.SystemLogService systemLogService;

    @GetMapping
    public ResponseEntity<List<MentorResponseDto>> getMentors(Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + principal.getName()));

        if (user.getRole() == Role.ROLE_ADMIN) {
            return ResponseEntity.ok(mentorService.getAllMentors());
        }

        List<Department> managedDepts = hodDepartmentAssignmentRepository.findByHod(user).stream()
                .map(HodDepartmentAssignment::getDepartment)
                .collect(Collectors.toList());

        if (managedDepts.isEmpty()) {
            return ResponseEntity.ok(mentorService.getAllMentors());
        }

        List<MentorResponseDto> mentors = new ArrayList<>();
        for (Department dept : managedDepts) {
            mentors.addAll(mentorService.getMentorsByDepartment(dept.getId()));
        }

        return ResponseEntity.ok(mentors);
    }

    private void ensureHodDepartmentAssignment(User user, Long departmentId) {
        if (user.getRole() == Role.ROLE_HOD && departmentId != null) {
            Department dept = departmentRepository.findById(departmentId).orElse(null);
            if (dept != null && !hodDepartmentAssignmentRepository.existsByHodIdAndDepartmentId(user.getId(), dept.getId())) {
                HodDepartmentAssignment assignment = HodDepartmentAssignment.builder()
                        .hod(user)
                        .department(dept)
                        .build();
                hodDepartmentAssignmentRepository.save(assignment);
            }
        }
    }

    @PostMapping
    public ResponseEntity<MentorResponseDto> createMentor(@Valid @RequestBody MentorRequestDto request, Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + principal.getName()));

        boolean manages = user.getRole() == Role.ROLE_ADMIN || user.getRole() == Role.ROLE_HOD || hodDepartmentAssignmentRepository.existsByHodIdAndDepartmentId(user.getId(), request.getDepartmentId());
        if (!manages) {
            return ResponseEntity.status(403).build();
        }

        MentorResponseDto created = mentorService.createMentor(request);
        ensureHodDepartmentAssignment(user, created.getDepartment() != null ? created.getDepartment().getId() : request.getDepartmentId());
        systemLogService.log(principal.getName(), "Create Mentor", "Added mentor profile: " + created.getName() + " (@" + created.getUsername() + ")");
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MentorResponseDto> updateMentor(
            @PathVariable Long id,
            @Valid @RequestBody MentorRequestDto request,
            Principal principal) {

        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + principal.getName()));

        MentorResponseDto mentor = mentorService.getMentorById(id);
        boolean manages = user.getRole() == Role.ROLE_ADMIN || user.getRole() == Role.ROLE_HOD || hodDepartmentAssignmentRepository.existsByHodIdAndDepartmentId(user.getId(), mentor.getDepartment().getId());
        if (!manages) {
            return ResponseEntity.status(403).build();
        }

        MentorResponseDto updated = mentorService.updateMentor(id, request);
        ensureHodDepartmentAssignment(user, updated.getDepartment() != null ? updated.getDepartment().getId() : request.getDepartmentId());
        systemLogService.log(principal.getName(), "Update Mentor", "Updated details for mentor: " + updated.getName() + " (@" + updated.getUsername() + ")");
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}/assign-class")
    public ResponseEntity<MentorResponseDto> assignClass(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            Principal principal) {

        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + principal.getName()));

        Long departmentId = ((Number) body.get("departmentId")).longValue();
        Integer year = ((Number) body.get("assignedYear")).intValue();
        String section = (String) body.get("assignedSection");

        MentorResponseDto mentor = mentorService.getMentorById(id);
        boolean managesCurrent = user.getRole() == Role.ROLE_ADMIN || user.getRole() == Role.ROLE_HOD || hodDepartmentAssignmentRepository.existsByHodIdAndDepartmentId(user.getId(), mentor.getDepartment().getId());
        boolean managesTarget = user.getRole() == Role.ROLE_ADMIN || user.getRole() == Role.ROLE_HOD || hodDepartmentAssignmentRepository.existsByHodIdAndDepartmentId(user.getId(), departmentId);

        if (!managesCurrent || !managesTarget) {
            return ResponseEntity.status(403).build();
        }

        MentorResponseDto updated = mentorService.assignClassToMentor(id, departmentId, year, section);
        ensureHodDepartmentAssignment(user, updated.getDepartment() != null ? updated.getDepartment().getId() : departmentId);
        systemLogService.log(principal.getName(), "Assign Class", "Assigned class to mentor " + updated.getName() + ": Year " + updated.getAssignedYear() + " Section " + updated.getAssignedSection());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMentor(@PathVariable String id, Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + principal.getName()));

        MentorResponseDto mentor = mentorService.findMentorByIdOrUsername(id);
        if (mentor == null) {
            return ResponseEntity.noContent().build();
        }

        boolean manages = user.getRole() == Role.ROLE_ADMIN || user.getRole() == Role.ROLE_HOD || hodDepartmentAssignmentRepository.existsByHodIdAndDepartmentId(user.getId(), mentor.getDepartment().getId());
        if (!manages) {
            return ResponseEntity.status(403).build();
        }

        mentorService.deleteMentor(mentor.getId());
        systemLogService.log(principal.getName(), "Delete Mentor", "Removed mentor profile: " + mentor.getName() + " (@" + mentor.getUsername() + ")");
        return ResponseEntity.noContent().build();
    }
}
