package com.thesis.java.javalearning.service;

import com.thesis.java.javalearning.config.HintScoreConfig;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class ScoringService {
    private final HintScoreConfig cfg;

    public ScoringService(HintScoreConfig cfg) {
        this.cfg = cfg;
    }

    public static final List<String> HINT_ORDER = List.of("concept", "syntax", "logic", "step", "reveal");
    public static final Map<String, Integer> HINT_LEVEL_CAP = Map.of(
            "concept", 100,
            "syntax", 90,
            "logic", 80,
            "step", 70,
            "reveal", 55
    );

    public static String getMaxHintLevel(Map<String, Integer> hintCounts) {
        String maxLevel = null;
        for (String h : HINT_ORDER) {
            if (hintCounts.getOrDefault(h, 0) > 0) {
                maxLevel = h;
            }
        }
        return maxLevel != null ? maxLevel : "none";
    }

    public int calculateScore(Map<String, Integer> hintCounts,
                              int failedRuns,
                              Duration timeTaken,
                              String hintLevelCap) {
        int score = cfg.getBaseScore();

        // Penalti hint
        for (Map.Entry<String, Integer> entry : hintCounts.entrySet()) {
            int penalty = cfg.getHintPenalty().getOrDefault(entry.getKey(), 0);
            score -= penalty * entry.getValue();
        }

        // Penalti run gagal
        score -= cfg.getScorePerFailedRun() * failedRuns;

        // Bonus tanpa hint
        boolean used = hintCounts.values().stream().anyMatch(c -> c > 0);
        if (!used) {
            score += cfg.getZeroHintBonus();
        }

        // Penalti per 1 menit
        long over = Math.max(0, timeTaken.getSeconds());
        score -= (int) (over / cfg.getTimeWindowSec()) * cfg.getScorePerTimeWindow();

        // Bonus penyelesaian cepat (<30 detik)
        if (timeTaken.getSeconds() < (cfg.getTimeWindowSec() / 2)) {
            score += cfg.getFastFinishBonus();
        }

        // Hint cap
        if (hintLevelCap != null && HINT_LEVEL_CAP.containsKey(hintLevelCap)) {
            score = Math.min(score, HINT_LEVEL_CAP.get(hintLevelCap));
        }

        return Math.max(0, Math.min(100, score));
    }
}

/////// src/main/java/com/thesis/java/javalearning/service/ScoringService.java



//package com.thesis.java.javalearning.service;
//
//import java.time.Duration;
//import java.util.Map;
//
//import org.springframework.stereotype.Service;
//import com.thesis.java.javalearning.config.HintScoreConfig;
//
//import java.util.*;
//@Service
//public class ScoringService {
//    private final HintScoreConfig cfg;
//    public ScoringService(HintScoreConfig cfg) {
//        this.cfg = cfg;
//    }
//
//    public static final List<String> HINT_ORDER = List.of("concept", "syntax", "logic", "step", "reveal");
//    public static final Map<String,Integer> HINT_LEVEL_CAP = Map.of(
//        "concept", 100,
//        "syntax", 90,
//        "logic", 80,
//        "step", 70,
//        "reveal", 55
//    );
//
//    // Cek hint tertinggi yang dipakai
//    public static String getMaxHintLevel(Map<String, Integer> hintCounts) {
//        String maxLevel = null;
//        for (String h : HINT_ORDER) {
//            if (hintCounts.getOrDefault(h, 0) > 0) {
//                maxLevel = h;
//            }
//        }
//        return maxLevel != null ? maxLevel : "none";
//    }
//       public int calculateScore(Map<String,Integer> hintCounts,
//                              int failedRuns,
//                              Duration timeTaken,
//                              String hintLevelCap) {
//        int score = cfg.getBaseScore();
//
//        // 2) penalti hint
//        for (Map.Entry<String, Integer> entry : hintCounts.entrySet()) {
//            int penalty = cfg.getHintPenalty().getOrDefault(entry.getKey(), 0);
//            score -= penalty * entry.getValue();
//        }
//
//        // 3) penalti run gagal
//        score -= cfg.getScorePerFailedRun() * failedRuns;
//
//        // 4) bonus jika tanpa hint
//        boolean used = hintCounts.values().stream().anyMatch(c -> c > 0);
//        if (!used) {
//            score += cfg.getZeroHintBonus();
//        }
//
//        // 5) penalti/bonus waktu
//        long secs = timeTaken.getSeconds();
//        long over = Math.max(0, secs - cfg.getTimeWindowSec());
//        score -= (int)(over / cfg.getTimeWindowSec()) * cfg.getScorePerTimeWindow();
//        if (secs < cfg.getTimeWindowSec() / 2) {
//            score += cfg.getFastFinishBonus();
//        }
//
//        // 6) SCORING CAP: cek hintLevelCap
//        if (hintLevelCap != null && HINT_LEVEL_CAP.containsKey(hintLevelCap)) {
//            score = Math.min(score, HINT_LEVEL_CAP.get(hintLevelCap));
//        }
//
//        // Clamp ke 0-100
//        score = Math.max(0, Math.min(100, score));
//        return score;
//    }
//}
//package com.thesis.java.javalearning.service;
//
//import java.time.Duration;
//import java.util.Map;
//import java.util.List;
//import java.util.HashMap;
//
//import org.springframework.stereotype.Service;
//import com.thesis.java.javalearning.config.HintScoreConfig;
//
//@Service
//public class ScoringService {
//
//    private final HintScoreConfig cfg;
//
//    public ScoringService(HintScoreConfig cfg) {
//        this.cfg = cfg;
//    }
//
//    public static final List<String> HINT_ORDER = List.of("concept", "syntax", "logic", "step", "reveal");
//
//    public static final Map<String,Integer> HINT_LEVEL_CAP = Map.of(
//        "concept", 100,
//        "syntax", 90,
//        "logic", 80,
//        "step", 70,
//        "reveal", 55
//    );
//
//    public static String getMaxHintLevel(Map<String, Integer> hintCounts) {
//        String maxLevel = null;
//        for (String h : HINT_ORDER) {
//            if (hintCounts.getOrDefault(h, 0) > 0) {
//                maxLevel = h;
//            }
//        }
//        return maxLevel != null ? maxLevel : "none";
//    }
//
//    public int calculateScore(Map<String,Integer> hintCounts,
//                              int failedRuns,
//                              Duration timeTaken,
//                              String hintLevelCap) {
//
//        int score = cfg.getBaseScore(); // 1. Base Score
//
//        // 2. Penalti hint
//        for (Map.Entry<String, Integer> entry : hintCounts.entrySet()) {
//            int penalty = cfg.getHintPenalty().getOrDefault(entry.getKey(), 0);
//            score -= penalty * entry.getValue();
//        }
//
//        // 3. Penalti run gagal
//        score -= cfg.getScorePerFailedRun() * failedRuns;
//
//        // 4. Bonus jika tidak pakai hint sama sekali
//        boolean usedAnyHint = hintCounts.values().stream().anyMatch(c -> c > 0);
//        if (!usedAnyHint) {
//            score += cfg.getZeroHintBonus();
//        }
//
//        // 5. Bonus waktu cepat (< setengah time window)
//        long elapsedSeconds = timeTaken.getSeconds();
//        if (elapsedSeconds < cfg.getTimeWindowSec() / 2) {
//            score += cfg.getFastFinishBonus();
//        }
//
//        // 6. Penalti berdasarkan waktu pengerjaan (1 poin per menit)
//        long totalMinutes = Math.max(1, timeTaken.toMinutes());
//        score -= totalMinutes;
//
//        // 7. Batas maksimum skor berdasarkan hint level tertinggi
//        if (hintLevelCap != null && HINT_LEVEL_CAP.containsKey(hintLevelCap)) {
//            score = Math.min(score, HINT_LEVEL_CAP.get(hintLevelCap));
//        }
//
//        // 8. Clamp skor agar tetap dalam 0–100
//        score = Math.max(0, Math.min(100, score));
//
//        return score;
//    }
//}
