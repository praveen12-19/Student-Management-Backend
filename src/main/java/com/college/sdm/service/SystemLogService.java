package com.college.sdm.service;

import com.college.sdm.entity.SystemLog;
import com.college.sdm.repository.SystemLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class SystemLogService {
    @Autowired
    private SystemLogRepository repository;

    public void log(String username, String action, String details) {
        SystemLog log = SystemLog.builder()
                .timestamp(LocalDateTime.now())
                .username(username)
                .action(action)
                .details(details)
                .build();
        repository.save(log);
    }
}
