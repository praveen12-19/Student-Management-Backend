package com.college.sdm.controller;

import com.college.sdm.entity.SystemLog;
import com.college.sdm.repository.SystemLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/logs")
@PreAuthorize("hasRole('ADMIN') or hasRole('ROLE_ADMIN')")
public class LogController {

    @Autowired
    private SystemLogRepository systemLogRepository;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @GetMapping
    public ResponseEntity<List<Map<String, String>>> getLogs() {
        List<Map<String, String>> response = systemLogRepository.findAllByOrderByIdDesc().stream()
                .map(log -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("id", log.getId() != null ? String.valueOf(log.getId()) : "");
                    map.put("timestamp", log.getTimestamp() != null ? log.getTimestamp().format(formatter) : "");
                    map.put("user", log.getUsername() != null ? log.getUsername() : "");
                    map.put("action", log.getAction() != null ? log.getAction() : "");
                    map.put("details", log.getDetails() != null ? log.getDetails() : "");
                    return map;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<Void> clearLogs() {
        systemLogRepository.deleteAll();
        return ResponseEntity.noContent().build();
    }
}
