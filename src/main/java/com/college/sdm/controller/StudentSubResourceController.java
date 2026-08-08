package com.college.sdm.controller;

import com.college.sdm.dto.*;
import com.college.sdm.entity.*;
import com.college.sdm.exception.ResourceNotFoundException;
import com.college.sdm.repository.*;
import com.college.sdm.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/students/{studentId}")
@PreAuthorize("hasAnyRole('ADMIN', 'HOD', 'MENTOR')")
public class StudentSubResourceController {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentService studentService;

    @Autowired
    private TenthDetailRepository tenthDetailRepository;

    @Autowired
    private TwelfthDetailRepository twelfthDetailRepository;

    @Autowired
    private AdmissionDetailRepository admissionDetailRepository;

    @Autowired
    private SiblingRepository siblingRepository;

    @Autowired
    private CertificateRepository certificateRepository;

    @Autowired
    private SemesterRecordRepository semesterRecordRepository;

    @Autowired
    private ExtraActivityRepository extraActivityRepository;

    @Autowired
    private InternshipRepository internshipRepository;

    @Autowired
    private IndustrialVisitRepository industrialVisitRepository;

    @Autowired
    private IndisciplinaryActivityRepository indisciplinaryActivityRepository;

    // Helper to get and verify student access
    private Student getVerifiedStudent(Long studentId, Principal principal) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + principal.getName()));
        studentService.verifyAccess(student, user);
        return student;
    }

    // ==========================================
    // Tenth Detail Endpoints
    // ==========================================

    @GetMapping("/tenth-detail")
    public ResponseEntity<TenthDetailDto> getTenthDetail(@PathVariable Long studentId, Principal principal) {
        Student student = getVerifiedStudent(studentId, principal);
        TenthDetail detail = student.getTenthDetail();
        if (detail == null) {
            throw new ResourceNotFoundException("Tenth details not found for student: " + studentId);
        }
        return ResponseEntity.ok(mapTenthToDto(detail));
    }

    @PostMapping("/tenth-detail")
    public ResponseEntity<TenthDetailDto> saveTenthDetail(
            @PathVariable Long studentId,
            @Valid @RequestBody TenthDetailDto dto,
            Principal principal) {
        Student student = getVerifiedStudent(studentId, principal);
        TenthDetail detail = student.getTenthDetail();
        if (detail == null) {
            detail = new TenthDetail();
            detail.setStudent(student);
        }
        detail.setSchoolName(dto.getSchoolName());
        detail.setMedium(dto.getMedium());
        detail.setTotalMarks(dto.getTotalMarks());
        detail.setPercentage(dto.getPercentage());
        detail.setYearOfPassing(dto.getYearOfPassing());
        detail = tenthDetailRepository.save(detail);
        return ResponseEntity.ok(mapTenthToDto(detail));
    }

    @PutMapping("/tenth-detail")
    public ResponseEntity<TenthDetailDto> updateTenthDetail(
            @PathVariable Long studentId,
            @Valid @RequestBody TenthDetailDto dto,
            Principal principal) {
        return saveTenthDetail(studentId, dto, principal);
    }

    @DeleteMapping("/tenth-detail")
    public ResponseEntity<Void> deleteTenthDetail(@PathVariable Long studentId, Principal principal) {
        Student student = getVerifiedStudent(studentId, principal);
        TenthDetail detail = student.getTenthDetail();
        if (detail != null) {
            tenthDetailRepository.delete(detail);
        }
        return ResponseEntity.noContent().build();
    }

    private TenthDetailDto mapTenthToDto(TenthDetail d) {
        return TenthDetailDto.builder()
                .id(d.getId())
                .schoolName(d.getSchoolName())
                .medium(d.getMedium())
                .totalMarks(d.getTotalMarks())
                .percentage(d.getPercentage())
                .yearOfPassing(d.getYearOfPassing())
                .build();
    }

    // ==========================================
    // Twelfth Detail Endpoints
    // ==========================================

    @GetMapping("/twelfth-detail")
    public ResponseEntity<TwelfthDetailDto> getTwelfthDetail(@PathVariable Long studentId, Principal principal) {
        Student student = getVerifiedStudent(studentId, principal);
        TwelfthDetail detail = student.getTwelfthDetail();
        if (detail == null) {
            throw new ResourceNotFoundException("Twelfth details not found for student: " + studentId);
        }
        return ResponseEntity.ok(mapTwelfthToDto(detail));
    }

    @PostMapping("/twelfth-detail")
    public ResponseEntity<TwelfthDetailDto> saveTwelfthDetail(
            @PathVariable Long studentId,
            @Valid @RequestBody TwelfthDetailDto dto,
            Principal principal) {
        Student student = getVerifiedStudent(studentId, principal);
        TwelfthDetail detail = student.getTwelfthDetail();
        if (detail == null) {
            detail = new TwelfthDetail();
            detail.setStudent(student);
        }
        detail.setSchoolName(dto.getSchoolName());
        detail.setMedium(dto.getMedium());
        detail.setGroup(dto.getGroup());
        detail.setTotalMarks(dto.getTotalMarks());
        detail.setPercentage(dto.getPercentage());
        detail.setCutoff(dto.getCutoff());
        detail.setPhysicsMarks(dto.getPhysicsMarks());
        detail.setChemistryMarks(dto.getChemistryMarks());
        detail.setMathsMarks(dto.getMathsMarks());
        detail.setYearOfPassing(dto.getYearOfPassing());
        detail = twelfthDetailRepository.save(detail);
        return ResponseEntity.ok(mapTwelfthToDto(detail));
    }

    @PutMapping("/twelfth-detail")
    public ResponseEntity<TwelfthDetailDto> updateTwelfthDetail(
            @PathVariable Long studentId,
            @Valid @RequestBody TwelfthDetailDto dto,
            Principal principal) {
        return saveTwelfthDetail(studentId, dto, principal);
    }

    @DeleteMapping("/twelfth-detail")
    public ResponseEntity<Void> deleteTwelfthDetail(@PathVariable Long studentId, Principal principal) {
        Student student = getVerifiedStudent(studentId, principal);
        TwelfthDetail detail = student.getTwelfthDetail();
        if (detail != null) {
            twelfthDetailRepository.delete(detail);
        }
        return ResponseEntity.noContent().build();
    }

    private TwelfthDetailDto mapTwelfthToDto(TwelfthDetail d) {
        return TwelfthDetailDto.builder()
                .id(d.getId())
                .schoolName(d.getSchoolName())
                .medium(d.getMedium())
                .group(d.getGroup())
                .totalMarks(d.getTotalMarks())
                .percentage(d.getPercentage())
                .cutoff(d.getCutoff())
                .physicsMarks(d.getPhysicsMarks())
                .chemistryMarks(d.getChemistryMarks())
                .mathsMarks(d.getMathsMarks())
                .yearOfPassing(d.getYearOfPassing())
                .build();
    }

    // ==========================================
    // Admission Detail Endpoints
    // ==========================================

    @GetMapping("/admission-detail")
    public ResponseEntity<AdmissionDetailDto> getAdmissionDetail(@PathVariable Long studentId, Principal principal) {
        Student student = getVerifiedStudent(studentId, principal);
        AdmissionDetail detail = student.getAdmissionDetail();
        if (detail == null) {
            throw new ResourceNotFoundException("Admission details not found for student: " + studentId);
        }
        return ResponseEntity.ok(mapAdmissionToDto(detail));
    }

    @PostMapping("/admission-detail")
    public ResponseEntity<AdmissionDetailDto> saveAdmissionDetail(
            @PathVariable Long studentId,
            @Valid @RequestBody AdmissionDetailDto dto,
            Principal principal) {
        Student student = getVerifiedStudent(studentId, principal);
        AdmissionDetail detail = student.getAdmissionDetail();
        if (detail == null) {
            detail = new AdmissionDetail();
            detail.setStudent(student);
        }
        detail.setScholarships(dto.getScholarships());
        detail = admissionDetailRepository.save(detail);
        return ResponseEntity.ok(mapAdmissionToDto(detail));
    }

    @PutMapping("/admission-detail")
    public ResponseEntity<AdmissionDetailDto> updateAdmissionDetail(
            @PathVariable Long studentId,
            @Valid @RequestBody AdmissionDetailDto dto,
            Principal principal) {
        return saveAdmissionDetail(studentId, dto, principal);
    }

    @DeleteMapping("/admission-detail")
    public ResponseEntity<Void> deleteAdmissionDetail(@PathVariable Long studentId, Principal principal) {
        Student student = getVerifiedStudent(studentId, principal);
        AdmissionDetail detail = student.getAdmissionDetail();
        if (detail != null) {
            admissionDetailRepository.delete(detail);
        }
        return ResponseEntity.noContent().build();
    }

    private AdmissionDetailDto mapAdmissionToDto(AdmissionDetail d) {
        return AdmissionDetailDto.builder()
                .id(d.getId())
                .scholarships(d.getScholarships())
                .build();
    }

    // ==========================================
    // Siblings Endpoints
    // ==========================================

    @GetMapping("/siblings")
    public ResponseEntity<List<SiblingDto>> getSiblings(@PathVariable Long studentId, Principal principal) {
        Student student = getVerifiedStudent(studentId, principal);
        List<SiblingDto> list = student.getSiblings().stream()
                .map(this::mapSiblingToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @PostMapping("/siblings")
    public ResponseEntity<SiblingDto> addSibling(
            @PathVariable Long studentId,
            @Valid @RequestBody SiblingDto dto,
            Principal principal) {
        Student student = getVerifiedStudent(studentId, principal);
        Sibling sibling = Sibling.builder()
                .student(student)
                .name(dto.getName())
                .occupation(dto.getOccupation())
                .qualification(dto.getQualification())
                .emailId(dto.getEmailId())
                .mobileNumber(dto.getMobileNumber())
                .build();
        sibling = siblingRepository.save(sibling);
        return ResponseEntity.ok(mapSiblingToDto(sibling));
    }

    @PutMapping("/siblings/{siblingId}")
    public ResponseEntity<SiblingDto> updateSibling(
            @PathVariable Long studentId,
            @PathVariable Long siblingId,
            @Valid @RequestBody SiblingDto dto,
            Principal principal) {
        getVerifiedStudent(studentId, principal);
        Sibling sibling = siblingRepository.findById(siblingId)
                .orElseThrow(() -> new ResourceNotFoundException("Sibling not found with id: " + siblingId));
        
        if (!sibling.getStudent().getId().equals(studentId)) {
            throw new IllegalArgumentException("Sibling does not belong to this student");
        }

        sibling.setName(dto.getName());
        sibling.setOccupation(dto.getOccupation());
        sibling.setQualification(dto.getQualification());
        sibling.setEmailId(dto.getEmailId());
        sibling.setMobileNumber(dto.getMobileNumber());
        sibling = siblingRepository.save(sibling);
        return ResponseEntity.ok(mapSiblingToDto(sibling));
    }

    @DeleteMapping("/siblings/{siblingId}")
    public ResponseEntity<Void> deleteSibling(
            @PathVariable Long studentId,
            @PathVariable Long siblingId,
            Principal principal) {
        getVerifiedStudent(studentId, principal);
        Sibling sibling = siblingRepository.findById(siblingId)
                .orElseThrow(() -> new ResourceNotFoundException("Sibling not found with id: " + siblingId));
        
        if (!sibling.getStudent().getId().equals(studentId)) {
            throw new IllegalArgumentException("Sibling does not belong to this student");
        }

        siblingRepository.delete(sibling);
        return ResponseEntity.noContent().build();
    }

    private SiblingDto mapSiblingToDto(Sibling s) {
        return SiblingDto.builder()
                .id(s.getId())
                .name(s.getName())
                .occupation(s.getOccupation())
                .qualification(s.getQualification())
                .emailId(s.getEmailId())
                .mobileNumber(s.getMobileNumber())
                .build();
    }

    // ==========================================
    // Certificates Endpoints
    // ==========================================

    @GetMapping("/certificates")
    public ResponseEntity<List<CertificateDto>> getCertificates(@PathVariable Long studentId, Principal principal) {
        Student student = getVerifiedStudent(studentId, principal);
        List<CertificateDto> list = student.getCertificates().stream()
                .map(this::mapCertificateToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @PostMapping("/certificates")
    public ResponseEntity<CertificateDto> addCertificate(
            @PathVariable Long studentId,
            @Valid @RequestBody CertificateDto dto,
            Principal principal) {
        Student student = getVerifiedStudent(studentId, principal);
        Certificate cert = Certificate.builder()
                .student(student)
                .type(dto.getType())
                .filePath(dto.getFilePath())
                .build();
        cert = certificateRepository.save(cert);
        return ResponseEntity.ok(mapCertificateToDto(cert));
    }

    @PutMapping("/certificates/{certId}")
    public ResponseEntity<CertificateDto> updateCertificate(
            @PathVariable Long studentId,
            @PathVariable Long certId,
            @Valid @RequestBody CertificateDto dto,
            Principal principal) {
        getVerifiedStudent(studentId, principal);
        Certificate certificate = certificateRepository.findById(certId)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found with id: " + certId));
        
        if (!certificate.getStudent().getId().equals(studentId)) {
            throw new IllegalArgumentException("Certificate does not belong to this student");
        }

        certificate.setType(dto.getType());
        certificate.setFilePath(dto.getFilePath());
        certificate = certificateRepository.save(certificate);
        return ResponseEntity.ok(mapCertificateToDto(certificate));
    }

    @DeleteMapping("/certificates/{certId}")
    public ResponseEntity<Void> deleteCertificate(
            @PathVariable Long studentId,
            @PathVariable Long certId,
            Principal principal) {
        getVerifiedStudent(studentId, principal);
        Certificate certificate = certificateRepository.findById(certId)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found with id: " + certId));
        
        if (!certificate.getStudent().getId().equals(studentId)) {
            throw new IllegalArgumentException("Certificate does not belong to this student");
        }

        certificateRepository.delete(certificate);
        return ResponseEntity.noContent().build();
    }

    private CertificateDto mapCertificateToDto(Certificate c) {
        return CertificateDto.builder()
                .id(c.getId())
                .type(c.getType())
                .filePath(c.getFilePath())
                .build();
    }

    // ==========================================
    // Semester Records Endpoints
    // ==========================================

    @GetMapping("/semester-records")
    public ResponseEntity<List<SemesterRecordDto>> getSemesterRecords(@PathVariable Long studentId, Principal principal) {
        Student student = getVerifiedStudent(studentId, principal);
        List<SemesterRecordDto> list = student.getSemesterRecords().stream()
                .map(this::mapSemesterToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @PostMapping("/semester-records")
    public ResponseEntity<SemesterRecordDto> addSemesterRecord(
            @PathVariable Long studentId,
            @Valid @RequestBody SemesterRecordDto dto,
            Principal principal) {
        Student student = getVerifiedStudent(studentId, principal);
        SemesterRecord record = SemesterRecord.builder()
                .student(student)
                .semesterNumber(dto.getSemesterNumber())
                .yearNumber(dto.getYearNumber())
                .mentorName(dto.getMentorName())
                .firstHourTestMarks(dto.getFirstHourTestMarks())
                .cat1(dto.getCat1())
                .cat2(dto.getCat2())
                .model(dto.getModel())
                .grade(dto.getGrade())
                .gpa(dto.getGpa())
                .cgpaTillNow(dto.getCgpaTillNow())
                .historyOfArrears(dto.getHistoryOfArrears())
                .standingArrears(dto.getStandingArrears())
                .build();
        record = semesterRecordRepository.save(record);
        return ResponseEntity.ok(mapSemesterToDto(record));
    }

    @PutMapping("/semester-records/{recordId}")
    public ResponseEntity<SemesterRecordDto> updateSemesterRecord(
            @PathVariable Long studentId,
            @PathVariable Long recordId,
            @Valid @RequestBody SemesterRecordDto dto,
            Principal principal) {
        getVerifiedStudent(studentId, principal);
        SemesterRecord record = semesterRecordRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("SemesterRecord not found with id: " + recordId));

        if (!record.getStudent().getId().equals(studentId)) {
            throw new IllegalArgumentException("SemesterRecord does not belong to this student");
        }

        record.setSemesterNumber(dto.getSemesterNumber());
        record.setYearNumber(dto.getYearNumber());
        record.setMentorName(dto.getMentorName());
        record.setFirstHourTestMarks(dto.getFirstHourTestMarks());
        record.setCat1(dto.getCat1());
        record.setCat2(dto.getCat2());
        record.setModel(dto.getModel());
        record.setGrade(dto.getGrade());
        record.setGpa(dto.getGpa());
        record.setCgpaTillNow(dto.getCgpaTillNow());
        record.setHistoryOfArrears(dto.getHistoryOfArrears());
        record.setStandingArrears(dto.getStandingArrears());
        record = semesterRecordRepository.save(record);
        return ResponseEntity.ok(mapSemesterToDto(record));
    }

    @DeleteMapping("/semester-records/{recordId}")
    public ResponseEntity<Void> deleteSemesterRecord(
            @PathVariable Long studentId,
            @PathVariable Long recordId,
            Principal principal) {
        getVerifiedStudent(studentId, principal);
        SemesterRecord record = semesterRecordRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("SemesterRecord not found with id: " + recordId));

        if (!record.getStudent().getId().equals(studentId)) {
            throw new IllegalArgumentException("SemesterRecord does not belong to this student");
        }

        semesterRecordRepository.delete(record);
        return ResponseEntity.noContent().build();
    }

    private SemesterRecordDto mapSemesterToDto(SemesterRecord r) {
        return SemesterRecordDto.builder()
                .id(r.getId())
                .semesterNumber(r.getSemesterNumber())
                .yearNumber(r.getYearNumber())
                .mentorName(r.getMentorName())
                .firstHourTestMarks(r.getFirstHourTestMarks())
                .cat1(r.getCat1())
                .cat2(r.getCat2())
                .model(r.getModel())
                .grade(r.getGrade())
                .gpa(r.getGpa())
                .cgpaTillNow(r.getCgpaTillNow())
                .historyOfArrears(r.getHistoryOfArrears())
                .standingArrears(r.getStandingArrears())
                .build();
    }

    // ==========================================
    // Extra Activities Endpoints
    // ==========================================

    @GetMapping("/extra-activities")
    public ResponseEntity<List<ExtraActivityDto>> getExtraActivities(@PathVariable Long studentId, Principal principal) {
        Student student = getVerifiedStudent(studentId, principal);
        List<ExtraActivityDto> list = student.getExtraActivities().stream()
                .map(this::mapExtraToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @PostMapping("/extra-activities")
    public ResponseEntity<ExtraActivityDto> addExtraActivity(
            @PathVariable Long studentId,
            @Valid @RequestBody ExtraActivityDto dto,
            Principal principal) {
        Student student = getVerifiedStudent(studentId, principal);
        ExtraActivity activity = ExtraActivity.builder()
                .student(student)
                .type(dto.getType())
                .name(dto.getName())
                .details(dto.getDetails())
                .build();
        activity = extraActivityRepository.save(activity);
        return ResponseEntity.ok(mapExtraToDto(activity));
    }

    @PutMapping("/extra-activities/{activityId}")
    public ResponseEntity<ExtraActivityDto> updateExtraActivity(
            @PathVariable Long studentId,
            @PathVariable Long activityId,
            @Valid @RequestBody ExtraActivityDto dto,
            Principal principal) {
        getVerifiedStudent(studentId, principal);
        ExtraActivity activity = extraActivityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("ExtraActivity not found with id: " + activityId));

        if (!activity.getStudent().getId().equals(studentId)) {
            throw new IllegalArgumentException("ExtraActivity does not belong to this student");
        }

        activity.setType(dto.getType());
        activity.setName(dto.getName());
        activity.setDetails(dto.getDetails());
        activity = extraActivityRepository.save(activity);
        return ResponseEntity.ok(mapExtraToDto(activity));
    }

    @DeleteMapping("/extra-activities/{activityId}")
    public ResponseEntity<Void> deleteExtraActivity(
            @PathVariable Long studentId,
            @PathVariable Long activityId,
            Principal principal) {
        getVerifiedStudent(studentId, principal);
        ExtraActivity activity = extraActivityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("ExtraActivity not found with id: " + activityId));

        if (!activity.getStudent().getId().equals(studentId)) {
            throw new IllegalArgumentException("ExtraActivity does not belong to this student");
        }

        extraActivityRepository.delete(activity);
        return ResponseEntity.noContent().build();
    }

    private ExtraActivityDto mapExtraToDto(ExtraActivity e) {
        return ExtraActivityDto.builder()
                .id(e.getId())
                .type(e.getType())
                .name(e.getName())
                .details(e.getDetails())
                .build();
    }

    // ==========================================
    // Internships Endpoints
    // ==========================================

    @GetMapping("/internships")
    public ResponseEntity<List<InternshipDto>> getInternships(@PathVariable Long studentId, Principal principal) {
        Student student = getVerifiedStudent(studentId, principal);
        List<InternshipDto> list = student.getInternships().stream()
                .map(this::mapInternshipToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @PostMapping("/internships")
    public ResponseEntity<InternshipDto> addInternship(
            @PathVariable Long studentId,
            @Valid @RequestBody InternshipDto dto,
            Principal principal) {
        Student student = getVerifiedStudent(studentId, principal);
        Internship internship = Internship.builder()
                .student(student)
                .name(dto.getName())
                .date(dto.getDate())
                .location(dto.getLocation())
                .domain(dto.getDomain())
                .certificatePath(dto.getCertificatePath())
                .build();
        internship = internshipRepository.save(internship);
        return ResponseEntity.ok(mapInternshipToDto(internship));
    }

    @PutMapping("/internships/{internId}")
    public ResponseEntity<InternshipDto> updateInternship(
            @PathVariable Long studentId,
            @PathVariable Long internId,
            @Valid @RequestBody InternshipDto dto,
            Principal principal) {
        getVerifiedStudent(studentId, principal);
        Internship internship = internshipRepository.findById(internId)
                .orElseThrow(() -> new ResourceNotFoundException("Internship not found with id: " + internId));

        if (!internship.getStudent().getId().equals(studentId)) {
            throw new IllegalArgumentException("Internship does not belong to this student");
        }

        internship.setName(dto.getName());
        internship.setDate(dto.getDate());
        internship.setLocation(dto.getLocation());
        internship.setDomain(dto.getDomain());
        internship.setCertificatePath(dto.getCertificatePath());
        internship = internshipRepository.save(internship);
        return ResponseEntity.ok(mapInternshipToDto(internship));
    }

    @DeleteMapping("/internships/{internId}")
    public ResponseEntity<Void> deleteInternship(
            @PathVariable Long studentId,
            @PathVariable Long internId,
            Principal principal) {
        getVerifiedStudent(studentId, principal);
        Internship internship = internshipRepository.findById(internId)
                .orElseThrow(() -> new ResourceNotFoundException("Internship not found with id: " + internId));

        if (!internship.getStudent().getId().equals(studentId)) {
            throw new IllegalArgumentException("Internship does not belong to this student");
        }

        internshipRepository.delete(internship);
        return ResponseEntity.noContent().build();
    }

    private InternshipDto mapInternshipToDto(Internship i) {
        return InternshipDto.builder()
                .id(i.getId())
                .name(i.getName())
                .date(i.getDate())
                .location(i.getLocation())
                .domain(i.getDomain())
                .certificatePath(i.getCertificatePath())
                .build();
    }

    // ==========================================
    // Industrial Visits Endpoints
    // ==========================================

    @GetMapping("/industrial-visits")
    public ResponseEntity<List<IndustrialVisitDto>> getIndustrialVisits(@PathVariable Long studentId, Principal principal) {
        Student student = getVerifiedStudent(studentId, principal);
        List<IndustrialVisitDto> list = student.getIndustrialVisits().stream()
                .map(this::mapVisitToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @PostMapping("/industrial-visits")
    public ResponseEntity<IndustrialVisitDto> addIndustrialVisit(
            @PathVariable Long studentId,
            @Valid @RequestBody IndustrialVisitDto dto,
            Principal principal) {
        Student student = getVerifiedStudent(studentId, principal);
        IndustrialVisit visit = IndustrialVisit.builder()
                .student(student)
                .name(dto.getName())
                .date(dto.getDate())
                .location(dto.getLocation())
                .build();
        visit = industrialVisitRepository.save(visit);
        return ResponseEntity.ok(mapVisitToDto(visit));
    }

    @PutMapping("/industrial-visits/{visitId}")
    public ResponseEntity<IndustrialVisitDto> updateIndustrialVisit(
            @PathVariable Long studentId,
            @PathVariable Long visitId,
            @Valid @RequestBody IndustrialVisitDto dto,
            Principal principal) {
        getVerifiedStudent(studentId, principal);
        IndustrialVisit visit = industrialVisitRepository.findById(visitId)
                .orElseThrow(() -> new ResourceNotFoundException("IndustrialVisit not found with id: " + visitId));

        if (!visit.getStudent().getId().equals(studentId)) {
            throw new IllegalArgumentException("IndustrialVisit does not belong to this student");
        }

        visit.setName(dto.getName());
        visit.setDate(dto.getDate());
        visit.setLocation(dto.getLocation());
        visit = industrialVisitRepository.save(visit);
        return ResponseEntity.ok(mapVisitToDto(visit));
    }

    @DeleteMapping("/industrial-visits/{visitId}")
    public ResponseEntity<Void> deleteIndustrialVisit(
            @PathVariable Long studentId,
            @PathVariable Long visitId,
            Principal principal) {
        getVerifiedStudent(studentId, principal);
        IndustrialVisit visit = industrialVisitRepository.findById(visitId)
                .orElseThrow(() -> new ResourceNotFoundException("IndustrialVisit not found with id: " + visitId));

        if (!visit.getStudent().getId().equals(studentId)) {
            throw new IllegalArgumentException("IndustrialVisit does not belong to this student");
        }

        industrialVisitRepository.delete(visit);
        return ResponseEntity.noContent().build();
    }

    private IndustrialVisitDto mapVisitToDto(IndustrialVisit v) {
        return IndustrialVisitDto.builder()
                .id(v.getId())
                .name(v.getName())
                .date(v.getDate())
                .location(v.getLocation())
                .build();
    }

    // ==========================================
    // Indisciplinary Activities Endpoints
    // ==========================================

    @GetMapping("/indisciplinary-activities")
    public ResponseEntity<List<IndisciplinaryActivityDto>> getIndisciplinaryActivities(@PathVariable Long studentId, Principal principal) {
        Student student = getVerifiedStudent(studentId, principal);
        List<IndisciplinaryActivityDto> list = student.getIndisciplinaryActivities().stream()
                .map(this::mapIndisciplinaryToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @PostMapping("/indisciplinary-activities")
    public ResponseEntity<IndisciplinaryActivityDto> addIndisciplinaryActivity(
            @PathVariable Long studentId,
            @Valid @RequestBody IndisciplinaryActivityDto dto,
            Principal principal) {
        Student student = getVerifiedStudent(studentId, principal);
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + principal.getName()));

        String addedByString = user.getUsername() + " (" + user.getRole().name().replace("ROLE_", "") + ")";

        IndisciplinaryActivity activity = IndisciplinaryActivity.builder()
                .student(student)
                .description(dto.getDescription())
                .date(dto.getDate())
                .addedBy(addedByString)
                .build();
        activity = indisciplinaryActivityRepository.save(activity);
        return ResponseEntity.ok(mapIndisciplinaryToDto(activity));
    }

    @PutMapping("/indisciplinary-activities/{activityId}")
    public ResponseEntity<IndisciplinaryActivityDto> updateIndisciplinaryActivity(
            @PathVariable Long studentId,
            @PathVariable Long activityId,
            @Valid @RequestBody IndisciplinaryActivityDto dto,
            Principal principal) {
        getVerifiedStudent(studentId, principal);
        IndisciplinaryActivity activity = indisciplinaryActivityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("IndisciplinaryActivity not found with id: " + activityId));

        if (!activity.getStudent().getId().equals(studentId)) {
            throw new IllegalArgumentException("IndisciplinaryActivity does not belong to this student");
        }

        activity.setDescription(dto.getDescription());
        activity.setDate(dto.getDate());
        // do not update addedBy to retain original creator info
        activity = indisciplinaryActivityRepository.save(activity);
        return ResponseEntity.ok(mapIndisciplinaryToDto(activity));
    }

    @DeleteMapping("/indisciplinary-activities/{activityId}")
    public ResponseEntity<Void> deleteIndisciplinaryActivity(
            @PathVariable Long studentId,
            @PathVariable Long activityId,
            Principal principal) {
        getVerifiedStudent(studentId, principal);
        IndisciplinaryActivity activity = indisciplinaryActivityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("IndisciplinaryActivity not found with id: " + activityId));

        if (!activity.getStudent().getId().equals(studentId)) {
            throw new IllegalArgumentException("IndisciplinaryActivity does not belong to this student");
        }

        indisciplinaryActivityRepository.delete(activity);
        return ResponseEntity.noContent().build();
    }

    private IndisciplinaryActivityDto mapIndisciplinaryToDto(IndisciplinaryActivity a) {
        return IndisciplinaryActivityDto.builder()
                .id(a.getId())
                .description(a.getDescription())
                .date(a.getDate())
                .addedBy(a.getAddedBy())
                .build();
    }
}
