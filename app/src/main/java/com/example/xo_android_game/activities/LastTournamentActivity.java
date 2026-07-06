package com.example.xo_android_game.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.xo_android_game.R;
import com.example.xo_android_game.models.Tournament;
import com.example.xo_android_game.utils.SoundManager;
import com.example.xo_android_game.utils.StorageManager;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class LastTournamentActivity extends AppCompatActivity {

    private StorageManager storageManager;
    private SoundManager soundManager;
    private LinearLayout historyContainer;
    private TextView lastWinnerTV, lastPlayer1ScoreTV, lastPlayer2ScoreTV, lastDrawsTV, lastTotalRoundsTV, lastDateTV;
    private TextView titleTV, lastTournamentTitleTV, historyTitleTV;
    private Button backBtn, prevBtn, nextBtn;

    private List<Tournament> history;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_last_tournament);

        soundManager = SoundManager.getInstance(this);
        storageManager = new StorageManager(this);

        initViews();
        setTranslatedTexts();
        loadLastTournament();
        loadTournamentHistory();

        backBtn.setOnClickListener(v -> {
            if (soundManager != null) soundManager.playClickSound();
            finish();
        });

    }

    private void initViews() {
        titleTV = findViewById(R.id.titleTV);
        lastTournamentTitleTV = findViewById(R.id.lastTournamentTitleTV);
        historyTitleTV = findViewById(R.id.historyTitleTV);
        lastWinnerTV = findViewById(R.id.lastWinnerTV);
        lastPlayer1ScoreTV = findViewById(R.id.lastPlayer1ScoreTV);
        lastPlayer2ScoreTV = findViewById(R.id.lastPlayer2ScoreTV);
        lastDrawsTV = findViewById(R.id.lastDrawsTV);
        lastTotalRoundsTV = findViewById(R.id.lastTotalRoundsTV);
        lastDateTV = findViewById(R.id.lastDateTV);
        backBtn = findViewById(R.id.backBtn);
        prevBtn = findViewById(R.id.prevBtn);
        nextBtn = findViewById(R.id.nextBtn);
        historyContainer = findViewById(R.id.historyContainer);
    }

    private void setTranslatedTexts() {
        titleTV.setText(getString(R.string.last_tournament_results));
        lastTournamentTitleTV.setText(getString(R.string.last_tournament_results));
        historyTitleTV.setText(getString(R.string.tournament_history));
        prevBtn.setText(getString(R.string.previous));
        nextBtn.setText(getString(R.string.next));
        backBtn.setText(getString(R.string.back));
    }

    private void loadLastTournament() {
        Tournament lastTournament = storageManager.loadLastTournament();

        if (lastTournament == null) {
            lastWinnerTV.setText(getString(R.string.no_tournament));
            lastPlayer1ScoreTV.setText("-");
            lastPlayer2ScoreTV.setText("-");
            lastDrawsTV.setText("-");
            lastTotalRoundsTV.setText("-");
            lastDateTV.setText("-");
            return;
        }

        String winner = lastTournament.getTournamentWinner();
        if (winner.equals("Draw")) {
            lastWinnerTV.setText(getString(R.string.draw));
        } else {
            lastWinnerTV.setText(winner);
        }

        lastPlayer1ScoreTV.setText(lastTournament.getPlayer1Name() + ": " + lastTournament.getPlayer1Score());
        lastPlayer2ScoreTV.setText(lastTournament.getPlayer2Name() + ": " + lastTournament.getPlayer2Score());
        lastDrawsTV.setText(getString(R.string.draws) + ": " + lastTournament.getDraws());
        lastTotalRoundsTV.setText(getString(R.string.total_rounds) + ": " + lastTournament.getTotalRounds());

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        lastDateTV.setText(getString(R.string.date) + ": " + sdf.format(lastTournament.getDate()));
    }

    private void loadTournamentHistory() {
        history = storageManager.loadTournamentHistory();

        if (history.isEmpty()) {
            historyContainer.removeAllViews();
            TextView emptyText = new TextView(this);
            emptyText.setText(getString(R.string.no_history));
            emptyText.setTextColor(getColor(R.color.white));
            emptyText.setPadding(20, 20, 20, 20);
            historyContainer.addView(emptyText);
            prevBtn.setVisibility(View.GONE);
            nextBtn.setVisibility(View.GONE);
            return;
        }

        updateHistoryDisplay();
    }

    private void updateHistoryDisplay() {
        historyContainer.removeAllViews();

        int end = Math.min(3, history.size());

        for (int i = 0; i < end; i++) {
            Tournament t = history.get(i);
            View historyItem = getLayoutInflater().inflate(R.layout.history_item, null);

            TextView matchInfo = historyItem.findViewById(R.id.matchInfo);
            TextView scores = historyItem.findViewById(R.id.scores);
            TextView date = historyItem.findViewById(R.id.date);

            matchInfo.setText(t.getPlayer1Name() + " vs " + t.getPlayer2Name());

            scores.setText(
                    t.getPlayer1Name() + ": " + t.getPlayer1Score() +
                            " | Draws: " + t.getDraws() +
                            " | " + t.getPlayer2Name() + ": " + t.getPlayer2Score()
            );

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            date.setText(sdf.format(t.getDate()));

            historyContainer.addView(historyItem);
        }

        prevBtn.setVisibility(View.GONE);
        nextBtn.setVisibility(View.GONE);
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