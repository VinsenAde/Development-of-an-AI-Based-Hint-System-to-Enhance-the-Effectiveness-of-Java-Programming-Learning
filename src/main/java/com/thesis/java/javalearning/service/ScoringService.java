////package com.thesis.java.javalearning.service;
////
////import com.thesis.java.javalearning.config.HintScoreConfig;
////import org.springframework.stereotype.Service;
////
////import java.time.Duration;
////import java.util.Collections;
////import java.util.List;
////import java.util.Map;
////
////@Service
////public class ScoringService {
////    private final HintScoreConfig cfg;
////
////    public ScoringService(HintScoreConfig cfg) {
////        this.cfg = cfg;
////    }
////
////    public static final List<String> HINT_ORDER = List.of("concept", "syntax", "logic", "step", "reveal");
////
////    public static final Map<String, Integer> HINT_LEVEL_CAP = Map.of(
////            "concept", 100,
////            "syntax", 90,
////            "logic", 80,
////            "step", 70,
////            "reveal", 55
////    );
////
////    public static String getMaxHintLevel(Map<String, Integer> hintCounts) {
////        String maxLevel = null;
////        for (String h : HINT_ORDER) {
////            if (hintCounts.getOrDefault(h, 0) > 0) {
////                maxLevel = h;
////            }
////        }
////        return maxLevel != null ? maxLevel : "none";
////    }
////
////    public int calculateScore(Map<String, Integer> hintCounts,
////                              int failedRuns,
////                              Duration timeTaken, // This parameter might become redundant for elapsedSeconds calculation
////                              String hintLevelCap,
////                              long onTaskTimeSeconds,
////                              long offTaskTimeSeconds) {
////
////        System.out.println("--- Score Calculation Details ---");
////        System.out.println("Input Parameters:");
////        System.out.println("  Hint Counts: " + hintCounts);
////        System.out.println("  Failed Runs: " + failedRuns);
////        // System.out.println("  Time Taken (Original Parameter): " + timeTaken); // Optional: keep if original duration useful for other debugging
////        System.out.println("  On-Task Time (seconds): " + onTaskTimeSeconds);
////        System.out.println("  Off-Task Time (seconds): " + offTaskTimeSeconds);
////        System.out.println("  Config Time Window Sec: " + cfg.getTimeWindowSec());
////        System.out.println("  Max Final Score Config: " + cfg.getMaxFinalScore());
////        System.out.println("---------------------------------");
////
////        boolean usedAnyHint = hintCounts.values().stream().anyMatch(c -> c > 0);
////        System.out.println("Used Any Hint: " + usedAnyHint);
////
////        int score = usedAnyHint ? cfg.getBaseScore() : 100;
////        System.out.println("Starting Score: " + score);
////
////        // 1. Hint penalty (safe null-check)
////        if (usedAnyHint) {
////            Map<String, Integer> penaltyMap = cfg.getHintPenalty() != null
////                    ? cfg.getHintPenalty()
////                    : Collections.emptyMap();
////            for (Map.Entry<String, Integer> entry : hintCounts.entrySet()) {
////                int penalty = penaltyMap.getOrDefault(entry.getKey(), 0);
////                int appliedPenalty = penalty * entry.getValue();
////                score -= appliedPenalty;
////                System.out.println("  - Hint Penalty (" + entry.getKey() + " x" + entry.getValue() + "): -" + appliedPenalty + "  -> Current Score: " + score);
////            }
////        } else {
////            System.out.println("  - No Hint Penalty applied (no hints used).");
////        }
////
////
////        // 2. Failed run penalty
////        int failedRunPenalty = cfg.getScorePerFailedRun() * failedRuns;
////        score -= failedRunPenalty;
////        System.out.println("  - Failed Run Penalty (" + failedRuns + " runs x " + cfg.getScorePerFailedRun() + "): -" + failedRunPenalty + "  -> Current Score: " + score);
////
////
////        // 3. On-task time penalty: -1 point every 2 minutes
////        long onTaskMinutes = onTaskTimeSeconds / 60;
////        long penaltyOnTask = onTaskMinutes / 2;
////        score -= penaltyOnTask;
////        System.out.println("  - On-Task Time Penalty (" + onTaskMinutes + " min / 2): -" + penaltyOnTask + "  -> Current Score: " + score);
////
////
////        // 4. Off-task time penalty: -6 points every 1 minute
////        long offTaskMinutes = offTaskTimeSeconds / 60;
////        long penaltyOffTask = offTaskMinutes * 6;
////        score -= penaltyOffTask;
////        System.out.println("  - Off-Task Time Penalty (" + offTaskMinutes + " min x 6): -" + penaltyOffTask + "  -> Current Score: " + score);
////
////
////        // 5. Bonus if no hints used
////        if (!usedAnyHint) {
////            score += cfg.getZeroHintBonus();
////            System.out.println("  + Zero Hint Bonus: +" + cfg.getZeroHintBonus() + "  -> Current Score: " + score);
////        } else {
////            System.out.println("  - No Zero Hint Bonus applied (hints were used).");
////        }
////
////
////        // 6. Fast finish bonus
////        long elapsedSeconds = onTaskTimeSeconds + offTaskTimeSeconds;
////        System.out.println("  Calculated Total Elapsed Seconds: " + elapsedSeconds + " (Threshold: " + (cfg.getTimeWindowSec() / 2) + "s)");
////        if (elapsedSeconds < cfg.getTimeWindowSec() / 2) {
////            score += cfg.getFastFinishBonus();
////            System.out.println("  + Fast Finish Bonus: +" + cfg.getFastFinishBonus() + "  -> Current Score: " + score);
////        } else {
////            System.out.println("  - No Fast Finish Bonus applied (time " + elapsedSeconds + "s >= " + (cfg.getTimeWindowSec() / 2) + "s).");
////        }
////
////        System.out.println("Score before applying final caps: " + score);
////
////        // 7. Cap score based on highest hint level used
////        if (hintLevelCap != null && HINT_LEVEL_CAP.containsKey(hintLevelCap)) {
////            int capValue = HINT_LEVEL_CAP.get(hintLevelCap);
////            int scoreBeforeCap = score;
////            score = Math.min(score, capValue);
////            if (scoreBeforeCap != score) {
////                System.out.println("  * Applied Hint Level Cap (" + hintLevelCap + " max " + capValue + "): Reduced from " + scoreBeforeCap + " to " + score);
////            } else {
////                System.out.println("  - Hint Level Cap (" + hintLevelCap + " max " + capValue + ") not applied (score already below cap).");
////            }
////        } else {
////            System.out.println("  - No Hint Level Cap applied (no hints or level not found).");
////        }
////
////        // Enforce maximum score cap (e.g., 100) and minimum score (0)
////        int scoreBeforeOverallCap = score;
////        score = Math.min(score, cfg.getMaxFinalScore());
////        if (scoreBeforeOverallCap != score) {
////             System.out.println("  * Applied Max Final Score Cap (" + cfg.getMaxFinalScore() + "): Reduced from " + scoreBeforeOverallCap + " to " + score);
////        }
////
////        int scoreBeforeMinCap = score;
////        score = Math.max(0, score);
////        if (scoreBeforeMinCap != score) {
////            System.out.println("  * Applied Min Score Cap (0): Increased from " + scoreBeforeMinCap + " to " + score);
////        }
////
////
////        System.out.println("--- Final Score: " + score + " ---");
////        System.out.println("---------------------------------");
////
////        return score;
////    }
////}
//
//package com.thesis.java.javalearning.service;
//
//import com.thesis.java.javalearning.config.HintScoreConfig;
//import org.springframework.stereotype.Service;
//
//import java.time.Duration; // Still needed for other potential uses if any, but not for elapsedSeconds in calculateScore
//import java.util.Collections;
//import java.util.List;
//import java.util.Map;
//
//@Service
//public class ScoringService {
//    private final HintScoreConfig cfg;
//
//    public ScoringService(HintScoreConfig cfg) {
//        this.cfg = cfg;
//    }
//
//    public static final List<String> HINT_ORDER = List.of("concept", "syntax", "logic", "step", "reveal");
//
//    public static final Map<String, Integer> HINT_LEVEL_CAP = Map.of(
//            "concept", 100,
//            "syntax", 90,
//            "logic", 80,
//            "step", 70,
//            "reveal", 55
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
//    public int calculateScore(Map<String, Integer> hintCounts,
//                              int failedRuns,
//                              // Duration timeTaken, // REMOVED: No longer needed for elapsedSeconds calculation
//                              String hintLevelCap,
//                              long onTaskTimeSeconds,
//                              long offTaskTimeSeconds) {
//        
//
//    System.out.println("--- Calculating Score ---");
//    System.out.println("Hint Counts: " + hintCounts);
//    System.out.println("Failed Runs: " + failedRuns);
//    // System.out.println("Time Taken (Duration): " + timeTaken); // REMOVED corresponding print
//    System.out.println("On-Task Time (seconds): " + onTaskTimeSeconds);
//    System.out.println("Off-Task Time (seconds): " + offTaskTimeSeconds);
//    System.out.println("Config Time Window Sec: " + cfg.getTimeWindowSec());
//
//
//    boolean usedAnyHint = hintCounts.values().stream().anyMatch(c -> c > 0);
//    System.out.println("Used Any Hint: " + usedAnyHint);
//
//    int score = usedAnyHint ? cfg.getBaseScore() : 100;
//    System.out.println("Initial Score (after base/no hint check): " + score);
//
//
//        // 1. Hint penalty (safe null-check)
//        if (usedAnyHint) {
//            Map<String, Integer> penaltyMap = cfg.getHintPenalty() != null
//                    ? cfg.getHintPenalty()
//                    : Collections.emptyMap();
//            for (Map.Entry<String, Integer> entry : hintCounts.entrySet()) {
//                int penalty = penaltyMap.getOrDefault(entry.getKey(), 0);
//                score -= penalty * entry.getValue();
//            }
//        }
//
//        // 2. Failed run penalty
//        score -= cfg.getScorePerFailedRun() * failedRuns;
//
//        // 3. On-task time penalty: -1 point every 2 minutes
//        long onTaskMinutes = onTaskTimeSeconds / 60;
//        long penaltyOnTask = onTaskMinutes / 2;
//        score -= penaltyOnTask;
//
//        // 4. Off-task time penalty: -6 points every 1 minute
//        long offTaskMinutes = offTaskTimeSeconds / 60;
//        long penaltyOffTask = offTaskMinutes * 6;
//        score -= penaltyOffTask;
//
//        // 5. Bonus if no hints used
//        if (!usedAnyHint) {
//                   score += cfg.getZeroHintBonus(); // Optional bonus
//        System.out.println("Zero Hint Bonus Applied. New Score: " + score);
//        }
//
//        // 6. Fast finish bonus
//    // MODIFIED: Calculate elapsedSeconds from onTaskTimeSeconds and offTaskTimeSeconds
//    long elapsedSeconds = onTaskTimeSeconds + offTaskTimeSeconds; 
//    System.out.println("Elapsed Seconds (On-Task + Off-Task): " + elapsedSeconds);
//    if (elapsedSeconds < cfg.getTimeWindowSec() / 2) {
//        score += cfg.getFastFinishBonus();
//        System.out.println("Fast Finish Bonus Applied. New Score: " + score);
//    }
//    System.out.println("Score before final cap: " + score);
//
//        // 7. Cap score based on highest hint level used
//        if (hintLevelCap != null && HINT_LEVEL_CAP.containsKey(hintLevelCap)) {
//            score = Math.min(score, HINT_LEVEL_CAP.get(hintLevelCap));
//        }
//
//        // Enforce maximum score cap (e.g., 100) and minimum score (0)
//        score = Math.min(score, cfg.getMaxFinalScore());
//    score = Math.max(0, score);
//    System.out.println("Final Score: " + score);
//    System.out.println("-------------------------");
//
//    return score;
//    }
//}

package com.thesis.java.javalearning.service;

import com.thesis.java.javalearning.config.HintScoreConfig;
import org.springframework.stereotype.Service;

import java.time.Duration; // Still imported, but not used in calculateScore method parameters
import java.util.Collections;
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
                              // Duration timeTaken, // This parameter remains removed
                              String hintLevelCap,
                              long onTaskTimeSeconds,
                              long offTaskTimeSeconds) {

        System.out.println("--- Score Calculation Details ---");
        System.out.println("Input Parameters:");
        System.out.println("  Hint Counts: " + hintCounts);
        System.out.println("  Failed Runs: " + failedRuns);
        System.out.println("  On-Task Time (seconds): " + onTaskTimeSeconds);
        System.out.println("  Off-Task Time (seconds): " + offTaskTimeSeconds);
        System.out.println("  Config Time Window Sec: " + cfg.getTimeWindowSec());
        System.out.println("  Max Final Score Config: " + cfg.getMaxFinalScore());
        System.out.println("---------------------------------");

        boolean usedAnyHint = hintCounts.values().stream().anyMatch(c -> c > 0);
        System.out.println("Used Any Hint: " + usedAnyHint);

        int score = usedAnyHint ? cfg.getBaseScore() : 100;
        System.out.println("Starting Score: " + score);

        // 1. Hint penalty
        if (usedAnyHint) {
            Map<String, Integer> penaltyMap = cfg.getHintPenalty() != null
                    ? cfg.getHintPenalty()
                    : Collections.emptyMap();
            for (Map.Entry<String, Integer> entry : hintCounts.entrySet()) {
                int penalty = penaltyMap.getOrDefault(entry.getKey(), 0);
                int appliedPenalty = penalty * entry.getValue();
                score -= appliedPenalty;
                System.out.println("  - Hint Penalty (" + entry.getKey() + " x" + entry.getValue() + "): -" + appliedPenalty + "  -> Current Score: " + score);
            }
        } else {
            System.out.println("  - No Hint Penalty applied (no hints used).");
        }


        // 2. Failed run penalty
        int failedRunPenalty = cfg.getScorePerFailedRun() * failedRuns;
        score -= failedRunPenalty;
        System.out.println("  - Failed Run Penalty (" + failedRuns + " runs x " + cfg.getScorePerFailedRun() + "): -" + failedRunPenalty + "  -> Current Score: " + score);


        // 3. On-task time penalty: -1 point every 2 minutes
        long onTaskMinutes = onTaskTimeSeconds / 60;
        long penaltyOnTask = onTaskMinutes / 2;
        score -= penaltyOnTask;
        System.out.println("  - On-Task Time Penalty (" + onTaskTimeSeconds + " seconds ~ " + onTaskMinutes + " full minutes / 2): -" + penaltyOnTask + "  -> Current Score: " + score);


        // 4. Off-task time penalty: -6 points every 1 minute
        long offTaskMinutes = offTaskTimeSeconds / 60;
        long penaltyOffTask = offTaskMinutes * 6;
        score -= penaltyOffTask;
        System.out.println("  - Off-Task Time Penalty (" + offTaskTimeSeconds + " seconds ~ " + offTaskMinutes + " full minutes x 6): -" + penaltyOffTask + "  -> Current Score: " + score);


        // 5. Bonus if no hints used
        if (!usedAnyHint) {
            score += cfg.getZeroHintBonus();
            System.out.println("  + Zero Hint Bonus: +" + cfg.getZeroHintBonus() + "  -> Current Score: " + score);
        } else {
            System.out.println("  - No Zero Hint Bonus applied (hints were used).");
        }


        // 6. Fast finish bonus
        long elapsedSeconds = onTaskTimeSeconds + offTaskTimeSeconds; // Correctly calculated from on-task and off-task times
        System.out.println("  Calculated Total Elapsed Seconds: " + elapsedSeconds + " (Threshold: " + (cfg.getTimeWindowSec() / 2) + "s)");
        if (elapsedSeconds < cfg.getTimeWindowSec() / 2) {
            score += cfg.getFastFinishBonus();
            System.out.println("  + Fast Finish Bonus: +" + cfg.getFastFinishBonus() + "  -> Current Score: " + score);
        } else {
            System.out.println("  - No Fast Finish Bonus applied (time " + elapsedSeconds + "s >= " + (cfg.getTimeWindowSec() / 2) + "s).");
        }

        System.out.println("Score before applying final caps: " + score);

        // 7. Cap score based on highest hint level used
        if (hintLevelCap != null && HINT_LEVEL_CAP.containsKey(hintLevelCap)) {
            int capValue = HINT_LEVEL_CAP.get(hintLevelCap);
            int scoreBeforeCap = score;
            score = Math.min(score, capValue);
            if (scoreBeforeCap != score) {
                System.out.println("  * Applied Hint Level Cap (" + hintLevelCap + " max " + capValue + "): Reduced from " + scoreBeforeCap + " to " + score);
            } else {
                System.out.println("  - Hint Level Cap (" + hintLevelCap + " max " + capValue + ") not applied (score already below cap).");
            }
        } else {
            System.out.println("  - No Hint Level Cap applied (no hints or level not found).");
        }

        // Enforce maximum score cap (e.g., 100) and minimum score (0)
        int scoreBeforeOverallCap = score;
        score = Math.min(score, cfg.getMaxFinalScore());
        if (scoreBeforeOverallCap != score) {
             System.out.println("  * Applied Max Final Score Cap (" + cfg.getMaxFinalScore() + "): Reduced from " + scoreBeforeOverallCap + " to " + score);
        }

        int scoreBeforeMinCap = score;
        score = Math.max(0, score);
        if (scoreBeforeMinCap != score) {
            System.out.println("  * Applied Min Score Cap (0): Increased from " + scoreBeforeMinCap + " to " + score);
        }


        System.out.println("--- Final Score: " + score + " ---");
        System.out.println("---------------------------------");

        return score;
    }
}