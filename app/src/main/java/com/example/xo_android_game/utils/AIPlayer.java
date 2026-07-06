package com.example.xo_android_game.utils;

import com.example.xo_android_game.models.Game;
import java.util.Random;

public class AIPlayer {
    private String difficulty;
    private char aiSymbol;
    private char playerSymbol;
    private Random random;

    public AIPlayer(String difficulty, char aiSymbol, char playerSymbol) {
        this.difficulty = difficulty;
        this.aiSymbol = aiSymbol;
        this.playerSymbol = playerSymbol;
        this.random = new Random();
    }

    public int[] getMove(Game game) {
        switch (difficulty) {
            case "Easy":
                return getRandomMove(game);
            case "Medium":
                return getMediumMove(game);
            case "Hard":
                return getBestMove(game);
            default:
                return getRandomMove(game);
        }
    }

    private int[] getRandomMove(Game game) {
        char[][] board = game.getBoard();
        java.util.ArrayList<int[]> emptyCells = new java.util.ArrayList<>();

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == ' ') {
                    emptyCells.add(new int[]{i, j});
                }
            }
        }

        if (!emptyCells.isEmpty()) {
            return emptyCells.get(random.nextInt(emptyCells.size()));
        }
        return null;
    }

    private int[] getMediumMove(Game game) {
        // 70% chance of best move, 30% random
        if (random.nextInt(100) < 70) {
            return getBestMove(game);
        }
        return getRandomMove(game);
    }

    private int[] getBestMove(Game game) {
        char[][] board = game.getBoard();

        // Try to win
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == ' ') {
                    board[i][j] = aiSymbol;
                    if (checkWin(board, aiSymbol, i, j)) {
                        board[i][j] = ' ';
                        return new int[]{i, j};
                    }
                    board[i][j] = ' ';
                }
            }
        }

        // Block player from winning
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == ' ') {
                    board[i][j] = playerSymbol;
                    if (checkWin(board, playerSymbol, i, j)) {
                        board[i][j] = ' ';
                        return new int[]{i, j};
                    }
                    board[i][j] = ' ';
                }
            }
        }

        // Take center
        if (board[1][1] == ' ') return new int[]{1, 1};

        // Take corners
        int[][] corners = {{0,0}, {0,2}, {2,0}, {2,2}};
        for (int[] corner : corners) {
            if (board[corner[0]][corner[1]] == ' ') {
                return corner;
            }
        }

        // Random move
        return getRandomMove(game);
    }

    private boolean checkWin(char[][] board, char symbol, int row, int col) {
        // Check row
        if (board[row][0] == symbol && board[row][1] == symbol && board[row][2] == symbol)
            return true;

        // Check column
        if (board[0][col] == symbol && board[1][col] == symbol && board[2][col] == symbol)
            return true;

        // Check diagonals
        if (row == col && board[0][0] == symbol && board[1][1] == symbol && board[2][2] == symbol)
            return true;

        if (row + col == 2 && board[0][2] == symbol && board[1][1] == symbol && board[2][0] == symbol)
            return true;

        return false;
    }
}