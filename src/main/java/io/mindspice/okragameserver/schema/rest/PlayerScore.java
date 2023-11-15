package io.mindspice.okragameserver.schema.rest;

import java.math.BigDecimal;
import java.math.RoundingMode;


public class PlayerScore {
    public final String playerName;
    public int wins = 0;
    public int losses = 0;

    public PlayerScore(String playerName) {
        this.playerName = playerName;
    }

    public double getWinRatio() {
        return new BigDecimal((double) wins / losses).setScale(3, RoundingMode.HALF_UP).doubleValue();
    }

    public void addResult(boolean isWin) {
        if (isWin) { wins++; } else { losses++; }
    }

    public String toString() {
        return "Wins: " + wins + " | Losses: " + losses + " | Win Ratio: " + getWinRatio();
    }
}
