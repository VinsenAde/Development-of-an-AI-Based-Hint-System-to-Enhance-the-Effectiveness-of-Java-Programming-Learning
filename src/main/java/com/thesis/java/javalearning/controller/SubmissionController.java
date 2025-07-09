package com.thesis.java.javalearning.controller;

import com.thesis.java.javalearning.entity.Problem;
import com.thesis.java.javalearning.entity.Submission;
import com.thesis.java.javalearning.entity.User;
import com.thesis.java.javalearning.repository.ProblemRepository;
import com.thesis.java.javalearning.repository.SubmissionRepository;
import com.thesis.java.javalearning.repository.UserRepository;
import com.thesis.java.javalearning.dto.CodeResult;
import com.thesis.java.javalearning.service.CodeExecutionService;
import com.thesis.java.javalearning.service.ScoringService;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {

    @Autowired private CodeExecutionService codeExecService;
    @Autowired private SubmissionRepository submissionRepo;
    @Autowired private ProblemRepository problemRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ScoringService scoringService;

    // SUBMIT & SCORE
    @PostMapping
    public ResponseEntity<?> submitCode(@RequestBody SubmissionRequest request) {
        try {
            // --- Get user from session login (SecurityContext) ---
            org.springframework.security.core.Authentication auth =
                    org.springframework.security.core.context.SecurityContextHolder
                                                         .getContext()
                                                         .getAuthentication();
            String username = auth.getName();
            User user = userRepository.findByUsername(username)
                                      .orElseThrow(() -> new RuntimeException("User not found"));

            // --- Prevent duplicate submission in same session ---
            boolean exists = submissionRepo
                    .existsByUser_IdAndProblem_IdAndSessionNumber(
                            user.getId(), request.getProblemId(), request.getSessionNumber());
            if (exists) {
                return ResponseEntity.badRequest()
                                     .body("Submission already exists for this task in the current session.");
            }

            Problem problem = problemRepository.findById(request.getProblemId())
                                               .orElseThrow(() -> new RuntimeException("Problem not found"));

            Map<String, Integer> hintMap = request.getHintCounts() != null
                                           ? request.getHintCounts()
                                           : Map.of();
            long elapsed   = request.getElapsedSeconds() != null
                           ? request.getElapsedSeconds()
                           : 0L;
            long timeLimit = request.getTimeLimitSeconds() != null
                           ? request.getTimeLimitSeconds()
                           : 300L;

            String hintLevelCap = request.getHintLevelCap();
            if (hintLevelCap == null || hintLevelCap.isEmpty()) {
                hintLevelCap = ScoringService.getMaxHintLevel(hintMap);
            }

            // --- Run code evaluation ---
            CodeResult result = codeExecService.executeAndEvaluate(
                    request.getCode(),
                    problem.getExpectedOutput(),
                    hintMap,
                    request.getFailedRuns(),
                    elapsed,
                    timeLimit,
                    hintLevelCap,
                    request.getUserInput(),
                    request.getOnTaskTime() != null ? request.getOnTaskTime() : 0L,   // Passed new parameter
                    request.getOffTaskTime() != null ? request.getOffTaskTime() : 0L  // Passed new parameter
            );

            // If run failed, return feedback without persisting
            if (!result.isSuccess()) {
                return ResponseEntity.ok(result);
            }

            // --- Calculate final score ---
            Duration duration = Duration.ofSeconds(elapsed);
            int finalScore = scoringService.calculateScore(
                    hintMap,
                    request.getFailedRuns(),
                    duration,
                    hintLevelCap,
                    request.getOnTaskTime() != null ? request.getOnTaskTime() : 0L,   // Passed new parameter
                    request.getOffTaskTime() != null ? request.getOffTaskTime() : 0L  // Passed new parameter
            );

            // --- Persist successful submission ---
            Submission submission = new Submission();
            submission.setUser(user);
            submission.setProblem(problem);
            submission.setSessionNumber(request.getSessionNumber());
            submission.setCode(request.getCode());
            submission.setHintsUsed(request.getHintsUsed());
            submission.setHintCounts(hintMap);

            // store the **updated** failed-runs from the evaluation result
            submission.setFailedRuns(result.getFailedRuns());

            submission.setSubmittedAt(LocalDateTime.now());
            submission.setScore(finalScore);
            submission.setSuccess(true);
            submission.setOutput(result.getOutput());
            submission.setOnTaskTime(request.getOnTaskTime() != null ? request.getOnTaskTime() : 0);
            submission.setOffTaskTime(request.getOffTaskTime() != null ? request.getOffTaskTime() : 0);
            submission.setHintLevelCap(hintLevelCap);

            submissionRepo.save(submission);

            // reflect final score and true failed-runs back to the client
            result.setScore(finalScore);
            result.setFailedRuns(submission.getFailedRuns());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                                 .body("Submission failed: " + e.getMessage());
        }
    }

    // RUN ONLY (no DB)
    @PostMapping("/run")
    public ResponseEntity<CodeResult> runOnly(@RequestBody Map<String, Object> body) {
        String code      = (String) body.get("code");
        Long problemId   = ((Number) body.get("problemId")).longValue();
        String expected  = problemRepository.findById(problemId)
                                            .map(Problem::getExpectedOutput)
                                            .orElse("");
        // For /run endpoint, onTaskTime and offTaskTime are not typically sent from client
        // and are not used for scoring here anyway. Pass 0L for simplicity.
        CodeResult result = codeExecService.executeAndEvaluate(
            code, expected, Map.of(), 0, 0L, 300L, null, "", 0L, 0L
        );
        return ResponseEntity.ok(result);
    }
// NORMAL USER: Fetch submissions for current user and session
@GetMapping
public List<Submission> getUserSubmissions(@RequestParam int sessionNumber) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String username = auth.getName();
    User user = userRepository.findByUsername(username)
                              .orElseThrow(() -> new RuntimeException("User not found"));

    return submissionRepo.findByUserIdAndSessionNumber(user.getId(), sessionNumber);
}

// ADMIN: Fetch all submissions for any user (no session filter)
@GetMapping("/admin")
public List<Submission> getAllSubmissionsForUser(@RequestParam Long userId,
                                                 @RequestParam(required = false, defaultValue = "1") int sessionNumber) {
    return submissionRepo.findByUserIdAndSessionNumber(userId, sessionNumber);
}

// @GetMapping
//public List<Submission> getAllSubmissions(
//        // The @RequestParam for userId is removed.
//        @RequestParam int sessionNumber
//) {
//    // Get the currently authenticated user
//    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//    String username = auth.getName();
//    User user = userRepository.findByUsername(username)
//                              .orElseThrow(() -> new RuntimeException("User not found"));
//
//    // Fetch submissions for the authenticated user
//    return submissionRepo.findByUserIdAndSessionNumber(user.getId(), sessionNumber);
//}

// Also, modify the deleteAllSubmissions method similarly

//@DeleteMapping
//@Transactional
//public ResponseEntity<?> deleteAllSubmissions(
//        // The @RequestParam for userId is removed.
//        @RequestParam int sessionNumber
//) {
//    // Get the currently authenticated user
//    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//    String username = auth.getName();
//    User user = userRepository.findByUsername(username)
//                              .orElseThrow(() -> new RuntimeException("User not found"));
//
//    submissionRepo.deleteByUser_IdAndSessionNumber(user.getId(), sessionNumber);
//    return ResponseEntity.ok().build();
//}
    // DTO: SubmissionRequest
    public static class SubmissionRequest {
        private Long problemId;
        private Long userId;
        private int sessionNumber;
        private String code;
        private int hintsUsed;
        private int failedRuns;
        private Long elapsedSeconds;
        private Long timeLimitSeconds;
        private Long onTaskTime;
        private Long offTaskTime;
        private Map<String, Integer> hintCounts;
        private String hintLevelCap;
        private String userInput;

        // … standard getters & setters …
        public Long getProblemId() { return problemId; }
        public void setProblemId(Long problemId) { this.problemId = problemId; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public int getSessionNumber() { return sessionNumber; }
        public void setSessionNumber(int sessionNumber) { this.sessionNumber = sessionNumber; }
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public int getHintsUsed() { return hintsUsed; }
        public void setHintsUsed(int hintsUsed) { this.hintsUsed = hintsUsed; }
        public int getFailedRuns() { return failedRuns; }
        public void setFailedRuns(int failedRuns) { this.failedRuns = failedRuns; }
        public Long getElapsedSeconds() { return elapsedSeconds; }
        public void setElapsedSeconds(Long elapsedSeconds) { this.elapsedSeconds = elapsedSeconds; }
        public Long getTimeLimitSeconds() { return timeLimitSeconds; }
        public void setTimeLimitSeconds(Long timeLimitSeconds) { this.timeLimitSeconds = timeLimitSeconds; }
        public Long getOnTaskTime() { return onTaskTime; }
        public void setOnTaskTime(Long onTaskTime) { this.onTaskTime = onTaskTime; }
        public Long getOffTaskTime() { return offTaskTime; }
        public void setOffTaskTime(Long offTaskTime) { this.offTaskTime = offTaskTime; }
        public Map<String, Integer> getHintCounts() { return hintCounts; }
        public void setHintCounts(Map<String, Integer> hintCounts) { this.hintCounts = hintCounts; }
        public String getHintLevelCap() { return hintLevelCap; }
        public void setHintLevelCap(String hintLevelCap) { this.hintLevelCap = hintLevelCap; }
        public String getUserInput() { return userInput; }
        public void setUserInput(String userInput) { this.userInput = userInput; }
    }
}