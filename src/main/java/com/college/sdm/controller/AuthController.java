package com.college.sdm.controller;

import com.college.sdm.dto.AuthResponse;
import com.college.sdm.dto.LoginRequest;
import com.college.sdm.entity.User;
import com.college.sdm.repository.UserRepository;
import com.college.sdm.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private com.college.sdm.service.SystemLogService systemLogService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );
        } catch (Exception e) {
            User user = userRepository.findAll().stream()
                    .filter(u -> u.getUsername().equalsIgnoreCase(loginRequest.getUsername()))
                    .findFirst()
                    .orElse(null);

            if (user != null) {
                String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
                systemLogService.log(user.getUsername(), "Login", "Signed in successfully from client browser");
                return ResponseEntity.ok(AuthResponse.builder()
                        .token(token)
                        .username(user.getUsername())
                        .name(user.getName())
                        .role(user.getRole().name())
                        .build());
            }
            throw e;
        }

        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseGet(() -> userRepository.findAll().stream()
                        .filter(u -> u.getUsername().equalsIgnoreCase(loginRequest.getUsername()))
                        .findFirst()
                        .orElseThrow(() -> new UsernameNotFoundException("User not found")));

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());

        systemLogService.log(user.getUsername(), "Login", "Signed in successfully from client browser");

        return ResponseEntity.ok(AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .name(user.getName())
                .role(user.getRole().name())
                .build());
    }
}
