package com.example.xo_android_game.models;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class OnlineTournament implements Serializable {

    private String gameId;
    private String player1Id;
    private String player2Id;
    private String player1Name;
    private String player2Name;

    private int totalRounds;
    private int currentRound;
    private int player1Score;
    private int player2Score;
    private int draws;

    private Map<String, String> board;
    private String currentPlayer;

    private boolean gameOver;
    private String winner;

    private boolean tournamentComplete;
    private String tournamentWinner;

    public OnlineTournament() {}

    public OnlineTournament(String gameId, String player1Id, String player1Name, int totalRounds) {
        this.gameId = gameId;
        this.player1Id = player1Id;
        this.player1Name = player1Name;
        this.totalRounds = totalRounds;

        this.currentRound = 1;
        this.player1Score = 0;
        this.player2Score = 0;
        this.draws = 0;

        this.currentPlayer = "X";
        this.gameOver = false;
        this.tournamentComplete = false;

        initializeBoard();
    }

    private void initializeBoard() {
        board = new HashMap<>();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board.put(i + "," + j, "");
            }
        }
    }

    public boolean makeMove(int row, int col, String playerId) {
        if (gameOver || tournamentComplete) return false;

        String key = row + "," + col;
        if (!board.get(key).equals("")) return false;

        char expected = playerId.equals(player1Id) ? 'X' : 'O';
        if (currentPlayer == null || currentPlayer.charAt(0) != expected) return false;

        board.put(key, String.valueOf(expected));

        if (checkWin(row, col)) {
            gameOver = true;
            winner = currentPlayer;
            updateScores(currentPlayer);
        } else if (isBoardFull()) {
            gameOver = true;
            winner = "Draw";
            draws++;
        } else {
            currentPlayer = currentPlayer.equals("X") ? "O" : "X";
        }

        return true;
    }

    private void updateScores(String winner) {
        if (winner.equals("X")) player1Score++;
        else if (winner.equals("O")) player2Score++;
    }

    public void startNextRound() {
        if (currentRound < totalRounds) {
            currentRound++;
            initializeBoard();
            currentPlayer = "X";
            gameOver = false;
            winner = null;
        } else {
            tournamentComplete = true;
            determineWinner();
        }
    }

    private void determineWinner() {
        if (player1Score > player2Score) tournamentWinner = player1Name;
        else if (player2Score > player1Score) tournamentWinner = player2Name;
        else tournamentWinner = "Draw";
    }

    public void resetGame() {
        initializeBoard();
        currentPlayer = "X";
        gameOver = false;
        winner = null;
    }

    public void resetFullTournament() {
        currentRound = 1;
        player1Score = 0;
        player2Score = 0;
        draws = 0;
        initializeBoard();
        currentPlayer = "X";
        gameOver = false;
        winner = null;
        tournamentComplete = false;
        tournamentWinner = null;
    }

    private boolean checkWin(int r, int c) {
        char s = currentPlayer.charAt(0);

        return (get(r,0)==s && get(r,1)==s && get(r,2)==s) ||
                (get(0,c)==s && get(1,c)==s && get(2,c)==s) ||
                (r==c && get(0,0)==s && get(1,1)==s && get(2,2)==s) ||
                (r+c==2 && get(0,2)==s && get(1,1)==s && get(2,0)==s);
    }

    private char get(int r, int c) {
        String v = board.get(r + "," + c);
        return (v == null || v.isEmpty()) ? ' ' : v.charAt(0);
    }

    private boolean isBoardFull() {
        for (String v : board.values()) {
            if (v.isEmpty()) return false;
        }
        return true;
    }

    // GETTERS
    public Map<String, String> getBoard() { return board; }
    public String getCurrentPlayer() { return currentPlayer; }
    public boolean isGameOver() { return gameOver; }
    public String getWinner() { return winner; }

    public int getPlayer1Score() { return player1Score; }
    public int getPlayer2Score() { return player2Score; }
    public int getDraws() { return draws; }

    public int getCurrentRound() { return currentRound; }
    public int getTotalRounds() { return totalRounds; }

    public boolean isTournamentComplete() { return tournamentComplete; }
    public String getTournamentWinner() { return tournamentWinner; }

    public String getPlayer1Name() { return player1Name; }
    public String getPlayer2Name() { return player2Name; }

    public String getPlayer1Id() { return player1Id; }
    public String getPlayer2Id() { return player2Id; }
    public void setPlayer2Id(String id) { player2Id = id; }
    public void setPlayer2Name(String name) { player2Name = name; }
}