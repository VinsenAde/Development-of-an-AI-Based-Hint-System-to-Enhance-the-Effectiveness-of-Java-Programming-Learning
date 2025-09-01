//package com.thesis.java.javalearning.entity;
//
//import jakarta.persistence.*;
//import java.time.LocalDateTime;
//import java.util.Map;
//import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
//import com.thesis.java.javalearning.util.HintCountConverter;
//
//@Entity
//@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
//public class Submission {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Lob
//    private String code;
//
//    private int sessionNumber;
//    private int hintsUsed;
//    private int failedRuns;
//    private int score;
//    private Boolean success;
//
//    @Lob
//    private String output;
//
//    private long onTaskTime;
//    private long offTaskTime;
//    
//    private LocalDateTime submittedAt;
//        private String aiFeedback;
//
//    private String hintLevelCap;
//
//    @Convert(converter = HintCountConverter.class)
//    @Lob
//    private Map<String, Integer> hintCounts;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id")
//    private User user;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "problem_id")
//    private Problem problem;
//
//    // Getter & Setter (auto-generate)
//    public Long getId() { return id; }
//    public void setId(Long id) { this.id = id; }
//
//    public String getCode() { return code; }
//    public void setCode(String code) { this.code = code; }
//
//    public int getSessionNumber() { return sessionNumber; }
//    public void setSessionNumber(int sessionNumber) { this.sessionNumber = sessionNumber; }
//
//    public int getHintsUsed() { return hintsUsed; }
//    public void setHintsUsed(int hintsUsed) { this.hintsUsed = hintsUsed; }
//
//    public int getFailedRuns() { return failedRuns; }
//    public void setFailedRuns(int failedRuns) { this.failedRuns = failedRuns; }
//
//    public int getScore() { return score; }
//    public void setScore(int score) { this.score = score; }
//
//    public Boolean isSuccess() { return success; }
//    public void setSuccess(Boolean success) { this.success = success; }
//
//    public String getOutput() { return output; }
//    public void setOutput(String output) { this.output = output; }
//
//    public long getOnTaskTime() { return onTaskTime; }
//    public void setOnTaskTime(long onTaskTime) { this.onTaskTime = onTaskTime; }
//
//    public long getOffTaskTime() { return offTaskTime; }
//    public void setOffTaskTime(long offTaskTime) { this.offTaskTime = offTaskTime; }
//
//    public LocalDateTime getSubmittedAt() { return submittedAt; }
//    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
//
//    public Map<String, Integer> getHintCounts() { return hintCounts; }
//    public void setHintCounts(Map<String, Integer> hintCounts) { this.hintCounts = hintCounts; }
//
//    public User getUser() { return user; }
//    public void setUser(User user) { this.user = user; }
//
//    public Problem getProblem() { return problem; }
//    public void setProblem(Problem problem) { this.problem = problem; }
//        public String getHintLevelCap() { return hintLevelCap; }
//    public void setHintLevelCap(String hintLevelCap) { this.hintLevelCap = hintLevelCap; }
//        public String getAiFeedback() {
//        return aiFeedback;
//    }
//
//    public void setAiFeedback(String aiFeedback) {
//        this.aiFeedback = aiFeedback;
//    }
//
//
//}
//
//
package com.thesis.java.javalearning.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.thesis.java.javalearning.util.HintCountConverter;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    private String code;

    private int sessionNumber;
    private int hintsUsed;
    private int failedRuns;
    private int score;
    private Boolean success;

    @Lob
    private String output;

    private long onTaskTime;
    private long offTaskTime;

    private LocalDateTime submittedAt;

    private String hintLevelCap;

    // Use the HintCountConverter to map the hintCounts field to a JSON string in the database
    @Convert(converter = HintCountConverter.class)
    @Lob
    private Map<String, Integer> hintCounts;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id")
    private Problem problem;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public int getSessionNumber() { return sessionNumber; }
    public void setSessionNumber(int sessionNumber) { this.sessionNumber = sessionNumber; }

    public int getHintsUsed() { return hintsUsed; }
    public void setHintsUsed(int hintsUsed) { this.hintsUsed = hintsUsed; }

    public int getFailedRuns() { return failedRuns; }
    public void setFailedRuns(int failedRuns) { this.failedRuns = failedRuns; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public Boolean isSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }

    public String getOutput() { return output; }
    public void setOutput(String output) { this.output = output; }

    public long getOnTaskTime() { return onTaskTime; }
    public void setOnTaskTime(long onTaskTime) { this.onTaskTime = onTaskTime; }

    public long getOffTaskTime() { return offTaskTime; }
    public void setOffTaskTime(long offTaskTime) { this.offTaskTime = offTaskTime; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public Map<String, Integer> getHintCounts() { return hintCounts; }
    public void setHintCounts(Map<String, Integer> hintCounts) { this.hintCounts = hintCounts; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Problem getProblem() { return problem; }
    public void setProblem(Problem problem) { this.problem = problem; }

    public String getHintLevelCap() { return hintLevelCap; }
    public void setHintLevelCap(String hintLevelCap) { this.hintLevelCap = hintLevelCap; }

}
