package com.thesis.java.javalearning.dto;

/**
 * DTO for collecting student performance metrics
 * to be used for AI feedback generation.
 */
public class FeedbackRequest {

    private int hintUsage;
    private int failedRuns;
    private double avgScore;
    private String totalTimeOnTask;

    // Constructors
    public FeedbackRequest() {}

    public FeedbackRequest(int hintUsage, int failedRuns, double avgScore, String totalTimeOnTask) {
        this.hintUsage = hintUsage;
        this.failedRuns = failedRuns;
        this.avgScore = avgScore;
        this.totalTimeOnTask = totalTimeOnTask;
    }

    // Getters and Setters
    public int getHintUsage() {
        return hintUsage;
    }

    public void setHintUsage(int hintUsage) {
        this.hintUsage = hintUsage;
    }

    public int getFailedRuns() {
        return failedRuns;
    }

    public void setFailedRuns(int failedRuns) {
        this.failedRuns = failedRuns;
    }

    public double getAvgScore() {
        return avgScore;
    }

    public void setAvgScore(double avgScore) {
        this.avgScore = avgScore;
    }

    public String getTotalTimeOnTask() {
        return totalTimeOnTask;
    }

    public void setTotalTimeOnTask(String totalTimeOnTask) {
        this.totalTimeOnTask = totalTimeOnTask;
    }
}
