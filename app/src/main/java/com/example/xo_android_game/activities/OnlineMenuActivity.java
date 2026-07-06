package com.example.xo_android_game.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.xo_android_game.R;
import com.example.xo_android_game.utils.SoundManager;

public class OnlineMenuActivity extends AppCompatActivity {

    private Button createGameBtn, joinGameBtn, backBtn;
    private Button btn5Rounds, btn10Rounds, btn15Rounds;
    private EditText roomCodeInput, playerNameInput;
    private int selectedRounds = 5;
    private SoundManager soundManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_menu);

        soundManager = SoundManager.getInstance(this);
        if (soundManager != null) {
            soundManager.resumeMusic();
        }

        initViews();
        setupRoundsButtons();
        setupButtons();
        setTranslatedTexts();
    }

    private void initViews() {
        createGameBtn = findViewById(R.id.createGameBtn);
        joinGameBtn = findViewById(R.id.joinGameBtn);
        backBtn = findViewById(R.id.backBtn);
        roomCodeInput = findViewById(R.id.roomCodeInput);
        playerNameInput = findViewById(R.id.playerNameInput);
        btn5Rounds = findViewById(R.id.btn5Rounds);
        btn10Rounds = findViewById(R.id.btn10Rounds);
        btn15Rounds = findViewById(R.id.btn15Rounds);
    }

    private void setTranslatedTexts() {
        playerNameInput.setHint(getString(R.string.your_name));
        roomCodeInput.setHint(getString(R.string.enter_room_code_joining));

        createGameBtn.setText(getString(R.string.create_new_game));
        joinGameBtn.setText(getString(R.string.join_game));
        backBtn.setText(getString(R.string.back));
    }

    private void setupRoundsButtons() {
        updateRoundsButtonStyle();

        btn5Rounds.setOnClickListener(v -> {
            if (soundManager != null) soundManager.playClickSound();
            selectedRounds = 5;
            updateRoundsButtonStyle();
        });

        btn10Rounds.setOnClickListener(v -> {
            if (soundManager != null) soundManager.playClickSound();
            selectedRounds = 10;
            updateRoundsButtonStyle();
        });

        btn15Rounds.setOnClickListener(v -> {
            if (soundManager != null) soundManager.playClickSound();
            selectedRounds = 15;
            updateRoundsButtonStyle();
        });
    }

    private void updateRoundsButtonStyle() {
        resetRoundButton(btn5Rounds);
        resetRoundButton(btn10Rounds);
        resetRoundButton(btn15Rounds);

        if (selectedRounds == 5) {
            btn5Rounds.setBackgroundResource(R.drawable.round_selected_glow);
            btn5Rounds.setTextColor(0xFFFFFFFF);
        } else if (selectedRounds == 10) {
            btn10Rounds.setBackgroundResource(R.drawable.round_selected_glow);
            btn10Rounds.setTextColor(0xFFFFFFFF);
        } else if (selectedRounds == 15) {
            btn15Rounds.setBackgroundResource(R.drawable.round_selected_glow);
            btn15Rounds.setTextColor(0xFFFFFFFF);
        }
    }

    private void resetRoundButton(Button button) {
        button.setBackgroundResource(R.drawable.round_unselected_green);
        button.setTextColor(0xFFD7E7E2);
    }

    private void setupButtons() {
        createGameBtn.setOnClickListener(v -> {
            if (soundManager != null) soundManager.playClickSound();

            String playerName = playerNameInput.getText().toString().trim();
            if (playerName.isEmpty()) playerName = "Player";

            Intent intent = new Intent(OnlineMenuActivity.this, OnlineTournamentActivity.class);
            intent.putExtra("playerName", playerName);
            intent.putExtra("totalRounds", selectedRounds);
            startActivity(intent);
        });

        joinGameBtn.setOnClickListener(v -> {
            if (soundManager != null) soundManager.playClickSound();

            String roomCode = roomCodeInput.getText().toString().trim().toUpperCase();
            String playerName = playerNameInput.getText().toString().trim();

            if (roomCode.isEmpty()) {
                Toast.makeText(this, getString(R.string.enter_room_code), Toast.LENGTH_SHORT).show();
                return;
            }

            if (playerName.isEmpty()) playerName = "Player";

            Intent intent = new Intent(OnlineMenuActivity.this, OnlineTournamentActivity.class);
            intent.putExtra("gameId", roomCode);
            intent.putExtra("playerName", playerName);
            startActivity(intent);
        });

        backBtn.setOnClickListener(v -> {
            if (soundManager != null) soundManager.playClickSound();
            finish();
        });
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
        setTranslatedTexts();
    }
}