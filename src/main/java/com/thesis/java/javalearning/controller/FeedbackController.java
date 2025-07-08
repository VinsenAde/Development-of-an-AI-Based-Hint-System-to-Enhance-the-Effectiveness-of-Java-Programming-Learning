package com.thesis.java.javalearning.controller;

import com.thesis.java.javalearning.service.AIFeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    @Autowired
    private AIFeedbackService aiFeedbackService;

    /**
     * Endpoint untuk menghasilkan dan menyimpan AI feedback.
     */
    @PostMapping("/generate")
    public ResponseEntity<String> generateFeedback(
            @RequestBody FeedbackRequest req,
            Principal principal
    ) {
        String prompt = buildPrompt(
            req.getTotalSubmissions(),
            req.getRunsToSuccess(),
            req.getHintUsage(),
            req.getAvgScore(),
            req.getTotalOnTaskTime(),
            req.getTotalOffTaskTime(),
            req.getHintDistribution()
        );

        String feedback = aiFeedbackService.generateAIFeedbackAndSave(
            prompt, principal.getName()
        );
        return ResponseEntity.ok(feedback);
    }

    /**
     * Membangun prompt berdasarkan statistik lengkap mahasiswa.
     */
    private String buildPrompt(
        long totalSubmissions,
        long runsToSuccess,
        long hintUsage,
        double avgScore,
        long totalOnTask,
        long totalOffTask,
        Map<String, Integer> hintDistribution
    ) {
        String dist = hintDistribution.entrySet().stream()
            .map(e -> e.getKey() + ":" + e.getValue())
            .collect(Collectors.joining(", "));

        double focusRatio = totalOnTask + totalOffTask > 0
            ? (totalOnTask * 100.0) / (totalOnTask + totalOffTask)
            : 0.0;

        return String.format("""
            Based on the following student statistics:
            - Total submissions (attempts): %d
            - Average runs-to-success: %d
            - Total hints used: %d
            - Average score: %.2f
            - Total on-task time: %d seconds
            - Total off-task time: %d seconds
            - On/off-task focus ratio: %.1f%%
            - Hint types distribution: %s

            You are an AI assistant in an educational programming system for beginner/intermediate Java learners.

            Generate a detailed, personalized feedback message with these sections:
            1. **Strengths:** What they did well.
            2. **Weaknesses:** Where they can improve.
            3. **Suggestions for Improvement:** Actionable tips.
            4. **Time Management Analysis:** Focus vs distraction insights.
            5. **Hint Usage Evaluation:** Effectiveness of their hint strategy.
            6. **Problem-Solving Behavior Insights:** What their attempt pattern suggests.
            7. **Learning Trend:** Note any improvement or plateau.
            8. **Encouragement and Motivation:** A supportive closing message.

            Use organized prose, avoid simply restating numbers, and focus on interpretation.
            Your goal is to help the student feel seen, supported, and guided toward growth.
        """,
        totalSubmissions,
        runsToSuccess,
        hintUsage,
        avgScore,
        totalOnTask,
        totalOffTask,
        focusRatio,
        dist
        );
    }

    /**
     * DTO untuk menampung semua metrik yang diperlukan.
     */
    public static class FeedbackRequest {
        private long totalSubmissions;
        private long runsToSuccess;
        private long hintUsage;
        private double avgScore;
        private long totalOnTaskTime;
        private long totalOffTaskTime;
        private Map<String,Integer> hintDistribution;

        // --- Getters & Setters ---
        public long getTotalSubmissions() {
            return totalSubmissions;
        }
        public void setTotalSubmissions(long totalSubmissions) {
            this.totalSubmissions = totalSubmissions;
        }

        public long getRunsToSuccess() {
            return runsToSuccess;
        }
        public void setRunsToSuccess(long runsToSuccess) {
            this.runsToSuccess = runsToSuccess;
        }

        public long getHintUsage() {
            return hintUsage;
        }
        public void setHintUsage(long hintUsage) {
            this.hintUsage = hintUsage;
        }

        public double getAvgScore() {
            return avgScore;
        }
        public void setAvgScore(double avgScore) {
            this.avgScore = avgScore;
        }

        public long getTotalOnTaskTime() {
            return totalOnTaskTime;
        }
        public void setTotalOnTaskTime(long totalOnTaskTime) {
            this.totalOnTaskTime = totalOnTaskTime;
        }

        public long getTotalOffTaskTime() {
            return totalOffTaskTime;
        }
        public void setTotalOffTaskTime(long totalOffTaskTime) {
            this.totalOffTaskTime = totalOffTaskTime;
        }

        public Map<String, Integer> getHintDistribution() {
            return hintDistribution;
        }
        public void setHintDistribution(Map<String, Integer> hintDistribution) {
            this.hintDistribution = hintDistribution;
        }
    }
}
