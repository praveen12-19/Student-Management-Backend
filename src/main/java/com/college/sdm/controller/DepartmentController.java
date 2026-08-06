package com.college.sdm.controller;

import com.college.sdm.dto.DepartmentDto;
import com.college.sdm.entity.HodDepartmentAssignment;
import com.college.sdm.entity.Role;
import com.college.sdm.entity.User;
import com.college.sdm.exception.ResourceNotFoundException;
import com.college.sdm.repository.HodDepartmentAssignmentRepository;
import com.college.sdm.repository.UserRepository;
import com.college.sdm.service.DepartmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HodDepartmentAssignmentRepository hodDepartmentAssignmentRepository;

    @Autowired
    private com.college.sdm.service.SystemLogService systemLogService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HOD', 'MENTOR')")
    public ResponseEntity<List<DepartmentDto>> getDepartments(Principal principal) {
        return ResponseEntity.ok(departmentService.getAllDepartments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepartmentDto> getDepartmentById(@PathVariable Long id, Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + principal.getName()));

        boolean manages = user.getRole() == Role.ROLE_ADMIN || hodDepartmentAssignmentRepository.existsByHodIdAndDepartmentId(user.getId(), id);
        if (!manages) {
            return ResponseEntity.status(403).build();
        }

        DepartmentDto departmentDto = departmentService.getDepartmentById(id);
        return ResponseEntity.ok(departmentDto);
    }

    @PostMapping
    public ResponseEntity<DepartmentDto> createDepartment(@Valid @RequestBody DepartmentDto departmentDto, Principal principal) {
        DepartmentDto created = departmentService.createDepartment(departmentDto, principal.getName());
        systemLogService.log(principal.getName(), "Create Department", "Added new department: " + created.getName());
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DepartmentDto> updateDepartment(@PathVariable Long id, @Valid @RequestBody DepartmentDto departmentDto, Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + principal.getName()));

        boolean manages = user.getRole() == Role.ROLE_ADMIN || hodDepartmentAssignmentRepository.existsByHodIdAndDepartmentId(user.getId(), id);
        if (!manages) {
            return ResponseEntity.status(403).build();
        }

        DepartmentDto updated = departmentService.updateDepartment(id, departmentDto);
        systemLogService.log(principal.getName(), "Update Department", "Modified department: " + updated.getName());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id, Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + principal.getName()));

        boolean manages = user.getRole() == Role.ROLE_ADMIN || hodDepartmentAssignmentRepository.existsByHodIdAndDepartmentId(user.getId(), id);
        if (!manages) {
            return ResponseEntity.status(403).build();
        }

        departmentService.deleteDepartment(id);
        systemLogService.log(principal.getName(), "Delete Department", "Removed department ID: " + id);
        return ResponseEntity.noContent().build();
    }
}
