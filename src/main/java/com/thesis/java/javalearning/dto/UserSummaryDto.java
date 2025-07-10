package com.thesis.java.javalearning.dto;

import java.util.Collections;
import java.util.Map;

public class UserSummaryDto {

    private Long userId;
    private String username;
    private String fullName;
    private int totalSubmissions;
    private double avgScore;
    private int totalHints;
    private int totalFailedRuns;

    // Statistik tambahan untuk keperluan CSV dan evaluasi
    private int noHintCount;
    private int usedHintCount;
    private long avgOnTaskTime;
    private long avgOffTaskTime;

    // Grafik / statistik detail
    private Map<String, Integer> capMap;
    private Map<String, Double> avgScorePerTask;
    private Map<String, Long> onTaskTimePerTask;
    private Map<String, Long> offTaskTimePerTask;

    // Constructor basic (untuk ringkasan)
    public UserSummaryDto(Long userId,
                          String username,
                          String fullName,
                          int totalSubmissions,
                          double avgScore,
                          int totalHints,
                          int totalFailedRuns) {
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.totalSubmissions = totalSubmissions;
        this.avgScore = avgScore;
        this.totalHints = totalHints;
        this.totalFailedRuns = totalFailedRuns;

        // default agar tidak null saat dipanggil
        this.capMap = Collections.emptyMap();
        this.avgScorePerTask = Collections.emptyMap();
        this.onTaskTimePerTask = Collections.emptyMap();
        this.offTaskTimePerTask = Collections.emptyMap();
    }

    // Constructor lengkap (misalnya untuk single user page)
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
                          Map<String, Long> offTaskTimePerTask) {
        this(userId, username, fullName, totalSubmissions, avgScore, totalHints, totalFailedRuns);
        this.capMap = capMap;
        this.avgScorePerTask = avgScorePerTask;
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

    public int getTotalSubmissions() { return totalSubmissions; }
    public void setTotalSubmissions(int totalSubmissions) { this.totalSubmissions = totalSubmissions; }

    public double getAvgScore() { return avgScore; }
    public void setAvgScore(double avgScore) { this.avgScore = avgScore; }

    public int getTotalHints() { return totalHints; }
    public void setTotalHints(int totalHints) { this.totalHints = totalHints; }

    public int getTotalFailedRuns() { return totalFailedRuns; }
    public void setTotalFailedRuns(int totalFailedRuns) { this.totalFailedRuns = totalFailedRuns; }

    public int getNoHintCount() { return noHintCount; }
    public void setNoHintCount(int noHintCount) { this.noHintCount = noHintCount; }

    public int getUsedHintCount() { return usedHintCount; }
    public void setUsedHintCount(int usedHintCount) { this.usedHintCount = usedHintCount; }

    public long getAvgOnTaskTime() { return avgOnTaskTime; }
    public void setAvgOnTaskTime(long avgOnTaskTime) { this.avgOnTaskTime = avgOnTaskTime; }

    public long getAvgOffTaskTime() { return avgOffTaskTime; }
    public void setAvgOffTaskTime(long avgOffTaskTime) { this.avgOffTaskTime = avgOffTaskTime; }

    // Untuk keperluan statistik dan chart
    public Map<String, Integer> getCapMap() { return capMap; }
    public void setCapMap(Map<String, Integer> capMap) { this.capMap = capMap; }

    public Map<String, Integer> getHintCaps() { return capMap; } // alias untuk eksternal
    public void setHintCaps(Map<String, Integer> hintCaps) { this.capMap = hintCaps; }

    public Map<String, Double> getAvgScorePerTask() { return avgScorePerTask; }
    public void setAvgScorePerTask(Map<String, Double> avgScorePerTask) { this.avgScorePerTask = avgScorePerTask; }

    public Map<String, Long> getOnTaskTimePerTask() { return onTaskTimePerTask; }
    public void setOnTaskTimePerTask(Map<String, Long> onTaskTimePerTask) { this.onTaskTimePerTask = onTaskTimePerTask; }

    public Map<String, Long> getOffTaskTimePerTask() { return offTaskTimePerTask; }
    public void setOffTaskTimePerTask(Map<String, Long> offTaskTimePerTask) { this.offTaskTimePerTask = offTaskTimePerTask; }
}
