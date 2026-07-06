package com.example.xo_android_game.models;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class OnlineGame implements Serializable {
    private String gameId;
    private String player1Id;
    private String player2Id;
    private Map<String, String> board;
    private String currentPlayer;
    private boolean gameOver;
    private String winner;

    // Empty constructor for Firebase
    public OnlineGame() {
    }

    public OnlineGame(String gameId, String player1Id) {
        this.gameId = gameId;
        this.player1Id = player1Id;
        this.board = new HashMap<>();
        this.currentPlayer = "X";
        this.gameOver = false;
        this.winner = null;
        initializeBoard();
    }

    private void initializeBoard() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board.put(i + "," + j, "");
            }
        }
    }

    public boolean makeMove(int row, int col, String playerId) {
        if (gameOver) return false;

        String cellKey = row + "," + col;
        if (!board.get(cellKey).equals("")) return false;

        char expectedPlayer = (playerId.equals(player1Id)) ? 'X' : 'O';
        if (currentPlayer.charAt(0) != expectedPlayer) return false;

        // Make the move
        board.put(cellKey, String.valueOf(currentPlayer.charAt(0)));

        // Check win
        if (checkWin(row, col)) {
            gameOver = true;
            winner = currentPlayer;
        } else if (isBoardFull()) {
            gameOver = true;
            winner = "Draw";
        } else {
            // Switch player
            currentPlayer = currentPlayer.equals("X") ? "O" : "X";
        }

        return true;
    }

    private boolean checkWin(int row, int col) {
        char symbol = currentPlayer.charAt(0);

        // Check row
        if (getCell(row, 0) == symbol && getCell(row, 1) == symbol && getCell(row, 2) == symbol)
            return true;
        // Check column
        if (getCell(0, col) == symbol && getCell(1, col) == symbol && getCell(2, col) == symbol)
            return true;
        // Check main diagonal
        if (row == col && getCell(0, 0) == symbol && getCell(1, 1) == symbol && getCell(2, 2) == symbol)
            return true;
        // Check anti-diagonal
        if (row + col == 2 && getCell(0, 2) == symbol && getCell(1, 1) == symbol && getCell(2, 0) == symbol)
            return true;
        return false;
    }

    private char getCell(int row, int col) {
        String value = board.get(row + "," + col);
        if (value == null || value.isEmpty()) return ' ';
        return value.charAt(0);
    }

    private boolean isBoardFull() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (getCell(i, j) == ' ') return false;
            }
        }
        return true;
    }

    public void resetGame() {
        initializeBoard();
        currentPlayer = "X";
        gameOver = false;
        winner = null;
    }

    // Getters and Setters
    public String getGameId() { return gameId; }
    public void setGameId(String gameId) { this.gameId = gameId; }
    public String getPlayer1Id() { return player1Id; }
    public void setPlayer1Id(String player1Id) { this.player1Id = player1Id; }
    public String getPlayer2Id() { return player2Id; }
    public void setPlayer2Id(String player2Id) { this.player2Id = player2Id; }
    public Map<String, String> getBoard() { return board; }
    public void setBoard(Map<String, String> board) { this.board = board; }
    public String getCurrentPlayer() { return currentPlayer; }
    public void setCurrentPlayer(String currentPlayer) { this.currentPlayer = currentPlayer; }
    public boolean isGameOver() { return gameOver; }
    public void setGameOver(boolean gameOver) { this.gameOver = gameOver; }
    public String getWinner() { return winner; }
    public void setWinner(String winner) { this.winner = winner; }
}