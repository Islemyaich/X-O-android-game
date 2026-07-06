package com.example.xo_android_game.models;

public class Game {
    private char[][] board;
    private char currentPlayer;
    private char player1Symbol;
    private char player2Symbol;
    private boolean gameOver;
    private String winner;

    private int[][] winningCells; // 3 cells -> { {r,c}, {r,c}, {r,c} }

    public Game(char player1Symbol, char player2Symbol) {
        this.player1Symbol = player1Symbol;
        this.player2Symbol = player2Symbol;
        this.currentPlayer = player1Symbol;
        this.board = new char[3][3];
        this.gameOver = false;
        this.winner = null;
        this.winningCells = null;
        initializeBoard();
    }

    private void initializeBoard() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = ' ';
            }
        }
    }

    public boolean makeMove(int row, int col) {
        if (gameOver || row < 0 || row > 2 || col < 0 || col > 2 || board[row][col] != ' ') {
            return false;
        }

        board[row][col] = currentPlayer;

        if (checkWin(row, col)) {
            gameOver = true;
            winner = String.valueOf(currentPlayer);
        } else if (isBoardFull()) {
            gameOver = true;
            winner = "Draw";
        } else {
            switchPlayer();
        }

        return true;
    }

    private boolean checkWin(int row, int col) {
        // Check row
        if (board[row][0] == currentPlayer && board[row][1] == currentPlayer && board[row][2] == currentPlayer) {
            winningCells = new int[][]{
                    {row, 0}, {row, 1}, {row, 2}
            };
            return true;
        }

        // Check column
        if (board[0][col] == currentPlayer && board[1][col] == currentPlayer && board[2][col] == currentPlayer) {
            winningCells = new int[][]{
                    {0, col}, {1, col}, {2, col}
            };
            return true;
        }

        // Main diagonal
        if (row == col && board[0][0] == currentPlayer && board[1][1] == currentPlayer && board[2][2] == currentPlayer) {
            winningCells = new int[][]{
                    {0, 0}, {1, 1}, {2, 2}
            };
            return true;
        }

        // Secondary diagonal
        if (row + col == 2 && board[0][2] == currentPlayer && board[1][1] == currentPlayer && board[2][0] == currentPlayer) {
            winningCells = new int[][]{
                    {0, 2}, {1, 1}, {2, 0}
            };
            return true;
        }

        return false;
    }

    private boolean isBoardFull() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == ' ') return false;
            }
        }
        return true;
    }

    private void switchPlayer() {
        currentPlayer = (currentPlayer == player1Symbol) ? player2Symbol : player1Symbol;
    }

    public char[][] getBoard() { return board; }
    public char getCurrentPlayer() { return currentPlayer; }
    public boolean isGameOver() { return gameOver; }
    public String getWinner() { return winner; }
    public int[][] getWinningCells() { return winningCells; }
}