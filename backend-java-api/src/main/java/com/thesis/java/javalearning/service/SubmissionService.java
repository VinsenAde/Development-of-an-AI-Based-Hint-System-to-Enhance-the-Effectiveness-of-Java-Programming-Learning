package com.thesis.java.javalearning.service; // Sesuaikan

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class SubmissionService {

    private static final Logger logger = LoggerFactory.getLogger(SubmissionService.class);

    @Async("submissionExecutor") // Memanggil bean executor yang kita buat
    public void processSubmissionAsync(String codePayload) {
        try {
            logger.info("Memproses kode... Thread: {}", Thread.currentThread().getName());
            
            // SIMULASI 1: Alokasi memori untuk mensimulasikan objek kompilasi OS (1 MB per tugas)
            byte[] dummyMemoryHog = new byte[1024 * 1024]; 
            
            // SIMULASI 2: Meniru ProcessBuilder yang butuh waktu 3 detik untuk compile
            Thread.sleep(3000); 
            
            logger.info("Selesai memproses kode.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Proses dihentikan.");
        }
    }
}