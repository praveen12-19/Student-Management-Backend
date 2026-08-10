package com.college.sdm.service;

import com.college.sdm.dto.*;
import com.college.sdm.entity.*;
import com.college.sdm.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class StudentSubResourceService {

    @Autowired
    private StudentRepository studentRepository;

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

    private Student getAndVerifyStudent(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + studentId));
        studentService.verifyAccess(student);
        return student;
    }

    // --- Tenth Details ---
    public TenthDetailDto getTenthDetail(Long studentId) {
        getAndVerifyStudent(studentId);
        return tenthDetailRepository.findByStudentId(studentId)
                .map(this::mapToDto)
                .orElse(null);
    }

    public TenthDetailDto saveTenthDetail(Long studentId, TenthDetailDto dto) {
        Student student = getAndVerifyStudent(studentId);
        TenthDetail details = tenthDetailRepository.findByStudentId(studentId)
                .orElseGet(() -> TenthDetail.builder().student(student).build());

        details.setSchoolName(dto.getSchoolName());
        details.setMedium(dto.getMedium());
        details.setTotalMarks(dto.getTotalMarks());
        details.setPercentage(dto.getPercentage());
        details.setYearOfPassing(dto.getYearOfPassing());

        return mapToDto(tenthDetailRepository.save(details));
    }

    // --- Twelfth Details ---
    public TwelfthDetailDto getTwelfthDetail(Long studentId) {
        getAndVerifyStudent(studentId);
        return twelfthDetailRepository.findByStudentId(studentId)
                .map(this::mapToDto)
                .orElse(null);
    }

    public TwelfthDetailDto saveTwelfthDetail(Long studentId, TwelfthDetailDto dto) {
        Student student = getAndVerifyStudent(studentId);
        TwelfthDetail details = twelfthDetailRepository.findByStudentId(studentId)
                .orElseGet(() -> TwelfthDetail.builder().student(student).build());

        details.setSchoolName(dto.getSchoolName());
        details.setMedium(dto.getMedium());
        details.setGroup(dto.getGroup());
        details.setTotalMarks(dto.getTotalMarks());
        details.setPercentage(dto.getPercentage());
        details.setCutoff(dto.getCutoff());
        details.setPhysicsMarks(dto.getPhysicsMarks());
        details.setChemistryMarks(dto.getChemistryMarks());
        details.setMathsMarks(dto.getMathsMarks());
        details.setYearOfPassing(dto.getYearOfPassing());

        return mapToDto(twelfthDetailRepository.save(details));
    }

    // --- Admission Details ---
    public AdmissionDetailDto getAdmissionDetail(Long studentId) {
        getAndVerifyStudent(studentId);
        return admissionDetailRepository.findByStudentId(studentId)
                .map(this::mapToDto)
                .orElse(null);
    }

    public AdmissionDetailDto saveAdmissionDetail(Long studentId, AdmissionDetailDto dto) {
        Student student = getAndVerifyStudent(studentId);
        AdmissionDetail details = admissionDetailRepository.findByStudentId(studentId)
                .orElseGet(() -> AdmissionDetail.builder().student(student).build());

        details.setScholarships(dto.getScholarships());

        return mapToDto(admissionDetailRepository.save(details));
    }

    // --- Sibling Details ---
    public List<SiblingDto> getSiblings(Long studentId) {
        getAndVerifyStudent(studentId);
        return siblingRepository.findByStudentId(studentId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public SiblingDto addSibling(Long studentId, SiblingDto dto) {
        Student student = getAndVerifyStudent(studentId);
        Sibling sibling = Sibling.builder()
                .student(student)
                .name(dto.getName())
                .age(dto.getAge())
                .occupation(dto.getOccupation())
                .qualification(dto.getQualification())
                .emailId(dto.getEmailId())
                .mobileNumber(dto.getMobileNumber())
                .build();
        return mapToDto(siblingRepository.save(sibling));
    }

    public SiblingDto updateSibling(Long id, SiblingDto dto) {
        Sibling sibling = siblingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sibling record not found: " + id));
        studentService.verifyAccess(sibling.getStudent());

        sibling.setName(dto.getName());
        sibling.setAge(dto.getAge());
        sibling.setOccupation(dto.getOccupation());
        sibling.setQualification(dto.getQualification());
        sibling.setEmailId(dto.getEmailId());
        sibling.setMobileNumber(dto.getMobileNumber());

        return mapToDto(siblingRepository.save(sibling));
    }

    public void deleteSibling(Long id) {
        Sibling sibling = siblingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sibling record not found: " + id));
        studentService.verifyAccess(sibling.getStudent());
        siblingRepository.delete(sibling);
    }

    // --- Certificates ---
    public List<CertificateDto> getCertificates(Long studentId) {
        getAndVerifyStudent(studentId);
        return certificateRepository.findByStudentId(studentId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public CertificateDto addCertificate(Long studentId, String type, String filePath) {
        Student student = getAndVerifyStudent(studentId);
        Certificate cert = Certificate.builder()
                .student(student)
                .type(type)
                .filePath(filePath)
                .build();
        return mapToDto(certificateRepository.save(cert));
    }

    public void deleteCertificate(Long id) {
        Certificate cert = certificateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Certificate record not found: " + id));
        studentService.verifyAccess(cert.getStudent());
        certificateRepository.delete(cert);
    }

    // --- Semester Records ---
    public List<SemesterRecordDto> getSemesterRecords(Long studentId) {
        getAndVerifyStudent(studentId);
        return semesterRecordRepository.findByStudentId(studentId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public SemesterRecordDto addSemesterRecord(Long studentId, SemesterRecordDto dto) {
        Student student = getAndVerifyStudent(studentId);
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
        return mapToDto(semesterRecordRepository.save(record));
    }

    public SemesterRecordDto updateSemesterRecord(Long id, SemesterRecordDto dto) {
        SemesterRecord record = semesterRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Semester record not found: " + id));
        studentService.verifyAccess(record.getStudent());

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
        record.setSubjectsJson(dto.getSubjectsJson());

        return mapToDto(semesterRecordRepository.save(record));
    }

    public void deleteSemesterRecord(Long id) {
        SemesterRecord record = semesterRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Semester record not found: " + id));
        studentService.verifyAccess(record.getStudent());
        semesterRecordRepository.delete(record);
    }

    // --- Extra Activities ---
    public List<ExtraActivityDto> getExtraActivities(Long studentId) {
        getAndVerifyStudent(studentId);
        return extraActivityRepository.findByStudentId(studentId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public ExtraActivityDto addExtraActivity(Long studentId, ExtraActivityDto dto) {
        Student student = getAndVerifyStudent(studentId);
        ExtraActivity act = ExtraActivity.builder()
                .student(student)
                .type(dto.getType())
                .name(dto.getName())
                .details(dto.getDetails())
                .certificatePath(dto.getCertificatePath())
                .build();
        return mapToDto(extraActivityRepository.save(act));
    }

    public ExtraActivityDto updateExtraActivity(Long id, ExtraActivityDto dto) {
        ExtraActivity act = extraActivityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Extra activity record not found: " + id));
        studentService.verifyAccess(act.getStudent());

        act.setType(dto.getType());
        act.setName(dto.getName());
        act.setDetails(dto.getDetails());
        act.setCertificatePath(dto.getCertificatePath());

        return mapToDto(extraActivityRepository.save(act));
    }

    public void deleteExtraActivity(Long id) {
        ExtraActivity act = extraActivityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Extra activity record not found: " + id));
        studentService.verifyAccess(act.getStudent());
        extraActivityRepository.delete(act);
    }

    // --- Internships ---
    public List<InternshipDto> getInternships(Long studentId) {
        getAndVerifyStudent(studentId);
        return internshipRepository.findByStudentId(studentId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public InternshipDto addInternship(Long studentId, InternshipDto dto) {
        Student student = getAndVerifyStudent(studentId);
        Internship intern = Internship.builder()
                .student(student)
                .name(dto.getName())
                .date(dto.getDate())
                .location(dto.getLocation())
                .domain(dto.getDomain())
                .certificatePath(dto.getCertificatePath())
                .build();
        return mapToDto(internshipRepository.save(intern));
    }

    public InternshipDto updateInternship(Long id, InternshipDto dto) {
        Internship intern = internshipRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Internship record not found: " + id));
        studentService.verifyAccess(intern.getStudent());

        intern.setName(dto.getName());
        intern.setDate(dto.getDate());
        intern.setLocation(dto.getLocation());
        intern.setDomain(dto.getDomain());
        if (dto.getCertificatePath() != null) {
            intern.setCertificatePath(dto.getCertificatePath());
        }

        return mapToDto(internshipRepository.save(intern));
    }

    public void deleteInternship(Long id) {
        Internship intern = internshipRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Internship record not found: " + id));
        studentService.verifyAccess(intern.getStudent());
        internshipRepository.delete(intern);
    }

    // --- Industrial Visits ---
    public List<IndustrialVisitDto> getIndustrialVisits(Long studentId) {
        getAndVerifyStudent(studentId);
        return industrialVisitRepository.findByStudentId(studentId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public IndustrialVisitDto addIndustrialVisit(Long studentId, IndustrialVisitDto dto) {
        Student student = getAndVerifyStudent(studentId);
        IndustrialVisit visit = IndustrialVisit.builder()
                .student(student)
                .name(dto.getName())
                .date(dto.getDate())
                .location(dto.getLocation())
                .build();
        return mapToDto(industrialVisitRepository.save(visit));
    }

    public IndustrialVisitDto updateIndustrialVisit(Long id, IndustrialVisitDto dto) {
        IndustrialVisit visit = industrialVisitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Industrial visit record not found: " + id));
        studentService.verifyAccess(visit.getStudent());

        visit.setName(dto.getName());
        visit.setDate(dto.getDate());
        visit.setLocation(dto.getLocation());

        return mapToDto(industrialVisitRepository.save(visit));
    }

    public void deleteIndustrialVisit(Long id) {
        IndustrialVisit visit = industrialVisitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Industrial visit record not found: " + id));
        studentService.verifyAccess(visit.getStudent());
        industrialVisitRepository.delete(visit);
    }

    // --- Indisciplinary Activities ---
    public List<IndisciplinaryActivityDto> getIndisciplinaryActivities(Long studentId) {
        getAndVerifyStudent(studentId);
        return indisciplinaryActivityRepository.findByStudentId(studentId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public IndisciplinaryActivityDto addIndisciplinaryActivity(Long studentId, IndisciplinaryActivityDto dto) {
        Student student = getAndVerifyStudent(studentId);
        
        String rawAdded = (dto.getAddedBy() != null && !dto.getAddedBy().trim().isEmpty()) ? dto.getAddedBy() : username;
        String addedByString = rawAdded.replaceAll("\\s*\\([^)]*\\)", "").trim();

        IndisciplinaryActivity act = IndisciplinaryActivity.builder()
                .student(student)
                .description(dto.getDescription())
                .date(dto.getDate())
                .addedBy(addedByString)
                .build();
        return mapToDto(indisciplinaryActivityRepository.save(act));
    }

    public IndisciplinaryActivityDto updateIndisciplinaryActivity(Long id, IndisciplinaryActivityDto dto) {
        IndisciplinaryActivity act = indisciplinaryActivityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Indisciplinary activity record not found: " + id));
        studentService.verifyAccess(act.getStudent());

        act.setDescription(dto.getDescription());
        act.setDate(dto.getDate());
        if (dto.getAddedBy() != null && !dto.getAddedBy().trim().isEmpty()) {
            act.setAddedBy(dto.getAddedBy().replaceAll("\\s*\\([^)]*\\)", "").trim());
        }

        return mapToDto(indisciplinaryActivityRepository.save(act));
    }

    public void deleteIndisciplinaryActivity(Long id) {
        IndisciplinaryActivity act = indisciplinaryActivityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Indisciplinary activity record not found: " + id));
        studentService.verifyAccess(act.getStudent());
        indisciplinaryActivityRepository.delete(act);
    }

    // --- Mappings ---
    private TenthDetailDto mapToDto(TenthDetail entity) {
        return TenthDetailDto.builder()
                .id(entity.getId())
                .studentId(entity.getStudent().getId())
                .schoolName(entity.getSchoolName())
                .medium(entity.getMedium())
                .totalMarks(entity.getTotalMarks())
                .percentage(entity.getPercentage())
                .yearOfPassing(entity.getYearOfPassing())
                .build();
    }

    private TwelfthDetailDto mapToDto(TwelfthDetail entity) {
        return TwelfthDetailDto.builder()
                .id(entity.getId())
                .studentId(entity.getStudent().getId())
                .schoolName(entity.getSchoolName())
                .medium(entity.getMedium())
                .group(entity.getGroup())
                .totalMarks(entity.getTotalMarks())
                .percentage(entity.getPercentage())
                .cutoff(entity.getCutoff())
                .physicsMarks(entity.getPhysicsMarks())
                .chemistryMarks(entity.getChemistryMarks())
                .mathsMarks(entity.getMathsMarks())
                .yearOfPassing(entity.getYearOfPassing())
                .build();
    }

    private AdmissionDetailDto mapToDto(AdmissionDetail entity) {
        return AdmissionDetailDto.builder()
                .id(entity.getId())
                .studentId(entity.getStudent().getId())
                .scholarships(entity.getScholarships())
                .build();
    }

    private SiblingDto mapToDto(Sibling entity) {
        return SiblingDto.builder()
                .id(entity.getId())
                .studentId(entity.getStudent().getId())
                .name(entity.getName())
                .age(entity.getAge())
                .occupation(entity.getOccupation())
                .qualification(entity.getQualification())
                .emailId(entity.getEmailId())
                .mobileNumber(entity.getMobileNumber())
                .build();
    }

    private CertificateDto mapToDto(Certificate entity) {
        return CertificateDto.builder()
                .id(entity.getId())
                .studentId(entity.getStudent().getId())
                .type(entity.getType())
                .filePath(entity.getFilePath())
                .build();
    }

    private SemesterRecordDto mapToDto(SemesterRecord entity) {
        return SemesterRecordDto.builder()
                .id(entity.getId())
                .studentId(entity.getStudent().getId())
                .semesterNumber(entity.getSemesterNumber())
                .yearNumber(entity.getYearNumber())
                .mentorName(entity.getMentorName())
                .firstHourTestMarks(entity.getFirstHourTestMarks())
                .cat1(entity.getCat1())
                .cat2(entity.getCat2())
                .model(entity.getModel())
                .grade(entity.getGrade())
                .gpa(entity.getGpa())
                .cgpaTillNow(entity.getCgpaTillNow())
                .historyOfArrears(entity.getHistoryOfArrears())
                .standingArrears(entity.getStandingArrears())
                .build();
    }

    private ExtraActivityDto mapToDto(ExtraActivity entity) {
        return ExtraActivityDto.builder()
                .id(entity.getId())
                .studentId(entity.getStudent().getId())
                .type(entity.getType())
                .name(entity.getName())
                .details(entity.getDetails())
                .certificatePath(entity.getCertificatePath())
                .build();
    }

    private InternshipDto mapToDto(Internship entity) {
        return InternshipDto.builder()
                .id(entity.getId())
                .studentId(entity.getStudent().getId())
                .name(entity.getName())
                .date(entity.getDate())
                .location(entity.getLocation())
                .domain(entity.getDomain())
                .certificatePath(entity.getCertificatePath())
                .build();
    }

    private IndustrialVisitDto mapToDto(IndustrialVisit entity) {
        return IndustrialVisitDto.builder()
                .id(entity.getId())
                .studentId(entity.getStudent().getId())
                .name(entity.getName())
                .date(entity.getDate())
                .location(entity.getLocation())
                .build();
    }

    private IndisciplinaryActivityDto mapToDto(IndisciplinaryActivity entity) {
        String cleaned = entity.getAddedBy() != null ? entity.getAddedBy().replaceAll("\\s*\\([^)]*\\)", "").trim() : "";
        return IndisciplinaryActivityDto.builder()
                .id(entity.getId())
                .studentId(entity.getStudent().getId())
                .description(entity.getDescription())
                .date(entity.getDate())
                .addedBy(cleaned)
                .build();
    }
}
