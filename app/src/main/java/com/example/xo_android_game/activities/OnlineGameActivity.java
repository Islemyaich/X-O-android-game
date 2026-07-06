package com.example.xo_android_game.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.xo_android_game.R;
import com.example.xo_android_game.models.OnlineGame;
import com.example.xo_android_game.utils.SoundManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;
import java.util.UUID;

public class OnlineGameActivity extends AppCompatActivity {

    // UI Components
    private GridLayout gameBoard;
    private TextView statusTV, roomCodeTV, yourSymbolTV;
    private Button resetBtn, leaveBtn;
    private Button[][] cells;

    // Game Logic
    private OnlineGame onlineGame;
    private DatabaseReference gameRef;
    private String gameId;
    private String playerId;
    private char playerSymbol;
    private boolean isMyTurn = false;

    // Audio & Database
    private SoundManager soundManager;
    private ValueEventListener gameListener;

    // Logging tag
    private static final String TAG = "OnlineGame";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_game);

        // Initialize sound manager
        soundManager = SoundManager.getInstance(this);
        if (soundManager != null) {
            soundManager.resumeMusic();
        }

        // Initialize UI components
        initViews();
        setupGameBoard();
        setupButtons();

        // Get game ID from intent (null if creating new game)
        gameId = getIntent().getStringExtra("gameId");
        playerId = UUID.randomUUID().toString();

        // Check if creating new game or joining existing
        if (gameId == null) {
            createNewGame();
        } else {
            joinGame(gameId);
        }
    }

    /**
     * Initialize all UI component references
     */
    private void initViews() {
        gameBoard = findViewById(R.id.gameBoard);
        statusTV = findViewById(R.id.statusTV);
        roomCodeTV = findViewById(R.id.roomCodeTV);
        yourSymbolTV = findViewById(R.id.yourSymbolTV);
        resetBtn = findViewById(R.id.resetBtn);
        leaveBtn = findViewById(R.id.leaveBtn);
    }

    /**
     * Create a new online game room
     */
    private void createNewGame() {
        try {
            // Generate unique room code and player ID
            gameId = generateRoomCode();
            onlineGame = new OnlineGame(gameId, playerId);
            playerSymbol = 'X';

            Log.d(TAG, "Creating game with ID: " + gameId);

            // Get Firebase reference for this game
            gameRef = FirebaseDatabase.getInstance().getReference()
                    .child("online_games").child(gameId);

            // Save game to Firebase
            gameRef.setValue(onlineGame)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Game created successfully");
                        runOnUiThread(() -> {
                            // Update UI with room info
                            roomCodeTV.setText(String.format(getString(R.string.room_code), gameId));
                            yourSymbolTV.setText(getString(R.string.you_are_x));
                            statusTV.setText(getString(R.string.waiting_for_opponent));
                            isMyTurn = true;
                            listenForGameUpdates();
                            Toast.makeText(this,
                                    String.format(getString(R.string.game_created), gameId),
                                    Toast.LENGTH_LONG).show();
                        });
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed: " + e.getMessage());
                        runOnUiThread(() -> {
                            Toast.makeText(this,
                                    getString(R.string.error_prefix) + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                            finish();
                        });
                    });
        } catch (Exception e) {
            Log.e(TAG, "Exception in createNewGame: " + e.getMessage(), e);
            Toast.makeText(this, getString(R.string.create_error), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    /**
     * Join an existing game room
     * @param roomCode The room code to join
     */
    private void joinGame(String roomCode) {
        gameId = roomCode.toUpperCase();
        gameRef = FirebaseDatabase.getInstance().getReference()
                .child("online_games").child(gameId);

        // Check if game exists
        gameRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    onlineGame = snapshot.getValue(OnlineGame.class);
                    // Check if game has space for player 2
                    if (onlineGame != null && onlineGame.getPlayer2Id() == null) {
                        onlineGame.setPlayer2Id(playerId);
                        playerSymbol = 'O';
                        gameRef.setValue(onlineGame);
                        roomCodeTV.setText(String.format(getString(R.string.room_code), gameId));
                        yourSymbolTV.setText(getString(R.string.you_are_o));
                        statusTV.setText(getString(R.string.game_starting));
                        isMyTurn = false;
                        listenForGameUpdates();
                    } else {
                        Toast.makeText(OnlineGameActivity.this,
                                getString(R.string.game_full), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(OnlineGameActivity.this,
                            getString(R.string.room_not_found), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(OnlineGameActivity.this,
                        getString(R.string.error_prefix) + error.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    /**
     * Listen for real-time game updates from Firebase
     */
    private void listenForGameUpdates() {
        gameListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                OnlineGame updatedGame = snapshot.getValue(OnlineGame.class);
                if (updatedGame != null) {
                    onlineGame = updatedGame;
                    updateBoardFromGame();
                    updateGameStatus();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(OnlineGameActivity.this,
                        getString(R.string.connection_error), Toast.LENGTH_SHORT).show();
            }
        };
        gameRef.addValueEventListener(gameListener);
    }

    /**
     * Update the game board UI based on the game state from Firebase
     */
    private void updateBoardFromGame() {
        if (onlineGame.getBoard() == null) return;

        Map<String, String> board = onlineGame.getBoard();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                String cellKey = i + "," + j;
                String value = board.get(cellKey);
                if (value != null && !value.isEmpty() && cells[i][j] != null) {
                    cells[i][j].setText(value);
                    cells[i][j].setEnabled(false);
                    if (value.equals("X")) {
                        cells[i][j].setTextColor(getColor(R.color.cyan_main));
                    } else {
                        cells[i][j].setTextColor(getColor(R.color.orange_main));
                    }
                }
            }
        }
    }

    /**
     * Update game status text and turn information
     */
    private void updateGameStatus() {
        if (onlineGame.isGameOver()) {
            if ("Draw".equals(onlineGame.getWinner())) {
                statusTV.setText(getString(R.string.game_draw));
            } else {
                String winner = onlineGame.getWinner();
                statusTV.setText(String.format(getString(R.string.player_wins), winner));
            }
            enableBoard(false);
            return;
        }

        char currentPlayer = onlineGame.getCurrentPlayer().charAt(0);
        isMyTurn = (currentPlayer == playerSymbol);

        if (isMyTurn) {
            statusTV.setText(getString(R.string.your_turn));
        } else {
            statusTV.setText(getString(R.string.opponent_turn));
        }
        enableBoard(isMyTurn && !onlineGame.isGameOver());
    }

    /**
     * Make a move on the board
     * @param row Row position (0-2)
     * @param col Column position (0-2)
     */
    private void makeMove(int row, int col) {
        if (!isMyTurn || onlineGame.isGameOver()) return;

        if (onlineGame.makeMove(row, col, playerId)) {
            // Update Firebase with the new move
            gameRef.child("board").setValue(onlineGame.getBoard());
            gameRef.child("currentPlayer").setValue(onlineGame.getCurrentPlayer());

            if (onlineGame.isGameOver()) {
                gameRef.child("gameOver").setValue(true);
                gameRef.child("winner").setValue(onlineGame.getWinner());
            }

            if (soundManager != null) soundManager.playMoveSound();
            updateBoardUI(row, col);
            enableBoard(false);
        }
    }

    /**
     * Update UI for a single cell after a move
     */
    private void updateBoardUI(int row, int col) {
        if (cells[row][col] != null) {
            cells[row][col].setText(String.valueOf(playerSymbol));
            cells[row][col].setEnabled(false);
            if (playerSymbol == 'X') {
                cells[row][col].setTextColor(getColor(R.color.cyan_main));
            } else {
                cells[row][col].setTextColor(getColor(R.color.orange_main));
            }
        }
    }

    /**
     * Enable or disable all empty cells on the board
     * @param enable true to enable, false to disable
     */
    private void enableBoard(boolean enable) {
        Map<String, String> board = onlineGame.getBoard();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                String cellKey = i + "," + j;
                String value = board.get(cellKey);
                if (cells[i][j] != null && (value == null || value.isEmpty())) {
                    cells[i][j].setEnabled(enable);
                }
            }
        }
    }

    /**
     * Setup the 3x3 game board grid
     */
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
                cell.setOnClickListener(v -> makeMove(row, col));

                gameBoard.addView(cell);
                cells[i][j] = cell;
            }
        }
    }

    /**
     * Setup button click listeners
     */
    private void setupButtons() {
        if (resetBtn != null) {
            resetBtn.setOnClickListener(v -> resetGame());
        }
        if (leaveBtn != null) {
            leaveBtn.setOnClickListener(v -> leaveGame());
        }
    }

    /**
     * Reset the current game (host only)
     */
    private void resetGame() {
        if (onlineGame != null && onlineGame.getPlayer1Id() != null &&
                onlineGame.getPlayer1Id().equals(playerId)) {
            onlineGame.resetGame();
            gameRef.setValue(onlineGame);
            resetBoardUI();
            Toast.makeText(this, getString(R.string.game_reset), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.only_host_reset), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Reset the board UI to empty state
     */
    private void resetBoardUI() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (cells[i][j] != null) {
                    cells[i][j].setText("");
                    cells[i][j].setEnabled(true);
                    cells[i][j].setBackgroundResource(R.drawable.input_bg);
                }
            }
        }
    }

    /**
     * Leave the game and clean up Firebase data if host
     */
    private void leaveGame() {
        if (gameRef != null && onlineGame != null &&
                onlineGame.getPlayer1Id() != null &&
                onlineGame.getPlayer1Id().equals(playerId)) {
            gameRef.removeValue();
        }
        finish();
    }

    /**
     * Generate a random 6-digit room code
     * @return Room code as string
     */
    private String generateRoomCode() {
        return String.valueOf((int)(Math.random() * 900000 + 100000));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove Firebase listener
        if (gameListener != null && gameRef != null) {
            gameRef.removeEventListener(gameListener);
        }
        // Clean up game data if host
        if (onlineGame != null && onlineGame.getPlayer1Id() != null &&
                onlineGame.getPlayer1Id().equals(playerId)) {
            if (gameRef != null) {
                gameRef.removeValue();
            }
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
}