package com.thesis.java.javalearning.dto;

import java.util.Collections;
import java.util.Map;

public class UserSummaryDto {

    private Long userId;
    private String username;
    private String fullName; // <--- This was added in the previous step
    private int totalSubmissions;
    private double avgScore;
    private int totalHints;
    private int totalFailedRuns;

    // --- for charts ---
    private Map<String, Integer> capMap;
    private Map<String, Double> avgScorePerTask;
    private Map<String, Long> onTaskTimePerTask;
    private Map<String, Long> offTaskTimePerTask;

    // Empty constructor
    public UserSummaryDto() {
        // default ctor leaves everything null/zero; you can always use setters
    }

    /**
     * Constructor for basic user summary, without chart data.
     * This constructor is now explicitly defined to match the 7-argument call.
     */
    public UserSummaryDto(Long userId,
                          String username,
                          String fullName, // <--- Added this parameter
                          int totalSubmissions,
                          double avgScore,
                          int totalHints,
                          int totalFailedRuns)
    {
        this.userId           = userId;
        this.username         = username;
        this.fullName         = fullName; // <--- Set the fullName
        this.totalSubmissions = totalSubmissions;
        this.avgScore         = avgScore;
        this.totalHints       = totalHints;
        this.totalFailedRuns  = totalFailedRuns;
        // Initialize chart maps to empty to avoid NullPointerExceptions if accessed
        this.capMap = Collections.emptyMap();
        this.avgScorePerTask = Collections.emptyMap();
        this.onTaskTimePerTask = Collections.emptyMap();
        this.offTaskTimePerTask = Collections.emptyMap();
    }


    /**
     * Main constructor including all fields for user summary and charts.
     */
    public UserSummaryDto(Long userId,
                          String username,
                          String fullName,
                          int totalSubmissions,
                          double avgScore,
                          int totalHints,
                          int totalFailedRuns,
                          Map<String, Integer> capMap,
                          Map<String, Double> avgScorePerTask,
                          Map<String, Long> onTaskTimePerTask,
                          Map<String, Long> offTaskTimePerTask)
    {
        this.userId           = userId;
        this.username         = username;
        this.fullName         = fullName;
        this.totalSubmissions = totalSubmissions;
        this.avgScore         = avgScore;
        this.totalHints       = totalHints;
        this.totalFailedRuns  = totalFailedRuns;
        this.capMap           = capMap;
        this.avgScorePerTask  = avgScorePerTask;
        this.onTaskTimePerTask = onTaskTimePerTask;
        this.offTaskTimePerTask = offTaskTimePerTask;
    }

    // Getters & Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public int getTotalSubmissions() {
        return totalSubmissions;
    }
    public void setTotalSubmissions(int totalSubmissions) {
        this.totalSubmissions = totalSubmissions;
    }

    public double getAvgScore() {
        return avgScore;
    }
    public void setAvgScore(double avgScore) {
        this.avgScore = avgScore;
    }

    public int getTotalHints() {
        return totalHints;
    }
    public void setTotalHints(int totalHints) {
        this.totalHints = totalHints;
    }

    public int getTotalFailedRuns() {
        return totalFailedRuns;
    }
    public void setTotalFailedRuns(int totalFailedRuns) {
        this.totalFailedRuns = totalFailedRuns;
    }

    public Map<String, Integer> getCapMap() {
        return capMap;
    }
    public void setCapMap(Map<String, Integer> capMap) {
        this.capMap = capMap;
    }

    public Map<String, Double> getAvgScorePerTask() {
        return avgScorePerTask;
    }
    public void setAvgScorePerTask(Map<String, Double> avgScorePerTask) {
        this.avgScorePerTask = avgScorePerTask;
    }

    public Map<String, Long> getOnTaskTimePerTask() {
        return onTaskTimePerTask;
    }
    public void setOnTaskTimePerTask(Map<String, Long> onTaskTimePerTask) {
        this.onTaskTimePerTask = onTaskTimePerTask;
    }

    public Map<String, Long> getOffTaskTimePerTask() {
        return offTaskTimePerTask;
    }
    public void setOffTaskTimePerTask(Map<String, Long> offTaskTimePerTask) {
        this.offTaskTimePerTask = offTaskTimePerTask;
    }
}