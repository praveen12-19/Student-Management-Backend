package com.college.sdm.service;

import com.college.sdm.dto.*;
import com.college.sdm.entity.*;
import com.college.sdm.exception.ResourceNotFoundException;
import com.college.sdm.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private MentorRepository mentorRepository;

    @Autowired
    private HodDepartmentAssignmentRepository hodDepartmentAssignmentRepository;

    @Transactional(readOnly = true)
    public List<StudentResponseDto> getStudents(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        if (user.getRole() == Role.ROLE_ADMIN) {
            return studentRepository.findAll().stream()
                    .map(this::mapToResponseDto)
                    .collect(Collectors.toList());
        }

        if (user.getRole() == Role.ROLE_HOD) {
            List<Department> managedDepts = hodDepartmentAssignmentRepository.findByHod(user).stream()
                    .map(HodDepartmentAssignment::getDepartment)
                    .collect(Collectors.toList());
            if (managedDepts.isEmpty()) {
                return Collections.emptyList();
            }
            return studentRepository.findByDepartmentIn(managedDepts).stream()
                    .map(this::mapToResponseDto)
                    .collect(Collectors.toList());
        } else {
            Mentor mentor = mentorRepository.findByUser(user).orElse(null);
            if (mentor != null) {
                List<Student> linkedStudents = studentRepository.findByMentor(mentor);
                List<Student> unassignedStudents = getUnassignedStudentsForMentor(mentor);
                List<Student> deptStudents = mentor.getDepartment() != null ? studentRepository.findByDepartment(mentor.getDepartment()) : Collections.emptyList();

                java.util.Set<Long> uniqueIds = new java.util.HashSet<>();
                List<StudentResponseDto> combined = new java.util.ArrayList<>();

                for (Student s : linkedStudents) {
                    if (uniqueIds.add(s.getId())) {
                        combined.add(mapToResponseDto(s));
                    }
                }
                for (Student s : unassignedStudents) {
                    if (uniqueIds.add(s.getId())) {
                        combined.add(mapToResponseDto(s));
                    }
                }
                for (Student s : deptStudents) {
                    if (uniqueIds.add(s.getId())) {
                        combined.add(mapToResponseDto(s));
                    }
                }
                return combined;
            }
            return studentRepository.findAll().stream().map(this::mapToResponseDto).collect(Collectors.toList());
        }
    }

    @Transactional(readOnly = true)
    public StudentResponseDto getStudentById(Long id, String username) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));

        return mapToResponseDto(student);
    }

    @Transactional(readOnly = true)
    public StudentResponseDto getStudentById(String idOrReg, String username) {
        Student student = null;
        try {
            Long numericId = Long.parseLong(idOrReg);
            student = studentRepository.findById(numericId).orElse(null);
        } catch (NumberFormatException ignored) {}

        if (student == null) {
            student = studentRepository.findByRegisterNumber(idOrReg).orElse(null);
        }

        if (student == null) {
            String clean = idOrReg.trim().toLowerCase();
            student = studentRepository.findAll().stream()
                    .filter(s -> s.getId().toString().equalsIgnoreCase(clean) ||
                            (s.getRegisterNumber() != null && s.getRegisterNumber().trim().equalsIgnoreCase(clean)))
                    .findFirst()
                    .orElse(null);
        }

        if (student == null) {
            throw new ResourceNotFoundException("Student not found with id or register number: " + idOrReg);
        }

        return mapToResponseDto(student);
    }

    @Transactional
    public StudentResponseDto createStudent(StudentRequestDto request, String currentUsername) {
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUsername));

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + request.getDepartmentId()));

        Mentor mentor = null;
        if (user.getRole() == Role.ROLE_MENTOR) {
            mentor = mentorRepository.findByUser(user)
                    .orElseThrow(() -> new ResourceNotFoundException("Mentor profile not found for user: " + currentUsername));
            if (!department.getId().equals(mentor.getDepartment().getId())) {
                throw new AccessDeniedException("Mentor can only create students in their own department: " + mentor.getDepartment().getName());
            }
            if (mentor.getAssignedYear() != null && !mentor.getAssignedYear().equals(request.getYear())) {
                throw new AccessDeniedException("Mentor can only create students in their assigned year: " + mentor.getAssignedYear());
            }
        } else if (request.getMentorId() != null) {
            mentor = mentorRepository.findById(request.getMentorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Mentor not found with id: " + request.getMentorId()));
        }

        // Verify HOD has permission to create student in this department
        if (user.getRole() == Role.ROLE_HOD) {
            boolean managesDept = hodDepartmentAssignmentRepository.existsByHodIdAndDepartmentId(user.getId(), department.getId());
            if (!managesDept) {
                throw new AccessDeniedException("HOD does not manage this department: " + department.getName());
            }
        }

        Student student = Student.builder()
                .name(request.getName())
                .registerNumber(request.getRegisterNumber())
                .year(request.getYear())
                .department(department)
                .academicYear(request.getAcademicYear())
                .dob(request.getDob())
                .gender(request.getGender())
                .aadharNumber(request.getAadharNumber())
                .panCard(request.getPanCard())
                .linkedinUrl(request.getLinkedinUrl())
                .laptopHaving(request.getLaptopHaving() != null ? request.getLaptopHaving() : false)
                .bloodGroup(request.getBloodGroup())
                .languagesKnown(request.getLanguagesKnown())
                .community(request.getCommunity())
                .emailId(request.getEmailId())
                .studentNumber(request.getStudentNumber())
                .currentAddress(request.getCurrentAddress())
                .permanentAddress(request.getPermanentAddress())
                .studentType(request.getStudentType())
                .mentor(mentor)
                .fatherDetail(request.getFatherDetail())
                .motherDetail(request.getMotherDetail())
                .totalLeavesTaken(request.getTotalLeavesTaken() != null ? request.getTotalLeavesTaken() : 0)
                .od(request.getOd() != null ? request.getOd() : 0)
                .lateComing(request.getLateComing() != null ? request.getLateComing() : 0)
                .build();

        student = studentRepository.save(student);
        return mapToResponseDto(student);
    }

    @Transactional
    public StudentResponseDto updateStudent(Long id, StudentRequestDto request, String currentUsername) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));

        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUsername));

        verifyAccess(student, user);

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + request.getDepartmentId()));

        // If HOD updates department, verify HOD has access to new department as well
        if (user.getRole() == Role.ROLE_HOD) {
            boolean managesNewDept = hodDepartmentAssignmentRepository.existsByHodIdAndDepartmentId(user.getId(), department.getId());
            if (!managesNewDept) {
                throw new AccessDeniedException("HOD does not manage the target department: " + department.getName());
            }
        }

        Mentor mentor = null;
        if (user.getRole() == Role.ROLE_MENTOR) {
            mentor = student.getMentor();
            if (!department.getId().equals(student.getDepartment().getId())) {
                throw new AccessDeniedException("Mentor cannot change the department of a student");
            }
            if (!request.getYear().equals(student.getYear())) {
                throw new AccessDeniedException("Mentor cannot change the year of a student");
            }
        } else {
            if (request.getMentorId() != null) {
                mentor = mentorRepository.findById(request.getMentorId())
                        .orElseThrow(() -> new ResourceNotFoundException("Mentor not found with id: " + request.getMentorId()));
            } else {
                mentor = null; // allow HOD/Admin to set to null
            }
        }

        student.setName(request.getName());
        student.setRegisterNumber(request.getRegisterNumber());
        student.setYear(request.getYear());
        student.setDepartment(department);
        student.setAcademicYear(request.getAcademicYear());
        student.setDob(request.getDob());
        student.setGender(request.getGender());
        student.setAadharNumber(request.getAadharNumber());
        student.setPanCard(request.getPanCard());
        student.setLinkedinUrl(request.getLinkedinUrl());
        student.setLaptopHaving(request.getLaptopHaving() != null ? request.getLaptopHaving() : false);
        student.setBloodGroup(request.getBloodGroup());
        student.setLanguagesKnown(request.getLanguagesKnown());
        student.setCommunity(request.getCommunity());
        student.setEmailId(request.getEmailId());
        student.setStudentNumber(request.getStudentNumber());
        student.setCurrentAddress(request.getCurrentAddress());
        student.setPermanentAddress(request.getPermanentAddress());
        student.setStudentType(request.getStudentType());
        student.setMentor(mentor);
        student.setFatherDetail(request.getFatherDetail());
        student.setMotherDetail(request.getMotherDetail());
        student.setTotalLeavesTaken(request.getTotalLeavesTaken() != null ? request.getTotalLeavesTaken() : 0);
        student.setOd(request.getOd() != null ? request.getOd() : 0);
        student.setLateComing(request.getLateComing() != null ? request.getLateComing() : 0);

        student = studentRepository.save(student);
        return mapToResponseDto(student);
    }

    @Transactional
    public void deleteStudent(Long id, String currentUsername) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));

        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUsername));

        verifyAccess(student, user);

        studentRepository.delete(student);
    }

    public List<StudentResponseDto> searchStudents(String query, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        if (user.getRole() == Role.ROLE_ADMIN) {
            return studentRepository.searchStudentsGlobal(query).stream()
                    .map(this::mapToResponseDto)
                    .collect(Collectors.toList());
        }

        if (user.getRole() == Role.ROLE_HOD) {
            List<Department> managedDepts = hodDepartmentAssignmentRepository.findByHod(user).stream()
                    .map(HodDepartmentAssignment::getDepartment)
                    .collect(Collectors.toList());
            if (managedDepts.isEmpty()) {
                return Collections.emptyList();
            }
            return studentRepository.searchStudentsForHod(managedDepts, query).stream()
                    .map(this::mapToResponseDto)
                    .collect(Collectors.toList());
        } else {
            Mentor mentor = mentorRepository.findByUser(user)
                    .orElseThrow(() -> new ResourceNotFoundException("Mentor profile not found for user: " + username));
            
            List<Student> linked = studentRepository.searchStudentsForMentor(mentor, query);
            List<Student> unassigned = getUnassignedStudentsForMentor(mentor);
            
            final String q = query.toLowerCase();
            List<Student> filteredUnassigned = unassigned.stream()
                .filter(s -> (s.getName() != null && s.getName().toLowerCase().contains(q)) 
                          || (s.getRegisterNumber() != null && s.getRegisterNumber().toLowerCase().contains(q)))
                .collect(Collectors.toList());
            
            java.util.Set<Long> uniqueIds = new java.util.HashSet<>();
            List<StudentResponseDto> combined = new java.util.ArrayList<>();
            
            for (Student s : linked) {
                if (uniqueIds.add(s.getId())) {
                    combined.add(mapToResponseDto(s));
                }
            }
            for (Student s : filteredUnassigned) {
                if (uniqueIds.add(s.getId())) {
                    combined.add(mapToResponseDto(s));
                }
            }
            return combined;
        }
    }

    public void verifyAccess(Student student) {
        String username = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        verifyAccess(student, user);
    }

    public void verifyAccess(Student student, User user) {
        if (user == null || user.getRole() == Role.ROLE_ADMIN) {
            return;
        }
        if (user.getRole() == Role.ROLE_HOD) {
            if (student.getDepartment() != null) {
                boolean managesDept = hodDepartmentAssignmentRepository.existsByHodIdAndDepartmentId(user.getId(), student.getDepartment().getId());
                if (!managesDept) {
                    System.err.println("HOD access warning for student: " + student.getName());
                }
            }
        } else if (user.getRole() == Role.ROLE_MENTOR) {
            Mentor mentor = mentorRepository.findByUser(user).orElse(null);
            if (mentor != null && student.getDepartment() != null) {
                boolean sameDept = student.getDepartment().getId().equals(mentor.getDepartment().getId());
                boolean isAssignedMentor = student.getMentor() != null && student.getMentor().getId().equals(mentor.getId());
                if (!sameDept && !isAssignedMentor) {
                    System.err.println("Mentor access warning for student: " + student.getName());
                }
            }
        }
    }

    @Transactional
    public void updateStudentImage(Long studentId, String imagePath, String currentUsername) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUsername));
        verifyAccess(student, user);

        student.setImage(imagePath);
        studentRepository.save(student);
    }

    public StudentResponseDto mapToResponseDto(Student s) {
        if (s == null) return null;

        MentorResponseDto mentorDto = null;
        if (s.getMentor() != null) {
            mentorDto = MentorResponseDto.builder()
                    .id(s.getMentor().getId())
                    .username(s.getMentor().getUser().getUsername())
                    .name(s.getMentor().getUser().getName())
                    .assignedYear(s.getMentor().getAssignedYear())
                    .assignedSection(s.getMentor().getAssignedSection())
                    .build();
        }

        DepartmentDto deptDto = DepartmentDto.builder()
                .id(s.getDepartment().getId())
                .name(s.getDepartment().getName())
                .build();

        return StudentResponseDto.builder()
                .id(s.getId())
                .image(s.getImage())
                .name(s.getName())
                .registerNumber(s.getRegisterNumber())
                .year(s.getYear())
                .department(deptDto)
                .academicYear(s.getAcademicYear())
                .dob(s.getDob())
                .gender(s.getGender())
                .aadharNumber(s.getAadharNumber())
                .panCard(s.getPanCard())
                .linkedinUrl(s.getLinkedinUrl())
                .laptopHaving(s.getLaptopHaving())
                .bloodGroup(s.getBloodGroup())
                .languagesKnown(s.getLanguagesKnown())
                .community(s.getCommunity())
                .emailId(s.getEmailId())
                .studentNumber(s.getStudentNumber())
                .currentAddress(s.getCurrentAddress())
                .permanentAddress(s.getPermanentAddress())
                .studentType(s.getStudentType())
                .mentor(mentorDto)
                .fatherDetail(s.getFatherDetail())
                .motherDetail(s.getMotherDetail())
                .totalLeavesTaken(s.getTotalLeavesTaken())
                .od(s.getOd())
                .lateComing(s.getLateComing())
                .tenthDetail(s.getTenthDetail() == null ? null : TenthDetailDto.builder()
                        .id(s.getTenthDetail().getId())
                        .schoolName(s.getTenthDetail().getSchoolName())
                        .medium(s.getTenthDetail().getMedium())
                        .totalMarks(s.getTenthDetail().getTotalMarks())
                        .percentage(s.getTenthDetail().getPercentage())
                        .yearOfPassing(s.getTenthDetail().getYearOfPassing())
                        .build())
                .twelfthDetail(s.getTwelfthDetail() == null ? null : TwelfthDetailDto.builder()
                        .id(s.getTwelfthDetail().getId())
                        .schoolName(s.getTwelfthDetail().getSchoolName())
                        .medium(s.getTwelfthDetail().getMedium())
                        .group(s.getTwelfthDetail().getGroup())
                        .totalMarks(s.getTwelfthDetail().getTotalMarks())
                        .percentage(s.getTwelfthDetail().getPercentage())
                        .cutoff(s.getTwelfthDetail().getCutoff())
                        .physicsMarks(s.getTwelfthDetail().getPhysicsMarks())
                        .chemistryMarks(s.getTwelfthDetail().getChemistryMarks())
                        .mathsMarks(s.getTwelfthDetail().getMathsMarks())
                        .yearOfPassing(s.getTwelfthDetail().getYearOfPassing())
                        .build())
                .admissionDetail(s.getAdmissionDetail() == null ? null : AdmissionDetailDto.builder()
                        .id(s.getAdmissionDetail().getId())
                        .scholarships(s.getAdmissionDetail().getScholarships())
                        .build())
                .siblings(s.getSiblings() == null ? Collections.emptyList() : s.getSiblings().stream()
                        .map(sib -> SiblingDto.builder()
                                .id(sib.getId())
                                .name(sib.getName())
                                .occupation(sib.getOccupation())
                                .qualification(sib.getQualification())
                                .emailId(sib.getEmailId())
                                .mobileNumber(sib.getMobileNumber())
                                .build())
                        .collect(Collectors.toList()))
                .certificates(s.getCertificates() == null ? Collections.emptyList() : s.getCertificates().stream()
                        .map(cert -> CertificateDto.builder()
                                .id(cert.getId())
                                .type(cert.getType())
                                .filePath(cert.getFilePath())
                                .build())
                        .collect(Collectors.toList()))
                .semesterRecords(s.getSemesterRecords() == null ? Collections.emptyList() : s.getSemesterRecords().stream()
                        .map(sem -> SemesterRecordDto.builder()
                                .id(sem.getId())
                                .semesterNumber(sem.getSemesterNumber())
                                .yearNumber(sem.getYearNumber())
                                .mentorName(sem.getMentorName())
                                .firstHourTestMarks(sem.getFirstHourTestMarks())
                                .cat1(sem.getCat1())
                                .cat2(sem.getCat2())
                                .model(sem.getModel())
                                .grade(sem.getGrade())
                                .gpa(sem.getGpa())
                                .cgpaTillNow(sem.getCgpaTillNow())
                                .historyOfArrears(sem.getHistoryOfArrears())
                                .standingArrears(sem.getStandingArrears())
                                .build())
                        .collect(Collectors.toList()))
                .extraActivities(s.getExtraActivities() == null ? Collections.emptyList() : s.getExtraActivities().stream()
                        .map(act -> ExtraActivityDto.builder()
                                .id(act.getId())
                                .type(act.getType())
                                .name(act.getName())
                                .details(act.getDetails())
                                .build())
                        .collect(Collectors.toList()))
                .internships(s.getInternships() == null ? Collections.emptyList() : s.getInternships().stream()
                        .map(intern -> InternshipDto.builder()
                                .id(intern.getId())
                                .name(intern.getName())
                                .date(intern.getDate())
                                .location(intern.getLocation())
                                .domain(intern.getDomain())
                                .certificatePath(intern.getCertificatePath())
                                .build())
                        .collect(Collectors.toList()))
                .industrialVisits(s.getIndustrialVisits() == null ? Collections.emptyList() : s.getIndustrialVisits().stream()
                        .map(visit -> IndustrialVisitDto.builder()
                                .id(visit.getId())
                                .name(visit.getName())
                                .date(visit.getDate())
                                .location(visit.getLocation())
                                .build())
                        .collect(Collectors.toList()))
                .indisciplinaryActivities(s.getIndisciplinaryActivities() == null ? Collections.emptyList() : s.getIndisciplinaryActivities().stream()
                        .map(act -> IndisciplinaryActivityDto.builder()
                                .id(act.getId())
                                .description(act.getDescription())
                                .date(act.getDate())
                                .addedBy(act.getAddedBy())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    private List<Student> getUnassignedStudentsForMentor(Mentor mentor) {
        Department dept = mentor.getDepartment();
        Integer year = mentor.getAssignedYear();
        String mentorSection = mentor.getAssignedSection();
        
        boolean hasSectionField = false;
        String fieldName = "";
        try {
            Student.class.getDeclaredField("section");
            hasSectionField = true;
            fieldName = "section";
        } catch (NoSuchFieldException e) {
            try {
                Student.class.getDeclaredField("assignedSection");
                hasSectionField = true;
                fieldName = "assignedSection";
            } catch (NoSuchFieldException ex) {}
        }
        
        List<Student> unassigned = studentRepository.findUnassignedStudents(dept, year);
        if (hasSectionField && mentorSection != null && !mentorSection.trim().isEmpty()) {
            final String finalFieldName = fieldName;
            final String finalSec = mentorSection.trim();
            unassigned = unassigned.stream().filter(s -> {
                try {
                    java.lang.reflect.Field field = Student.class.getDeclaredField(finalFieldName);
                    field.setAccessible(true);
                    Object value = field.get(s);
                    return value != null && String.valueOf(value).equalsIgnoreCase(finalSec);
                } catch (Exception ex) {
                    return true;
                }
            }).collect(Collectors.toList());
        }
        return unassigned;
    }
}
