package com.thesis.java.javalearning.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SubmissionQueueExperiment {
    private static final Logger log = LoggerFactory.getLogger(SubmissionQueueExperiment.class);

    // Core pool: 2 thread, Max pool: 4 thread, Kapasitas antrean di RAM: 100 tugas
    private final ExecutorService executorService = new ThreadPoolExecutor(
        2, 4, 60L, TimeUnit.SECONDS,
        new LinkedBlockingQueue<>(100)
    );

    public void queueSubmission(String submissionId, Runnable compilationTask) {
        log.info("[EXPERIMENTAL] Mengantrekan submission ID: {} ke dalam JVM memory queue", submissionId);
        
        executorService.submit(() -> {
            try {
                log.info("[EXPERIMENTAL] Thread pool lokal mulai memproses biner untuk ID: {}", submissionId);
                compilationTask.run();
                log.info("[EXPERIMENTAL] Sukses mengeksekusi tugas pada thread lokal untuk ID: {}", submissionId);
            } catch (Exception e) {
                log.error("[EXPERIMENTAL] Kegagalan eksekusi pada thread pool lokal", e);
            }
        });
    }
}