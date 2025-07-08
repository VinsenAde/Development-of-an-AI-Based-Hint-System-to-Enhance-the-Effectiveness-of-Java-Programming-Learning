package com.thesis.java.javalearning.dto;

import com.thesis.java.javalearning.entity.Submission;

public class SubmissionResponseDTO {
    private String code;
    private int score;
    private String output;
    private int sessionNumber;
    private int hintsUsed;
    private long onTaskTime;
    private long offTaskTime;
    private boolean success;
    private Long problemId;
    private String hintLevelCap;

    // Constructor
    public SubmissionResponseDTO(Submission s) {
        this.code = s.getCode();
        this.score = s.getScore();
        this.output = s.getOutput();
        this.sessionNumber = s.getSessionNumber();
        this.hintsUsed = s.getHintsUsed();
        this.onTaskTime = s.getOnTaskTime();
        this.offTaskTime = s.getOffTaskTime();
        this.success = s.isSuccess();
        this.problemId = s.getProblem() != null ? s.getProblem().getId() : null;

    }

    // Getters & Setters
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public String getOutput() { return output; }
    public void setOutput(String output) { this.output = output; }

    public int getSessionNumber() { return sessionNumber; }
    public void setSessionNumber(int sessionNumber) { this.sessionNumber = sessionNumber; }

    public int getHintsUsed() { return hintsUsed; }
    public void setHintsUsed(int hintsUsed) { this.hintsUsed = hintsUsed; }

    public long getOnTaskTime() { return onTaskTime; }
    public void setOnTaskTime(long onTaskTime) { this.onTaskTime = onTaskTime; }

    public long getOffTaskTime() { return offTaskTime; }
    public void setOffTaskTime(long offTaskTime) { this.offTaskTime = offTaskTime; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public Long getProblemId() { return problemId; }
    public void setProblemId(Long problemId) { this.problemId = problemId; }

    public String getHintLevelCap() { return hintLevelCap; }
    public void setHintLevelCap(String cap) { this.hintLevelCap = cap; }
}
