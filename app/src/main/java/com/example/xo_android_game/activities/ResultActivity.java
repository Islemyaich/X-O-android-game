package com.example.xo_android_game.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.xo_android_game.R;
import com.example.xo_android_game.models.Tournament;
import com.example.xo_android_game.utils.SoundManager;
import com.example.xo_android_game.utils.StorageManager;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class ResultActivity extends AppCompatActivity {

    private Tournament tournament;
    private StorageManager storageManager;
    private SoundManager soundManager;
    private TextView winnerTV, player1ScoreTV, player2ScoreTV, drawsTV, totalRoundsTV, dateTV;
    private TextView player1Label, player2Label, drawsLabel, totalRoundsLabel, dateLabel;
    private Button saveBtn, playAgainBtn, backBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        soundManager = SoundManager.getInstance(this);
        soundManager.resumeMusic();

        storageManager = new StorageManager(this);
        tournament = (Tournament) getIntent().getSerializableExtra("tournament");

        initViews();
        setTranslatedTexts();
        displayResults();

        saveBtn.setOnClickListener(v -> saveTournament());
        playAgainBtn.setOnClickListener(v -> playAgain());
        backBtn.setOnClickListener(v -> goBackToHome());
    }

    private void initViews() {
        winnerTV = findViewById(R.id.winnerTV);
        player1ScoreTV = findViewById(R.id.player1ScoreTV);
        player2ScoreTV = findViewById(R.id.player2ScoreTV);
        drawsTV = findViewById(R.id.drawsTV);
        totalRoundsTV = findViewById(R.id.totalRoundsTV);
        dateTV = findViewById(R.id.dateTV);
        player1Label = findViewById(R.id.player1Label);
        player2Label = findViewById(R.id.player2Label);
        drawsLabel = findViewById(R.id.drawsLabel);
        totalRoundsLabel = findViewById(R.id.totalRoundsLabel);
        dateLabel = findViewById(R.id.dateLabel);
        saveBtn = findViewById(R.id.saveBtn);
        playAgainBtn = findViewById(R.id.playAgainBtn);
        backBtn = findViewById(R.id.backBtn);
    }

    private void setTranslatedTexts() {
        player1Label.setText(tournament.getPlayer1Name());
        player2Label.setText(tournament.getPlayer2Name());
        drawsLabel.setText(getString(R.string.draws));
        totalRoundsLabel.setText(getString(R.string.total_rounds));
        dateLabel.setText(getString(R.string.date));
        saveBtn.setText(getString(R.string.save_tournament));
        playAgainBtn.setText(getString(R.string.play_again));
        backBtn.setText(getString(R.string.back_to_home));
    }

    private void displayResults() {
        String winner = tournament.getTournamentWinner();
        if (winner.equals("Draw")) {
            winnerTV.setText(getString(R.string.draw));
        } else {
            winnerTV.setText(getString(R.string.winner) + "\n" + winner);
        }

        player1ScoreTV.setText(String.valueOf(tournament.getPlayer1Score()));
        player2ScoreTV.setText(String.valueOf(tournament.getPlayer2Score()));
        drawsTV.setText(String.valueOf(tournament.getDraws()));
        totalRoundsTV.setText(String.valueOf(tournament.getTotalRounds()));

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        dateTV.setText(sdf.format(tournament.getDate()));
    }

    private void saveTournament() {
        storageManager.saveTournament(tournament);
        Toast.makeText(this, getString(R.string.tournament_saved), Toast.LENGTH_SHORT).show();
        saveBtn.setEnabled(false);
        saveBtn.setText("✓ " + getString(R.string.saved));
        soundManager.playClickSound();
    }

    private void playAgain() {
        soundManager.playClickSound();

        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("player1", tournament.getPlayer1Name());
        intent.putExtra("player2", tournament.getPlayer2Name());
        intent.putExtra("symbol", String.valueOf(tournament.getPlayer1Symbol()));
        intent.putExtra("rounds", tournament.getTotalRounds());
        intent.putExtra("ai", tournament.isAgainstAI());
        intent.putExtra("difficulty", tournament.getAiDifficulty());

        startActivity(intent);
        finish();
    }

    private void goBackToHome() {
        soundManager.playClickSound();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
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