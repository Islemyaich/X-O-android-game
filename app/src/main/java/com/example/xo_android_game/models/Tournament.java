package com.example.xo_android_game.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Tournament implements Serializable {
    private static final long serialVersionUID = 1L;

    private String player1Name;
    private String player2Name;
    private char player1Symbol;
    private char player2Symbol;
    private int totalRounds;
    private int currentRound;
    private int player1Score;
    private int player2Score;
    private int draws;
    private List<String> roundResults;
    private Date date;
    private boolean isAgainstAI;
    private String aiDifficulty;

    public Tournament(String player1Name, String player2Name, char player1Symbol,
                      char player2Symbol, int totalRounds, boolean isAgainstAI, String aiDifficulty) {
        this.player1Name = player1Name;
        this.player2Name = player2Name;
        this.player1Symbol = player1Symbol;
        this.player2Symbol = player2Symbol;
        this.totalRounds = totalRounds;
        this.currentRound = 1;
        this.player1Score = 0;
        this.player2Score = 0;
        this.draws = 0;
        this.roundResults = new ArrayList<>();
        this.date = new Date();
        this.isAgainstAI = isAgainstAI;
        this.aiDifficulty = aiDifficulty;
    }

    public void updateScores(String winner) {
        if (winner.equals(String.valueOf(player1Symbol))) {
            player1Score++;
        } else if (winner.equals(String.valueOf(player2Symbol))) {
            player2Score++;
        } else if (winner.equals("Draw")) {
            draws++;
        }

        roundResults.add(winner);
        currentRound++;
    }

    public boolean isTournamentComplete() {
        return currentRound > totalRounds;
    }

    public String getTournamentWinner() {
        if (player1Score > player2Score) return player1Name;
        if (player2Score > player1Score) return player2Name;
        return "Draw";
    }

    // Getters
    public String getPlayer1Name() { return player1Name; }
    public String getPlayer2Name() { return player2Name; }
    public char getPlayer1Symbol() { return player1Symbol; }
    public char getPlayer2Symbol() { return player2Symbol; }
    public int getTotalRounds() { return totalRounds; }
    public int getCurrentRound() { return currentRound; }
    public int getPlayer1Score() { return player1Score; }
    public int getPlayer2Score() { return player2Score; }
    public int getDraws() { return draws; }
    public List<String> getRoundResults() { return roundResults; }
    public Date getDate() { return date; }
    public boolean isAgainstAI() { return isAgainstAI; }
    public String getAiDifficulty() { return aiDifficulty; }
}