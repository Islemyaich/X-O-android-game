package com.example.xo_android_game.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;

import com.example.xo_android_game.R;

public class SoundManager {
    private static SoundManager instance;
    private MediaPlayer backgroundMusic;
    private Context context;
    private boolean isMusicPlaying = false;
    private boolean isMuted = false;
    private MediaPlayer soundEffect;

    private SoundManager(Context context) {
        this.context = context;
        loadMutePreference();
        initSounds();
    }

    public static synchronized SoundManager getInstance(Context context) {
        if (instance == null) {
            instance = new SoundManager(context.getApplicationContext());
        }
        return instance;
    }

    private void initSounds() {
        try {
            backgroundMusic = MediaPlayer.create(context, R.raw.music);
            if (backgroundMusic != null) {
                backgroundMusic.setLooping(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadMutePreference() {
        SharedPreferences prefs = context.getSharedPreferences("GamePrefs", Context.MODE_PRIVATE);
        isMuted = prefs.getBoolean("isMuted", false);
    }

    private void saveMutePreference() {
        SharedPreferences prefs = context.getSharedPreferences("GamePrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("isMuted", isMuted);
        editor.apply();
    }

    public void toggleMute() {
        isMuted = !isMuted;
        saveMutePreference();

        if (isMuted) {
            pauseMusic();
        } else {
            resumeMusic();
        }
    }

    public boolean isMuted() {
        return isMuted;
    }

    public boolean isMusicPlaying() {
        return isMusicPlaying;
    }

    public void startBackgroundMusic() {
        if (!isMuted && backgroundMusic != null && !isMusicPlaying) {
            try {
                backgroundMusic.start();
                isMusicPlaying = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void pauseMusic() {
        if (backgroundMusic != null && isMusicPlaying) {
            backgroundMusic.pause();
            isMusicPlaying = false;
        }
    }

    public void resumeMusic() {
        if (!isMuted && backgroundMusic != null && !isMusicPlaying) {
            try {
                backgroundMusic.start();
                isMusicPlaying = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void stopBackgroundMusic() {
        // Don't actually stop, just pause
        pauseMusic();
    }

    public void playMoveSound() {
        if (!isMuted) {
            try {
                if (soundEffect != null) {
                    soundEffect.release();
                }
                soundEffect = MediaPlayer.create(context, R.raw.move_sound);
                soundEffect.setOnCompletionListener(mp -> {
                    mp.release();
                    soundEffect = null;
                });
                soundEffect.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void playWinSound() {
        if (!isMuted) {
            try {
                if (soundEffect != null) {
                    soundEffect.release();
                }
                soundEffect = MediaPlayer.create(context, R.raw.win_sound);
                soundEffect.setOnCompletionListener(mp -> {
                    mp.release();
                    soundEffect = null;
                });
                soundEffect.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void playClickSound() {
        if (!isMuted) {
            try {
                if (soundEffect != null) {
                    soundEffect.release();
                }
                soundEffect = MediaPlayer.create(context, R.raw.click_sound);
                soundEffect.setOnCompletionListener(mp -> {
                    mp.release();
                    soundEffect = null;
                });
                soundEffect.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void release() {
        // Don't release, just pause
        pauseMusic();
    }
}