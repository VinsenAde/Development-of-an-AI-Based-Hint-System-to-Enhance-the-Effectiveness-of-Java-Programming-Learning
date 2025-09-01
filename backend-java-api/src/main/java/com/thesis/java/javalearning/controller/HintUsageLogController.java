package com.thesis.java.javalearning.controller;

import com.thesis.java.javalearning.entity.HintUsageLog;
import com.thesis.java.javalearning.repository.HintUsageLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hint-logs")
public class HintUsageLogController {

    @Autowired
    private HintUsageLogRepository hintRepo;

    @GetMapping
    public List<HintUsageLog> getAllLogs() {
        return hintRepo.findAll();
    }

    @GetMapping("/user/{userId}")
    public List<HintUsageLog> getByUser(@PathVariable Long userId) {
        return hintRepo.findAll().stream()
            .filter(log -> log.getUser().getId().equals(userId))
            .toList();
    }

    @GetMapping("/session")
    public List<HintUsageLog> getBySession(
            @RequestParam Long userId,
            @RequestParam int sessionNumber) {
        return hintRepo.findAll().stream()
            .filter(log -> log.getUser().getId().equals(userId)
                        && log.getSessionNumber() == sessionNumber)
            .toList();
    }
}
