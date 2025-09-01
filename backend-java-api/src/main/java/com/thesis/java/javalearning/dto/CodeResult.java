package com.thesis.java.javalearning.dto;

/**
 * DTO untuk mewakili hasil eksekusi dan evaluasi kode pengguna.
 */

public class CodeResult {
    private boolean success;
    private String output;
    private int score;
    private int failedRuns;
    private String feedbackMessage;

    // ✅ Add this:
    public CodeResult() {
    }

    public CodeResult(boolean success, String output, int score, int failedRuns, String feedbackMessage) {
        this.success = success;
        this.output = output;
        this.score = score;
        this.failedRuns = failedRuns;
        this.feedbackMessage = feedbackMessage;
    }

    public CodeResult(boolean success, String output, int score, int failedRuns) {
        this(success, output, score, failedRuns, "");
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getFailedRuns() {
        return failedRuns;
    }

    public void setFailedRuns(int failedRuns) {
        this.failedRuns = failedRuns;
    }

    public String getFeedbackMessage() {
        return feedbackMessage;
    }

    public void setFeedbackMessage(String feedbackMessage) {
        this.feedbackMessage = feedbackMessage;
    }
}
