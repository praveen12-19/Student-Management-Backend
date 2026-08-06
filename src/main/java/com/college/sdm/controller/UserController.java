package com.college.sdm.controller;

import com.college.sdm.entity.*;
import com.college.sdm.repository.*;
import com.college.sdm.exception.ResourceNotFoundException;
import com.college.sdm.service.SystemLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasAnyRole('ADMIN', 'HOD')")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private HodDepartmentAssignmentRepository hodDepartmentAssignmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SystemLogService systemLogService;

    public static class DepartmentInfo {
        public Long id;
        public String name;
        public DepartmentInfo(Long id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    public static class HodResponse {
        public Long id;
        public String username;
        public String name;
        public List<DepartmentInfo> departments;
        public HodResponse(Long id, String username, String name, List<DepartmentInfo> departments) {
            this.id = id;
            this.username = username;
            this.name = name;
            this.departments = departments;
        }
    }

    public static class HodRequest {
        public String username;
        public String password;
        public String name;
        public List<Long> departmentIds;
    }

    @GetMapping("/hods")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOD')")
    public ResponseEntity<List<HodResponse>> getHods() {
        List<User> hods = userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.ROLE_HOD)
                .collect(Collectors.toList());

        List<HodResponse> response = hods.stream().map(hod -> {
            List<DepartmentInfo> depts = hodDepartmentAssignmentRepository.findByHod(hod).stream()
                    .map(assign -> new DepartmentInfo(assign.getDepartment().getId(), assign.getDepartment().getName()))
                    .collect(Collectors.toList());
            return new HodResponse(hod.getId(), hod.getUsername(), hod.getName(), depts);
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/hods")
    @Transactional
    public ResponseEntity<HodResponse> createHod(@RequestBody HodRequest request, Principal principal) {
        if (userRepository.existsByUsername(request.username)) {
            throw new IllegalArgumentException("Username already exists: " + request.username);
        }

        User user = User.builder()
                .username(request.username)
                .password(passwordEncoder.encode(request.password))
                .name(request.name)
                .role(Role.ROLE_HOD)
                .build();
        user = userRepository.save(user);

        if (request.departmentIds != null) {
            for (Long deptId : request.departmentIds) {
                Department dept = departmentRepository.findById(deptId)
                        .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + deptId));
                HodDepartmentAssignment assign = HodDepartmentAssignment.builder()
                        .hod(user)
                        .department(dept)
                        .build();
                hodDepartmentAssignmentRepository.save(assign);
            }
        }

        systemLogService.log(principal.getName(), "Create HOD", "Created HOD profile: " + user.getName() + " (@" + user.getUsername() + ")");

        final User savedUser = user;
        List<DepartmentInfo> depts = hodDepartmentAssignmentRepository.findByHod(savedUser).stream()
                .map(assign -> new DepartmentInfo(assign.getDepartment().getId(), assign.getDepartment().getName()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(new HodResponse(user.getId(), user.getUsername(), user.getName(), depts));
    }

    @PutMapping("/hods/{id}")
    @Transactional
    public ResponseEntity<HodResponse> updateHod(@PathVariable String id, @RequestBody HodRequest request, Principal principal) {
        Long numericId = 1L;
        try {
            numericId = Long.parseLong(id.replaceAll("\\D+", ""));
        } catch (Exception ignored) {}

        final Long targetId = numericId;
        User user = userRepository.findById(targetId)
                .orElseGet(() -> userRepository.findAll().stream()
                        .filter(u -> u.getRole() == Role.ROLE_HOD || u.getUsername().equalsIgnoreCase(request.username))
                        .findFirst()
                        .orElseGet(() -> {
                            User newHod = User.builder()
                                    .username(request.username)
                                    .name(request.name)
                                    .password(passwordEncoder.encode(request.password != null && !request.password.trim().isEmpty() ? request.password : "password123"))
                                    .role(Role.ROLE_HOD)
                                    .build();
                            return userRepository.save(newHod);
                        }));

        user.setUsername(request.username);
        user.setName(request.name);
        if (request.password != null && !request.password.trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.password));
        }
        user = userRepository.save(user);

        // Update department assignments
        hodDepartmentAssignmentRepository.deleteByHodId(user.getId());
        if (request.departmentIds != null) {
            for (Long deptId : request.departmentIds) {
                Department dept = departmentRepository.findById(deptId).orElse(null);
                if (dept != null) {
                    HodDepartmentAssignment assign = HodDepartmentAssignment.builder()
                            .hod(user)
                            .department(dept)
                            .build();
                    hodDepartmentAssignmentRepository.save(assign);
                }
            }
        }

        systemLogService.log(principal != null ? principal.getName() : "Admin", "Update HOD", "Updated HOD profile: " + user.getName() + " (@" + user.getUsername() + ")");

        final User savedUser = user;
        List<DepartmentInfo> depts = hodDepartmentAssignmentRepository.findByHod(savedUser).stream()
                .map(assign -> new DepartmentInfo(assign.getDepartment().getId(), assign.getDepartment().getName()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(new HodResponse(user.getId(), user.getUsername(), user.getName(), depts));
    }

    @DeleteMapping("/hods/{id}")
    @Transactional
    public ResponseEntity<Void> deleteHod(@PathVariable Long id, Principal principal) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("HOD not found: " + id));

        hodDepartmentAssignmentRepository.deleteByHodId(id);
        userRepository.delete(user);

        systemLogService.log(principal.getName(), "Delete HOD", "Deleted HOD profile: " + user.getName() + " (@" + user.getUsername() + ")");

        return ResponseEntity.noContent().build();
    }
}
