package com.thesis.java.javalearning.config;

import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "hint.scoring")
public class HintScoreConfig {
    private Map<String, Integer> hintPenalty;
    private int baseScore;
    private int zeroHintBonus;
    private int scorePerFailedRun;
    private int timeWindowSec;
    private int scorePerTimeWindow;
    private int fastFinishBonus;

    public Map<String, Integer> getHintPenalty() { return hintPenalty; }
    public void setHintPenalty(Map<String, Integer> hintPenalty) { this.hintPenalty = hintPenalty; }

    public int getBaseScore() { return baseScore; }
    public void setBaseScore(int baseScore) { this.baseScore = baseScore; }

    public int getZeroHintBonus() { return zeroHintBonus; }
    public void setZeroHintBonus(int zeroHintBonus) { this.zeroHintBonus = zeroHintBonus; }

    public int getScorePerFailedRun() { return scorePerFailedRun; }
    public void setScorePerFailedRun(int scorePerFailedRun) { this.scorePerFailedRun = scorePerFailedRun; }

    public int getTimeWindowSec() { return timeWindowSec; }
    public void setTimeWindowSec(int timeWindowSec) { this.timeWindowSec = timeWindowSec; }

    public int getScorePerTimeWindow() { return scorePerTimeWindow; }
    public void setScorePerTimeWindow(int scorePerTimeWindow) { this.scorePerTimeWindow = scorePerTimeWindow; }

    public int getFastFinishBonus() { return fastFinishBonus; }
    public void setFastFinishBonus(int fastFinishBonus) { this.fastFinishBonus = fastFinishBonus; }
}
