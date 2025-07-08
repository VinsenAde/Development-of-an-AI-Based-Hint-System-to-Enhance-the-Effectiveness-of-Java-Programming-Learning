package com.thesis.java.javalearning.service;

import com.thesis.java.javalearning.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class AIFeedbackService {

    private final String OLLAMA_URL = "http://localhost:11434/api/generate"; // URL untuk lokal LLM (DeepSeek)
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Menghasilkan feedback berbasis analisis performa.
     * @param prompt
     * @return 
     */
    
    @Autowired
private UserRepository userRepository;

public String generateAIFeedbackAndSave(String prompt, String username) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    Map<String, Object> requestBody = Map.of(
            "model", "deepseek-v2:16b",
            "prompt", prompt,
            "stream", false
    );

    HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

    try {
        Map<String, Object> response = restTemplate.postForObject(
                OLLAMA_URL,
                request,
                Map.class
        );

        String feedback = (String) response.get("response");

        // Save feedback to user
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setAiFeedback(feedback);
            userRepository.save(user);
        });

        return feedback;

    } catch (Exception e) {
        return "❌ Error contacting local LLM: " + e.getMessage();
    }
}

//    public String generateAIFeedback(String prompt) {
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//
//        Map<String, Object> requestBody = Map.of(
//                "model", "deepseek-v2:16b",  // Ganti dengan model lokal Anda
//                "prompt", prompt,  // Prompt dari analisis data performa
//                "stream", false  // Stream tidak digunakan, tunggu hingga response lengkap
//        );
//
//        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
//
//        try {
//            Map<String, Object> response = restTemplate.postForObject(
//                    OLLAMA_URL,
//                    request,
//                    Map.class
//            );
//
//            // Mendapatkan response feedback dari model
//            return (String) response.get("response");
//        } catch (Exception e) {
//            // Error handling
//            return "❌ Error contacting local LLM: " + e.getMessage();
//        }
//    }
}
