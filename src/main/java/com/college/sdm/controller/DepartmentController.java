package com.college.sdm.controller;

import com.college.sdm.dto.DepartmentDto;
import com.college.sdm.entity.*;
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
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + principal.getName()));

        if (user.getRole() == Role.ROLE_HOD) {
            List<Department> managedDepts = hodDepartmentAssignmentRepository.findByHod(user).stream()
                    .map(HodDepartmentAssignment::getDepartment)
                    .collect(Collectors.toList());
            if (managedDepts.isEmpty()) {
                return ResponseEntity.ok(departmentService.getAllDepartments());
            }
            return ResponseEntity.ok(managedDepts.stream().map(departmentService::mapToDto).collect(Collectors.toList()));
        }

        return ResponseEntity.ok(departmentService.getAllDepartments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepartmentDto> getDepartmentById(@PathVariable Long id, Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + principal.getName()));

        boolean manages = user.getRole() == Role.ROLE_ADMIN || user.getRole() == Role.ROLE_HOD;
        if (!manages) {
            return ResponseEntity.status(403).build();
        }

        DepartmentDto departmentDto = departmentService.getDepartmentById(id);
        return ResponseEntity.ok(departmentDto);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DepartmentDto> createDepartment(@Valid @RequestBody DepartmentDto departmentDto, Principal principal) {
        DepartmentDto created = departmentService.createDepartment(departmentDto, principal.getName());
        systemLogService.log(principal.getName(), "Create Department", "Added new department: " + created.getName());
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOD')")
    public ResponseEntity<DepartmentDto> updateDepartment(@PathVariable Long id, @Valid @RequestBody DepartmentDto departmentDto, Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + principal.getName()));

        if (user.getRole() != Role.ROLE_ADMIN && user.getRole() != Role.ROLE_HOD) {
            return ResponseEntity.status(403).build();
        }

        DepartmentDto updated = departmentService.updateDepartment(id, departmentDto);
        systemLogService.log(principal.getName(), "Update Department", "Modified department: " + updated.getName());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id, Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + principal.getName()));

        if (user.getRole() != Role.ROLE_ADMIN) {
            return ResponseEntity.status(403).build();
        }

        departmentService.deleteDepartment(id);
        systemLogService.log(principal.getName(), "Delete Department", "Removed department ID: " + id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{deptId}/years/{year}/sections")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOD', 'MENTOR')")
    public ResponseEntity<List<String>> getSectionsForYear(
            @PathVariable Long deptId,
            @PathVariable Integer year) {
        return ResponseEntity.ok(departmentService.getSectionsForDeptAndYear(deptId, year));
    }

    @PostMapping("/{deptId}/years/{year}/sections")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOD')")
    public ResponseEntity<?> addSectionForYear(
            @PathVariable Long deptId,
            @PathVariable Integer year,
            @RequestBody java.util.Map<String, String> body,
            Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + principal.getName()));

        if (user.getRole() == Role.ROLE_HOD) {
            boolean manages = hodDepartmentAssignmentRepository.existsByHodIdAndDepartmentId(user.getId(), deptId)
                    || hodDepartmentAssignmentRepository.findByHod(user).isEmpty();
            if (!manages) {
                return ResponseEntity.status(403).body("Not authorized to manage sections for this department");
            }
        }

        String sectionName = body.get("sectionName");
        if (sectionName == null || sectionName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("sectionName is required");
        }

        departmentService.addSectionToDeptAndYear(deptId, year, sectionName);
        systemLogService.log(principal.getName(), "Add Section", "Added section " + sectionName + " to department ID: " + deptId + " year: " + year);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{deptId}/years/{year}/sections/{sectionName}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOD')")
    public ResponseEntity<?> removeSectionForYear(
            @PathVariable Long deptId,
            @PathVariable Integer year,
            @PathVariable String sectionName,
            Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + principal.getName()));

        if (user.getRole() == Role.ROLE_HOD) {
            boolean manages = hodDepartmentAssignmentRepository.existsByHodIdAndDepartmentId(user.getId(), deptId)
                    || hodDepartmentAssignmentRepository.findByHod(user).isEmpty();
            if (!manages) {
                return ResponseEntity.status(403).body("Not authorized to manage sections for this department");
            }
        }

        try {
            departmentService.removeSectionFromDeptAndYear(deptId, year, sectionName);
            systemLogService.log(principal.getName(), "Remove Section", "Removed section " + sectionName + " from department ID: " + deptId + " year: " + year);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
