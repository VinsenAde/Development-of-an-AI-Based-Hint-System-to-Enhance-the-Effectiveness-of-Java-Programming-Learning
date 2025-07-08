    package com.thesis.java.javalearning.service;

    import org.springframework.stereotype.Service;
    import org.springframework.web.client.RestTemplate;
    import org.springframework.http.*;

    import java.util.Map;

    @Service
    public class HintGenerationService {

        private final String OLLAMA_URL = "http://localhost:11434/api/generate";
        private final RestTemplate restTemplate = new RestTemplate();

        public String generateHint(String prompt) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = Map.of(
                "model", "deepseek-coder-v2:16b",
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

                return (String) response.get("response"); // response is the actual reply
            } catch (Exception e) {
                return "❌ Error contacting local LLM: " + e.getMessage();
            }
        }
    }
