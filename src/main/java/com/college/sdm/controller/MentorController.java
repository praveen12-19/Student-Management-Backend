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
            return ResponseEntity.ok(new ArrayList<>());
        }

        List<MentorResponseDto> mentors = new ArrayList<>();
        for (Department dept : managedDepts) {
            mentors.addAll(mentorService.getMentorsByDepartment(dept.getId()));
        }

        return ResponseEntity.ok(mentors);
    }

    private boolean hasDepartmentAccess(User user, Long departmentId) {
        if (user.getRole() == Role.ROLE_ADMIN) return true;
        if (user.getRole() == Role.ROLE_HOD) {
            if (departmentId == null) return false;
            return hodDepartmentAssignmentRepository.existsByHodIdAndDepartmentId(user.getId(), departmentId);
        }
        return false;
    }

    @PostMapping
    public ResponseEntity<MentorResponseDto> createMentor(@Valid @RequestBody MentorRequestDto request, Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + principal.getName()));

        if (!hasDepartmentAccess(user, request.getDepartmentId())) {
            return ResponseEntity.status(403).build();
        }

        MentorResponseDto created = mentorService.createMentor(request);
        systemLogService.log(principal.getName(), "Create Mentor", "Added mentor profile: " + created.getName() + " (@" + created.getUsername() + ")");
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MentorResponseDto> updateMentor(
            @PathVariable String id,
            @Valid @RequestBody MentorRequestDto request,
            Principal principal) {

        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + principal.getName()));

        MentorResponseDto mentor = mentorService.findMentorByIdOrUsername(id);
        if (mentor == null) {
            return ResponseEntity.notFound().build();
        }

        if (!hasDepartmentAccess(user, mentor.getDepartment() != null ? mentor.getDepartment().getId() : null)
                || !hasDepartmentAccess(user, request.getDepartmentId())) {
            return ResponseEntity.status(403).build();
        }

        MentorResponseDto updated = mentorService.updateMentor(id, request);
        systemLogService.log(principal.getName(), "Update Mentor", "Updated details for mentor: " + updated.getName() + " (@" + updated.getUsername() + ")");
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}/assign-class")
    public ResponseEntity<MentorResponseDto> assignClass(
            @PathVariable String id,
            @RequestBody Map<String, Object> body,
            Principal principal) {

        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + principal.getName()));

        MentorResponseDto mentor = mentorService.findMentorByIdOrUsername(id);
        if (mentor == null) {
            return ResponseEntity.notFound().build();
        }

        Long departmentId = ((Number) body.get("departmentId")).longValue();
        Integer year = ((Number) body.get("assignedYear")).intValue();
        String section = (String) body.get("assignedSection");

        if (!hasDepartmentAccess(user, mentor.getDepartment() != null ? mentor.getDepartment().getId() : null)
                || !hasDepartmentAccess(user, departmentId)) {
            return ResponseEntity.status(403).build();
        }

        MentorResponseDto updated = mentorService.assignClassToMentor(id, departmentId, year, section);
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

        if (!hasDepartmentAccess(user, mentor.getDepartment() != null ? mentor.getDepartment().getId() : null)) {
            return ResponseEntity.status(403).build();
        }

        mentorService.deleteMentor(mentor.getId());
        systemLogService.log(principal.getName(), "Delete Mentor", "Removed mentor profile: " + mentor.getName() + " (@" + mentor.getUsername() + ")");
        return ResponseEntity.noContent().build();
    }
}
