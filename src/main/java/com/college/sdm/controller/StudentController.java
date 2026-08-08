package com.college.sdm.controller;

import com.college.sdm.dto.CertificateDto;
import com.college.sdm.dto.StudentRequestDto;
import com.college.sdm.dto.StudentResponseDto;
import com.college.sdm.entity.Certificate;
import com.college.sdm.entity.Student;
import com.college.sdm.entity.User;
import com.college.sdm.exception.ResourceNotFoundException;
import com.college.sdm.repository.CertificateRepository;
import com.college.sdm.repository.StudentRepository;
import com.college.sdm.repository.UserRepository;
import com.college.sdm.service.FileStorageService;
import com.college.sdm.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/students")
@PreAuthorize("hasAnyRole('ADMIN', 'HOD', 'MENTOR')")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CertificateRepository certificateRepository;

    @Autowired
    private com.college.sdm.service.SystemLogService systemLogService;

    @GetMapping
    public ResponseEntity<List<StudentResponseDto>> getStudents(Principal principal) {
        List<StudentResponseDto> students = studentService.getStudents(principal.getName());
        return ResponseEntity.ok(students);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentResponseDto> getStudentById(@PathVariable String id, Principal principal) {
        StudentResponseDto student = studentService.getStudentById(id, principal.getName());
        return ResponseEntity.ok(student);
    }

    @PostMapping
    public ResponseEntity<StudentResponseDto> createStudent(@Valid @RequestBody StudentRequestDto request, Principal principal) {
        StudentResponseDto created = studentService.createStudent(request, principal.getName());
        systemLogService.log(principal.getName(), "Create Student", "Created student: " + created.getName() + " (Reg: " + created.getRegisterNumber() + ")");
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentResponseDto> updateStudent(
            @PathVariable Long id,
            @Valid @RequestBody StudentRequestDto request,
            Principal principal) {
        StudentResponseDto updated = studentService.updateStudent(id, request, principal.getName());
        systemLogService.log(principal.getName(), "Update Student", "Updated student details: " + updated.getName() + " (Reg: " + updated.getRegisterNumber() + ")");
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id, Principal principal) {
        studentService.deleteStudent(id, principal.getName());
        systemLogService.log(principal.getName(), "Delete Student", "Removed student profile ID: " + id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<StudentResponseDto>> searchStudents(@RequestParam String query, Principal principal) {
        List<StudentResponseDto> results = studentService.searchStudents(query, principal.getName());
        return ResponseEntity.ok(results);
    }

    @PostMapping("/{id}/upload-image")
    public ResponseEntity<StudentResponseDto> uploadImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            Principal principal) {
        
        String imagePath = fileStorageService.storeFile(file, "images");
        studentService.updateStudentImage(id, imagePath, principal.getName());
        StudentResponseDto updated = studentService.getStudentById(id, principal.getName());
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/upload-certificate")
    public ResponseEntity<CertificateDto> uploadCertificate(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String type,
            Principal principal) {

        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));

        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + principal.getName()));

        studentService.verifyAccess(student, user);

        String filePath = fileStorageService.storeFile(file, "certificates");

        Certificate certificate = Certificate.builder()
                .student(student)
                .type(type)
                .filePath(filePath)
                .build();
        certificate = certificateRepository.save(certificate);

        CertificateDto dto = CertificateDto.builder()
                .id(certificate.getId())
                .type(certificate.getType())
                .filePath(certificate.getFilePath())
                .build();

        return ResponseEntity.ok(dto);
    }
}
