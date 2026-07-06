package com.example.xo_android_game.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.xo_android_game.R;
import com.example.xo_android_game.utils.SoundManager;

public class RulesActivity extends AppCompatActivity {

    private Button backBtn;
    private TextView titleTV, rulesText;
    private SoundManager soundManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rules);

        soundManager = SoundManager.getInstance(this);

        backBtn = findViewById(R.id.backBtn);
        titleTV = findViewById(R.id.titleTV);
        rulesText = findViewById(R.id.rulesText);

        // Set translated title
        titleTV.setText(getString(R.string.game_rules));
        // Set translated rules text
        rulesText.setText(getString(R.string.rules_text));
        // Set translated back button text
        backBtn.setText(getString(R.string.back));

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
    }
}