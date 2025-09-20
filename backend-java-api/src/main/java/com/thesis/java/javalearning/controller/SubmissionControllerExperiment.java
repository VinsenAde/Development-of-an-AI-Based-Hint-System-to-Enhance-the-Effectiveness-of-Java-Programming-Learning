package com.thesis.java.javalearning.controller;

import com.thesis.java.javalearning.service.SubmissionQueueExperiment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/experiment")
public class SubmissionControllerExperiment {

    private final SubmissionQueueExperiment queueService;

    public SubmissionControllerExperiment(SubmissionQueueExperiment queueService) {
        this.queueService = queueService;
    }

    @PostMapping("/submit-local")
    public ResponseEntity<String> submitCodeLocal(@RequestParam String id) {
        // Menerima request HTTP dan langsung mendelegasikan tugas ke background thread lokal
        queueService.queueSubmission(id, () -> {
            try {
                // Simulasi pemanggilan ProcessBuilder kompilasi kode yang memakan waktu I/O intensif selama 5 detik
                Thread.sleep(5000); 
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // Mengembalikan HTTP 202 Accepted secara instan (Non-blocking) ke client
        return ResponseEntity.accepted().body("Submission " + id + " berhasil masuk ke antrean memori.");
    }
}