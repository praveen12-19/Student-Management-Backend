package com.college.sdm.config;

import com.college.sdm.entity.*;
import com.college.sdm.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private HodDepartmentAssignmentRepository hodDepartmentAssignmentRepository;

    @Autowired
    private MentorRepository mentorRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // If 'Admin' exists but has ROLE_HOD, drop it so it re-seeds as ROLE_ADMIN
        userRepository.findByUsername("Admin").ifPresent(user -> {
            if (user.getRole() == Role.ROLE_HOD) {
                System.out.println("====== REMOVING OLD ADMIN (HOD ROLE) TO RE-SEED AS ROLE_ADMIN ======");
                studentRepository.deleteAll();
                mentorRepository.deleteAll();
                hodDepartmentAssignmentRepository.deleteAll();
                departmentRepository.deleteAll();
                userRepository.delete(user);
            }
        });

        // If the old "hod" or "mentor" user exists, clear the database of all default entries
        if (userRepository.existsByUsername("hod") || userRepository.existsByUsername("mentor")) {
            System.out.println("====== CLEANING DATABASE OF DEFAULT VALUES ======");
            studentRepository.deleteAll();
            mentorRepository.deleteAll();
            hodDepartmentAssignmentRepository.deleteAll();
            departmentRepository.deleteAll();
            
            userRepository.findByUsername("hod").ifPresent(userRepository::delete);
            userRepository.findByUsername("mentor").ifPresent(userRepository::delete);
            System.out.println("====== DATABASE CLEARED ======");
        }

        // Always ensure 'Admin' user exists
        if (!userRepository.existsByUsername("Admin")) {
            User adminUser = User.builder()
                    .username("Admin")
                    .password(passwordEncoder.encode("admin123"))
                    .name("Main Administrator")
                    .role(Role.ROLE_ADMIN)
                    .build();
            userRepository.save(adminUser);
            System.out.println("====== SEEDED ADMIN USER ======");
            System.out.println("Admin credentials: Admin / admin123");
            System.out.println("===============================");
        }
    }
}
