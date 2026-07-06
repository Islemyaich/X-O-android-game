package com.example.xo_android_game.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.xo_android_game.R;
import com.example.xo_android_game.models.OnlineTournament;
import com.example.xo_android_game.utils.SoundManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class OnlineTournamentActivity extends AppCompatActivity {

    private GridLayout gameBoard;
    private TextView statusTV, roomCodeTV, roundInfoTV;
    private TextView player1NameTV, player2NameTV;
    private TextView player1ScoreTV, player2ScoreTV, drawsTV;
    private Button leaveBtn;
    private Button[][] cells;

    private OnlineTournament tournament;
    private DatabaseReference gameRef;
    private String gameId, playerId, playerName;
    private char playerSymbol;
    private boolean isMyTurn = false;
    private boolean waitingForNextRound = false;
    private boolean isHost = false;
    private boolean tournamentResultShown = false;
    private boolean nextRoundScheduled = false;

    private SoundManager soundManager;
    private ValueEventListener gameListener;

    private static final String TAG = "OnlineTournament";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_tournament);

        soundManager = SoundManager.getInstance(this);
        if (soundManager != null) soundManager.resumeMusic();

        initViews();
        setupGameBoard();
        setupButtons();

        gameId = getIntent().getStringExtra("gameId");
        playerId = UUID.randomUUID().toString();
        playerName = getIntent().getStringExtra("playerName");
        int totalRounds = getIntent().getIntExtra("totalRounds", 5);

        if (totalRounds != 5 && totalRounds != 10 && totalRounds != 15) totalRounds = 5;

        if (playerName == null || playerName.isEmpty()) playerName = "Player";

        if (gameId == null) {
            isHost = true;
            createNewTournament(totalRounds);
        } else {
            joinTournament(gameId, playerName);
        }
    }

    private void initViews() {
        gameBoard = findViewById(R.id.gameBoard);
        statusTV = findViewById(R.id.statusTV);
        roomCodeTV = findViewById(R.id.roomCodeTV);
        roundInfoTV = findViewById(R.id.roundInfoTV);
        player1NameTV = findViewById(R.id.player1NameTV);
        player2NameTV = findViewById(R.id.player2NameTV);
        player1ScoreTV = findViewById(R.id.player1ScoreTV);
        player2ScoreTV = findViewById(R.id.player2ScoreTV);
        drawsTV = findViewById(R.id.drawsTV);
        leaveBtn = findViewById(R.id.leaveBtn);
    }

    private void createNewTournament(int totalRounds) {
        gameId = generateRoomCode();
        tournament = new OnlineTournament(gameId, playerId, playerName, totalRounds);
        playerSymbol = 'X';

        gameRef = FirebaseDatabase.getInstance().getReference()
                .child("online_tournaments").child(gameId);

        gameRef.setValue(tournament)
                .addOnSuccessListener(aVoid -> runOnUiThread(() -> {
                    roomCodeTV.setText(getString(R.string.room_code, gameId));
                    statusTV.setText(getString(R.string.waiting_for_opponent_to_join));
                    updateScoreUI();
                    updateRoundInfo();
                    listenForGameUpdates();
                    Toast.makeText(this, getString(R.string.game_created, gameId), Toast.LENGTH_LONG).show();
                }))
                .addOnFailureListener(e -> runOnUiThread(() -> {
                    Toast.makeText(this, getString(R.string.error_prefix) + e.getMessage(), Toast.LENGTH_LONG).show();
                    finish();
                }));
    }

    private void joinTournament(String roomCode, String name) {
        gameId = roomCode.toUpperCase();

        gameRef = FirebaseDatabase.getInstance().getReference()
                .child("online_tournaments").child(gameId);

        gameRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(OnlineTournamentActivity.this,
                            getString(R.string.room_not_found), Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                tournament = snapshot.getValue(OnlineTournament.class);

                if (tournament != null && tournament.getPlayer2Id() == null) {
                    tournament.setPlayer2Id(playerId);
                    tournament.setPlayer2Name(name);
                    playerSymbol = 'O';
                    isHost = false;

                    gameRef.setValue(tournament);

                    runOnUiThread(() -> {
                        roomCodeTV.setText(getString(R.string.room_code, gameId));
                        statusTV.setText(getString(R.string.game_starting));
                        updateScoreUI();
                        updateRoundInfo();
                        isMyTurn = tournament.getCurrentPlayer() != null &&
                                tournament.getCurrentPlayer().charAt(0) == playerSymbol;
                        listenForGameUpdates();
                        Toast.makeText(OnlineTournamentActivity.this,
                                "Joined tournament!", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    Toast.makeText(OnlineTournamentActivity.this,
                            getString(R.string.game_full), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(OnlineTournamentActivity.this,
                        getString(R.string.error_prefix) + error.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void listenForGameUpdates() {
        gameListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    if (!isFinishing()) {
                        Toast.makeText(OnlineTournamentActivity.this,
                                "The host has left. Returning to main menu.", Toast.LENGTH_LONG).show();
                        finish();
                    }
                    return;
                }

                OnlineTournament updated = snapshot.getValue(OnlineTournament.class);
                if (updated == null) return;

                tournament = updated;

                updateBoardFromGame();
                updateScoreUI();
                updateRoundInfo();
                updateGameStatus();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(OnlineTournamentActivity.this,
                        getString(R.string.connection_error), Toast.LENGTH_SHORT).show();
            }
        };

        gameRef.addValueEventListener(gameListener);
    }

    private void updateBoardFromGame() {
        if (tournament.getBoard() == null) return;

        Map<String, String> board = tournament.getBoard();

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                String key = i + "," + j;
                String value = board.get(key);

                if (cells[i][j] != null) {
                    if (value != null && !value.isEmpty()) {
                        cells[i][j].setText(value);
                        cells[i][j].setEnabled(false);
                        cells[i][j].setTextColor(
                                getColor(value.equals("X") ? R.color.cyan_main : R.color.orange_main)
                        );
                    } else {
                        // Cellule vide - réinitialiser l'affichage
                        cells[i][j].setText("");
                        cells[i][j].setBackgroundResource(R.drawable.input_bg);
                        cells[i][j].setTextColor(getColor(R.color.white));
                        // Activer uniquement si c'est le tour du joueur et que la partie n'est pas terminée
                        if (!tournament.isGameOver() && !waitingForNextRound && !tournament.isTournamentComplete()) {
                            cells[i][j].setEnabled(isMyTurn);
                        } else {
                            cells[i][j].setEnabled(false);
                        }
                    }
                }
            }
        }
    }

    private void updateScoreUI() {
        if (player1NameTV != null && tournament.getPlayer1Name() != null)
            player1NameTV.setText(tournament.getPlayer1Name());
        if (player2NameTV != null && tournament.getPlayer2Name() != null)
            player2NameTV.setText(tournament.getPlayer2Name());
        if (player1ScoreTV != null)
            player1ScoreTV.setText(String.valueOf(tournament.getPlayer1Score()));
        if (player2ScoreTV != null)
            player2ScoreTV.setText(String.valueOf(tournament.getPlayer2Score()));
        if (drawsTV != null)
            drawsTV.setText(getString(R.string.draws) + ": " + tournament.getDraws());
    }

    private void updateRoundInfo() {
        if (roundInfoTV != null)
            roundInfoTV.setText(getString(R.string.round_x_of_y,
                    tournament.getCurrentRound(),
                    tournament.getTotalRounds()));
    }

    private void updateGameStatus() {
        if (tournament == null) return;

        if (tournament.isTournamentComplete()) {
            enableBoard(false);
            if (!tournamentResultShown) {
                tournamentResultShown = true;
                showTournamentResultDialog();
            }
            return;
        }

        if (tournament.isGameOver()) {
            waitingForNextRound = true;
            enableBoard(false);

            String winner = tournament.getWinner();
            String message;
            if (winner == null || "Draw".equals(winner)) {
                message = getString(R.string.round_draw);
            } else {
                String winnerName = "X".equals(winner)
                        ? tournament.getPlayer1Name()
                        : tournament.getPlayer2Name();
                message = winnerName + " " + getString(R.string.wins_round);
            }
            statusTV.setText(message);

            if (isHost && !nextRoundScheduled) {
                nextRoundScheduled = true;
                new Handler().postDelayed(() -> {
                    nextRoundScheduled = false;
                    if (tournament.getCurrentRound() >= tournament.getTotalRounds()) {
                        Map<String, Object> endUpdates = new HashMap<>();
                        endUpdates.put("tournamentComplete", true);
                        endUpdates.put("tournamentWinner", tournament.getTournamentWinner());
                        gameRef.updateChildren(endUpdates);
                    } else {
                        startNextRound();
                    }
                }, 2000);
            }
            return;
        }

        waitingForNextRound = false;
        String currentPlayerStr = tournament.getCurrentPlayer();
        if (currentPlayerStr == null || currentPlayerStr.isEmpty()) currentPlayerStr = "X";
        char current = currentPlayerStr.charAt(0);
        isMyTurn = (current == playerSymbol);

        statusTV.setText(isMyTurn ? getString(R.string.your_turn) : getString(R.string.opponent_turn));
        enableBoard(isMyTurn && !tournament.isGameOver());
    }

    private void startNextRound() {
        if (tournament == null || tournament.isTournamentComplete()) return;

        tournament.startNextRound();

        Map<String, Object> updates = new HashMap<>();
        updates.put("currentRound", tournament.getCurrentRound());
        updates.put("board", tournament.getBoard());
        updates.put("currentPlayer", tournament.getCurrentPlayer());
        updates.put("gameOver", false);
        updates.put("winner", null);

        gameRef.updateChildren(updates).addOnSuccessListener(aVoid ->
                runOnUiThread(() -> {
                    waitingForNextRound = false;
                    String currentPlayerStr = tournament.getCurrentPlayer();
                    if (currentPlayerStr != null && !currentPlayerStr.isEmpty()) {
                        isMyTurn = currentPlayerStr.charAt(0) == playerSymbol;
                    }
                    updateRoundInfo();
                    updateGameStatus();

                    String roundMessage = String.format(getString(R.string.round_x_of_y),
                            tournament.getCurrentRound(), tournament.getTotalRounds());
                    Toast.makeText(this, roundMessage, Toast.LENGTH_SHORT).show();
                })
        );
    }

    private void enableBoard(boolean enable) {
        if (waitingForNextRound || tournament == null || tournament.isGameOver()) {
            enable = false;
        }

        Map<String, String> board = tournament != null ? tournament.getBoard() : null;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (cells[i][j] != null) {
                    String key = i + "," + j;
                    String value = board != null ? board.get(key) : null;
                    boolean cellEmpty = value == null || value.isEmpty();
                    cells[i][j].setEnabled(enable && cellEmpty);
                }
            }
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

                int row = i, col = j;
                cell.setOnClickListener(v -> makeMove(row, col));

                gameBoard.addView(cell);
                cells[i][j] = cell;
            }
        }
    }

    private void makeMove(int row, int col) {
        if (!isMyTurn || tournament == null || tournament.isGameOver()
                || waitingForNextRound || tournament.isTournamentComplete()) return;

        if (tournament.makeMove(row, col, playerId)) {
            Map<String, Object> updates = new HashMap<>();
            updates.put("board", tournament.getBoard());
            updates.put("currentPlayer", tournament.getCurrentPlayer());

            if (tournament.isGameOver()) {
                updates.put("gameOver", true);
                updates.put("winner", tournament.getWinner());
                updates.put("player1Score", tournament.getPlayer1Score());
                updates.put("player2Score", tournament.getPlayer2Score());
                updates.put("draws", tournament.getDraws());
            }

            gameRef.updateChildren(updates);

            if (soundManager != null) soundManager.playMoveSound();
            updateCellUI(row, col);
        }
    }

    private void updateCellUI(int row, int col) {
        if (cells[row][col] != null) {
            cells[row][col].setText(String.valueOf(playerSymbol));
            cells[row][col].setEnabled(false);
            cells[row][col].setTextColor(
                    getColor(playerSymbol == 'X' ? R.color.cyan_main : R.color.orange_main)
            );
        }
    }

    private void setupButtons() {
        if (leaveBtn != null) {
            leaveBtn.setOnClickListener(v -> leaveGame());
        }
    }

    private void leaveGame() {
        if (isHost && gameRef != null) gameRef.removeValue();
        finish();
    }

    private void showTournamentResultDialog() {
        if (isFinishing()) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Tournament Finished!");

        String winnerName = tournament.getTournamentWinner();
        String message = "Draw".equals(winnerName)
                ? getString(R.string.draw)
                : String.format(getString(R.string.final_winner), winnerName);
        builder.setMessage(message);

        builder.setPositiveButton("Play Again", (dialog, which) -> {
            if (isHost) {
                tournament.resetFullTournament();

                Map<String, Object> updates = new HashMap<>();
                updates.put("currentRound", tournament.getCurrentRound());
                updates.put("player1Score", tournament.getPlayer1Score());
                updates.put("player2Score", tournament.getPlayer2Score());
                updates.put("draws", tournament.getDraws());
                updates.put("board", tournament.getBoard());
                updates.put("currentPlayer", tournament.getCurrentPlayer());
                updates.put("gameOver", false);
                updates.put("winner", null);
                updates.put("tournamentComplete", false);
                updates.put("tournamentWinner", null);

                gameRef.updateChildren(updates);

                runOnUiThread(() -> {
                    waitingForNextRound = false;
                    nextRoundScheduled = false;
                    tournamentResultShown = false;
                    updateScoreUI();
                    updateRoundInfo();
                    updateGameStatus();
                    Toast.makeText(this, "Tournament restarted!", Toast.LENGTH_SHORT).show();
                });
            } else {
                Toast.makeText(this, "Waiting for host to restart...", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Back to Menu", (dialog, which) -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        builder.setCancelable(false);
        builder.show();
    }

    private String generateRoomCode() {
        return String.valueOf((int) (Math.random() * 900000 + 100000));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (gameListener != null && gameRef != null)
            gameRef.removeEventListener(gameListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (soundManager != null) soundManager.pauseMusic();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (soundManager != null && !soundManager.isMuted()) soundManager.resumeMusic();
    }
}