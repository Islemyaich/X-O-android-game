package com.example.xo_android_game.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.xo_android_game.R;
import com.example.xo_android_game.models.Game;
import com.example.xo_android_game.models.Tournament;
import com.example.xo_android_game.utils.AIPlayer;
import com.example.xo_android_game.utils.SoundManager;

public class GameActivity extends AppCompatActivity {

    private GridLayout gameBoard;
    private TextView roundInfo, statusTV, drawsTV;
    private TextView player1ScoreTV, player2ScoreTV;
    private TextView player1NameTV, player2NameTV;
    private TextView player1SymbolTV, player2SymbolTV;
    private Button resetBtn;

    private Game currentGame;
    private Tournament tournament;
    private AIPlayer aiPlayer;
    private boolean isAIGame;
    private String aiDifficulty;
    private Button[][] cells;
    private boolean isAITurn = false;
    private SoundManager soundManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        soundManager = SoundManager.getInstance(this);
        soundManager.resumeMusic();

        initViews();
        loadGameData();
        setupGameBoard();
        updateUI();

        if (isAIGame && currentGame != null && currentGame.getCurrentPlayer() == tournament.getPlayer2Symbol()) {
            isAITurn = true;
            new Handler().postDelayed(this::makeAIMove, 500);
        }
    }

    private void initViews() {
        gameBoard = findViewById(R.id.gameBoard);
        roundInfo = findViewById(R.id.roundInfo);
        statusTV = findViewById(R.id.statusTV);
        drawsTV = findViewById(R.id.drawsTV);
        player1ScoreTV = findViewById(R.id.player1Score);
        player2ScoreTV = findViewById(R.id.player2Score);
        player1NameTV = findViewById(R.id.player1NameTV);
        player2NameTV = findViewById(R.id.player2NameTV);
        player1SymbolTV = findViewById(R.id.player1Symbol);
        player2SymbolTV = findViewById(R.id.player2Symbol);
        resetBtn = findViewById(R.id.resetBtn);

        if (resetBtn != null) {
            resetBtn.setOnClickListener(v -> resetCurrentRound());
        }
    }

    private void loadGameData() {
        String player1Name = getIntent().getStringExtra("player1");
        String player2Name = getIntent().getStringExtra("player2");
        String selectedSymbol = getIntent().getStringExtra("symbol");
        int totalRounds = getIntent().getIntExtra("rounds", 5);
        isAIGame = getIntent().getBooleanExtra("ai", false);
        aiDifficulty = getIntent().getStringExtra("difficulty");

        if (player1Name == null || player1Name.isEmpty()) player1Name = "Player 1";
        if (player2Name == null || player2Name.isEmpty()) player2Name = "Player 2";
        if (selectedSymbol == null || selectedSymbol.isEmpty()) selectedSymbol = "X";

        char player1Symbol = selectedSymbol.charAt(0);
        char player2Symbol = (player1Symbol == 'X') ? 'O' : 'X';

        tournament = new Tournament(
                player1Name,
                player2Name,
                player1Symbol,
                player2Symbol,
                totalRounds,
                isAIGame,
                aiDifficulty
        );

        if (isAIGame) {
            aiPlayer = new AIPlayer(aiDifficulty, player2Symbol, player1Symbol);
        }

        startNewRound();
    }

    private void startNewRound() {
        currentGame = new Game(tournament.getPlayer1Symbol(), tournament.getPlayer2Symbol());
        resetBoardUI();
        updateUI();
        updateStatusText();
        isAITurn = false;

        if (isAIGame && currentGame.getCurrentPlayer() == tournament.getPlayer2Symbol()) {
            isAITurn = true;
            new Handler().postDelayed(this::makeAIMove, 500);
        }
    }

    private void setupGameBoard() {
        if (gameBoard == null) return;

        gameBoard.removeAllViews();
        cells = new Button[3][3];
        gameBoard.setColumnCount(3);
        gameBoard.setRowCount(3);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                Button cell = new Button(this);

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = 0;
                params.height = 0;
                params.columnSpec = GridLayout.spec(j, 1f);
                params.rowSpec = GridLayout.spec(i, 1f);
                params.setMargins(4, 4, 4, 4);

                cell.setLayoutParams(params);
                cell.setTextSize(32);
                cell.setTextColor(getColor(R.color.white));
                cell.setBackgroundResource(R.drawable.input_bg);
                cell.setPadding(20, 20, 20, 20);

                final int row = i;
                final int col = j;
                cell.setOnClickListener(v -> onCellClick(row, col));

                gameBoard.addView(cell);
                cells[i][j] = cell;
            }
        }
    }

    private void onCellClick(int row, int col) {
        if (isAITurn || currentGame.isGameOver()) return;

        if (currentGame.makeMove(row, col)) {
            soundManager.playMoveSound();
            updateCellUI(row, col);

            if (currentGame.isGameOver()) {
                handleRoundEnd();
            } else {
                updateUI();
                updateStatusText();

                if (isAIGame && currentGame.getCurrentPlayer() == tournament.getPlayer2Symbol()) {
                    isAITurn = true;
                    new Handler().postDelayed(this::makeAIMove, 500);
                }
            }
        }
    }

    private void makeAIMove() {
        if (!isAIGame || currentGame.isGameOver() || !isAITurn) return;

        int[] move = aiPlayer.getMove(currentGame);
        if (move != null && currentGame.makeMove(move[0], move[1])) {
            soundManager.playMoveSound();
            updateCellUI(move[0], move[1]);

            if (currentGame.isGameOver()) {
                handleRoundEnd();
            } else {
                updateUI();
                updateStatusText();
                isAITurn = false;
            }
        } else {
            isAITurn = false;
        }
    }

    private void updateCellUI(int row, int col) {
        char symbol = currentGame.getBoard()[row][col];
        if (cells[row][col] != null) {
            cells[row][col].setText(String.valueOf(symbol));

            if (symbol == tournament.getPlayer1Symbol()) {
                cells[row][col].setTextColor(getColor(R.color.cyan_main));
            } else {
                cells[row][col].setTextColor(getColor(R.color.orange_main));
            }

            cells[row][col].setEnabled(false);
        }
    }

    private void handleRoundEnd() {
        String winner = currentGame.getWinner();
        tournament.updateScores(winner);
        updateUI();

        String message;
        if ("Draw".equals(winner)) {
            message = getString(R.string.round_draw);

            Toast.makeText(this, message, Toast.LENGTH_LONG).show();

            if (tournament.isTournamentComplete()) {
                new Handler().postDelayed(this::showTournamentResult, 1500);
            } else {
                new Handler().postDelayed(() -> {
                    startNewRound();
                    if (roundInfo != null) {
                        roundInfo.setText(getString(R.string.round) + " " + tournament.getCurrentRound() + " " +
                                getString(R.string.of) + " " + tournament.getTotalRounds());
                    }
                }, 1500);
            }
            return;
        }

        soundManager.playWinSound();

        char winnerSymbol = winner.charAt(0);
        String winnerName = (winnerSymbol == tournament.getPlayer1Symbol())
                ? tournament.getPlayer1Name()
                : tournament.getPlayer2Name();

        message = winnerName + " " + getString(R.string.wins_round);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

        highlightWinningCells();

        if (tournament.isTournamentComplete()) {
            new Handler().postDelayed(this::showTournamentResult, 1800);
        } else {
            new Handler().postDelayed(() -> {
                startNewRound();
                if (roundInfo != null) {
                    roundInfo.setText(getString(R.string.round) + " " + tournament.getCurrentRound() + " " +
                            getString(R.string.of) + " " + tournament.getTotalRounds());
                }
            }, 1800);
        }
    }

    private void highlightWinningCells() {
        int[][] winningCells = currentGame.getWinningCells();
        if (winningCells == null) return;

        for (int[] cellPosition : winningCells) {
            int row = cellPosition[0];
            int col = cellPosition[1];

            if (cells[row][col] != null) {
                cells[row][col].setBackgroundResource(R.drawable.winner_cell_bg);
                cells[row][col].setTextColor(getColor(R.color.white));
            }
        }
    }

    private void showTournamentResult() {
        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra("tournament", tournament);
        startActivity(intent);
        finish();
    }

    private void resetCurrentRound() {
        startNewRound();
        if (roundInfo != null) {
            roundInfo.setText(getString(R.string.round) + " " + tournament.getCurrentRound() + " " +
                    getString(R.string.of) + " " + tournament.getTotalRounds());
        }
        Toast.makeText(this, getString(R.string.round_reset), Toast.LENGTH_SHORT).show();
    }

    private void resetBoardUI() {
        if (cells == null) return;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (cells[i][j] != null) {
                    cells[i][j].setText("");
                    cells[i][j].setEnabled(true);
                    cells[i][j].setBackgroundResource(R.drawable.input_bg);
                    cells[i][j].setTextColor(getColor(R.color.white));
                }
            }
        }
    }

    private void updateUI() {
        if (player1NameTV != null) player1NameTV.setText(tournament.getPlayer1Name());
        if (player2NameTV != null) player2NameTV.setText(tournament.getPlayer2Name());
        if (player1SymbolTV != null) player1SymbolTV.setText(String.valueOf(tournament.getPlayer1Symbol()));
        if (player2SymbolTV != null) player2SymbolTV.setText(String.valueOf(tournament.getPlayer2Symbol()));
        if (player1ScoreTV != null) player1ScoreTV.setText(String.valueOf(tournament.getPlayer1Score()));
        if (player2ScoreTV != null) player2ScoreTV.setText(String.valueOf(tournament.getPlayer2Score()));
        if (drawsTV != null) drawsTV.setText(getString(R.string.draws) + ": " + tournament.getDraws());
        if (roundInfo != null) {
            roundInfo.setText(getString(R.string.round) + " " + tournament.getCurrentRound() + " " +
                    getString(R.string.of) + " " + tournament.getTotalRounds());
        }
    }

    private void updateStatusText() {
        if (statusTV == null) return;

        if (currentGame.isGameOver()) {
            statusTV.setText(getString(R.string.round_over));
        } else {
            char currentPlayer = currentGame.getCurrentPlayer();
            String playerName = (currentPlayer == tournament.getPlayer1Symbol())
                    ? tournament.getPlayer1Name()
                    : tournament.getPlayer2Name();
            statusTV.setText(playerName + " " + getString(R.string.turn));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (soundManager != null) {
            soundManager.pauseMusic();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (soundManager != null && !soundManager.isMuted()) {
            soundManager.resumeMusic();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}