package com.example.xo_android_game.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.xo_android_game.R;
import com.example.xo_android_game.utils.SoundManager;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    TextView autoAssignedSymbol;
    EditText player1Name, player2Name;
    Button radioX, radioO;
    Switch switchAI;
    LinearLayout difficultyLayout;
    Button easyBtn, mediumBtn, hardBtn;
    TextView player1SymbolBadge, player2SymbolBadge;
    Button round5, round10, round15;
    Button btnPlay, btnRules, btnScores, btnOnline;
    Button btnEN, btnFR, btnAR, btnMute;

    String selectedSymbol = "X";
    int selectedRounds = 5;
    String difficulty = "Medium";

    private SoundManager soundManager;
    private String currentLanguage = "en";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        soundManager = SoundManager.getInstance(this);
        currentLanguage = getCurrentLanguage();

        // Resume music
        soundManager.resumeMusic();

        autoAssignedSymbol = findViewById(R.id.autoAssignedSymbol);
        player1SymbolBadge = findViewById(R.id.player1SymbolBadge);
        player2SymbolBadge = findViewById(R.id.player2SymbolBadge);
        player1Name = findViewById(R.id.player1Name);
        player2Name = findViewById(R.id.player2Name);
        radioX = findViewById(R.id.radioX);
        radioO = findViewById(R.id.radioO);
        switchAI = findViewById(R.id.switchAI);
        difficultyLayout = findViewById(R.id.difficultyLayout);
        easyBtn = findViewById(R.id.easyBtn);
        mediumBtn = findViewById(R.id.mediumBtn);
        hardBtn = findViewById(R.id.hardBtn);
        round5 = findViewById(R.id.round5);
        round10 = findViewById(R.id.round10);
        round15 = findViewById(R.id.round15);
        btnPlay = findViewById(R.id.btnPlay);
        btnRules = findViewById(R.id.btnRules);
        btnScores = findViewById(R.id.btnScores);
        btnOnline = findViewById(R.id.btnOnline);
        btnEN = findViewById(R.id.btnEN);
        btnFR = findViewById(R.id.btnFR);
        btnAR = findViewById(R.id.btnAR);
        btnMute = findViewById(R.id.btnMute);

        updateLanguageButtonHighlight();

        btnEN.setOnClickListener(v -> {
            if (soundManager != null) soundManager.playClickSound();
            if (!currentLanguage.equals("en")) {
                changeLanguage("en");
            }
        });

        btnFR.setOnClickListener(v -> {
            if (soundManager != null) soundManager.playClickSound();
            if (!currentLanguage.equals("fr")) {
                changeLanguage("fr");
            }
        });

        btnAR.setOnClickListener(v -> {
            if (soundManager != null) soundManager.playClickSound();
            if (!currentLanguage.equals("ar")) {
                changeLanguage("ar");
            }
        });

        updateMuteButton();
        btnMute.setOnClickListener(v -> {
            if (soundManager != null) {
                soundManager.playClickSound();
                soundManager.toggleMute();
                updateMuteButton();
            }
        });

        updateSymbolSelectionUI();
        updateDifficultySelectionUI();
        updateRoundsSelectionUI();

        radioX.setOnClickListener(v -> {
            if (soundManager != null) soundManager.playClickSound();
            selectedSymbol = "X";
            updateSymbolSelectionUI();
        });

        radioO.setOnClickListener(v -> {
            if (soundManager != null) soundManager.playClickSound();
            selectedSymbol = "O";
            updateSymbolSelectionUI();
        });

        switchAI.setOnCheckedChangeListener((buttonView, isChecked) -> {
            difficultyLayout.setVisibility(isChecked ? LinearLayout.VISIBLE : LinearLayout.GONE);
        });

        easyBtn.setOnClickListener(v -> {
            if (soundManager != null) soundManager.playClickSound();
            difficulty = "Easy";
            updateDifficultySelectionUI();
        });

        mediumBtn.setOnClickListener(v -> {
            if (soundManager != null) soundManager.playClickSound();
            difficulty = "Medium";
            updateDifficultySelectionUI();
        });

        hardBtn.setOnClickListener(v -> {
            if (soundManager != null) soundManager.playClickSound();
            difficulty = "Hard";
            updateDifficultySelectionUI();
        });

        round5.setOnClickListener(v -> {
            if (soundManager != null) soundManager.playClickSound();
            selectedRounds = 5;
            updateRoundsSelectionUI();
        });

        round10.setOnClickListener(v -> {
            if (soundManager != null) soundManager.playClickSound();
            selectedRounds = 10;
            updateRoundsSelectionUI();
        });

        round15.setOnClickListener(v -> {
            if (soundManager != null) soundManager.playClickSound();
            selectedRounds = 15;
            updateRoundsSelectionUI();
        });

        btnPlay.setOnClickListener(v -> {
            if (soundManager != null) soundManager.playClickSound();
            Intent intent = new Intent(MainActivity.this, GameActivity.class);
            String p1Name = player1Name.getText().toString().trim();
            String p2Name = player2Name.getText().toString().trim();
            if (p1Name.isEmpty()) p1Name = "Player 1";
            if (p2Name.isEmpty()) p2Name = "Player 2";
            intent.putExtra("player1", p1Name);
            intent.putExtra("player2", p2Name);
            intent.putExtra("symbol", selectedSymbol);
            intent.putExtra("rounds", selectedRounds);
            intent.putExtra("ai", switchAI.isChecked());
            intent.putExtra("difficulty", difficulty);
            startActivity(intent);
        });

        btnRules.setOnClickListener(v -> {
            if (soundManager != null) soundManager.playClickSound();
            startActivity(new Intent(this, RulesActivity.class));
        });

        btnScores.setOnClickListener(v -> {
            if (soundManager != null) soundManager.playClickSound();
            startActivity(new Intent(this, LastTournamentActivity.class));
        });

        btnOnline.setOnClickListener(v -> {
            if (soundManager != null) soundManager.playClickSound();
            startActivity(new Intent(MainActivity.this, OnlineMenuActivity.class));
        });
    }

    private String getCurrentLanguage() {
        String currentLang = Locale.getDefault().getLanguage();
        if (currentLang.equals("fr")) return "fr";
        if (currentLang.equals("ar")) return "ar";
        return "en";
    }

    private void changeLanguage(String newLanguage) {
        // Save music state
        boolean wasPlaying = !soundManager.isMuted() && soundManager.isMusicPlaying();

        // Change language
        setLocale(newLanguage);
        currentLanguage = newLanguage;

        // Restore music after activity recreates
        new Handler().postDelayed(() -> {
            if (soundManager != null) {
                if (wasPlaying && !soundManager.isMuted()) {
                    soundManager.resumeMusic();
                }
                updateLanguageButtonHighlight();
                updateMuteButton();
            }
        }, 200);
    }

    private void updateLanguageButtonHighlight() {
        resetLanguageButton(btnEN);
        resetLanguageButton(btnFR);
        resetLanguageButton(btnAR);

        if (currentLanguage.equals("en")) {
            btnEN.setBackgroundResource(R.drawable.option_selected_orange_bg);
            btnEN.setTextColor(0xFFFFA500);
        } else if (currentLanguage.equals("fr")) {
            btnFR.setBackgroundResource(R.drawable.option_selected_orange_bg);
            btnFR.setTextColor(0xFFFFA500);
        } else if (currentLanguage.equals("ar")) {
            btnAR.setBackgroundResource(R.drawable.option_selected_orange_bg);
            btnAR.setTextColor(0xFFFFA500);
        }
    }

    private void resetLanguageButton(Button button) {
        button.setBackgroundResource(R.drawable.bottom_btn_green);
        button.setTextColor(0xFFFFFFFF);
    }

    private void updateSymbolSelectionUI() {
        if ("X".equals(selectedSymbol)) {
            radioX.setBackgroundResource(R.drawable.symbol_selected_blue_bg);
            radioX.setTextColor(0xFF20D6FF);
            radioO.setBackgroundResource(R.drawable.symbol_unselected_bg);
            radioO.setTextColor(0xFFD7E7E2);
            player1SymbolBadge.setText("X");
            player1SymbolBadge.setTextColor(0xFF20D6FF);
            player1SymbolBadge.setBackgroundResource(R.drawable.small_badge_blue);
            autoAssignedSymbol.setText("O");
            autoAssignedSymbol.setTextColor(0xFFFFA500);
            autoAssignedSymbol.setBackgroundResource(R.drawable.small_badge_orange);
            player2SymbolBadge.setText("O");
            player2SymbolBadge.setTextColor(0xFFFFA500);
            player2SymbolBadge.setBackgroundResource(R.drawable.small_badge_orange);
        } else {
            radioO.setBackgroundResource(R.drawable.symbol_selected_orange_bg);
            radioO.setTextColor(0xFFFFA500);
            radioX.setBackgroundResource(R.drawable.symbol_unselected_bg);
            radioX.setTextColor(0xFFD7E7E2);
            player1SymbolBadge.setText("O");
            player1SymbolBadge.setTextColor(0xFFFFA500);
            player1SymbolBadge.setBackgroundResource(R.drawable.small_badge_orange);
            autoAssignedSymbol.setText("X");
            autoAssignedSymbol.setTextColor(0xFF20D6FF);
            autoAssignedSymbol.setBackgroundResource(R.drawable.small_badge_blue);
            player2SymbolBadge.setText("X");
            player2SymbolBadge.setTextColor(0xFF20D6FF);
            player2SymbolBadge.setBackgroundResource(R.drawable.small_badge_blue);
        }
    }

    private void updateDifficultySelectionUI() {
        resetSmallOptionButton(easyBtn);
        resetSmallOptionButton(mediumBtn);
        resetSmallOptionButton(hardBtn);
        if ("Easy".equals(difficulty)) {
            easyBtn.setBackgroundResource(R.drawable.option_selected_orange_glow);
            easyBtn.setTextColor(0xFFFFA500);
        } else if ("Medium".equals(difficulty)) {
            mediumBtn.setBackgroundResource(R.drawable.option_selected_orange_bg);
            mediumBtn.setTextColor(0xFFFFA500);
        } else {
            hardBtn.setBackgroundResource(R.drawable.option_selected_orange_bg);
            hardBtn.setTextColor(0xFFFFA500);
        }
    }

    private void updateRoundsSelectionUI() {
        resetRoundButton(round5);
        resetRoundButton(round10);
        resetRoundButton(round15);
        if (selectedRounds == 5) {
            round5.setBackgroundResource(R.drawable.round_selected_glow);
            round5.setTextColor(0xFFFFFFFF);
        } else if (selectedRounds == 10) {
            round10.setBackgroundResource(R.drawable.round_selected_bg);
            round10.setTextColor(0xFFFFFFFF);
        } else {
            round15.setBackgroundResource(R.drawable.round_selected_bg);
            round15.setTextColor(0xFFFFFFFF);
        }
    }

    private void resetRoundButton(Button button) {
        button.setBackgroundResource(R.drawable.round_unselected_green);
        button.setTextColor(0xFFD7E7E2);
    }

    private void resetSmallOptionButton(Button button) {
        button.setBackgroundResource(R.drawable.option_unselected_green_bg);
        button.setTextColor(0xFFD7E7E2);
    }

    private void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
        recreate();
    }

    private void updateMuteButton() {
        if (btnMute != null && soundManager != null) {
            if (soundManager.isMuted()) {
                btnMute.setText("🔇");
            } else {
                btnMute.setText("🔊");
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
        currentLanguage = getCurrentLanguage();
        updateLanguageButtonHighlight();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Don't release SoundManager
    }
}