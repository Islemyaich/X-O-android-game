package com.example.xo_android_game.utils;

import android.content.Context;

import com.example.xo_android_game.models.Tournament;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class StorageManager {
    private static final String FILE_NAME = "tournament_data.ser";
    private static final String HISTORY_FILE = "tournament_history.ser";

    private final Context context;

    public StorageManager(Context context) {
        this.context = context;
    }

    public void saveTournament(Tournament tournament) {
        saveLastTournament(tournament);
        saveToHistory(tournament);
    }

    private void saveLastTournament(Tournament tournament) {
        try {
            FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(tournament);
            oos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Tournament loadLastTournament() {
        try {
            FileInputStream fis = context.openFileInput(FILE_NAME);
            ObjectInputStream ois = new ObjectInputStream(fis);
            Tournament tournament = (Tournament) ois.readObject();
            ois.close();
            fis.close();
            return tournament;
        } catch (IOException | ClassNotFoundException e) {
            return null;
        }
    }

    private void saveToHistory(Tournament tournament) {
        List<Tournament> history = loadTournamentHistory();

        // éviter doublon si le même tournoi est sauvegardé 2 fois
        if (!history.isEmpty()) {
            Tournament first = history.get(0);
            boolean sameTournament =
                    first.getPlayer1Name().equals(tournament.getPlayer1Name()) &&
                            first.getPlayer2Name().equals(tournament.getPlayer2Name()) &&
                            first.getPlayer1Score() == tournament.getPlayer1Score() &&
                            first.getPlayer2Score() == tournament.getPlayer2Score() &&
                            first.getDraws() == tournament.getDraws() &&
                            first.getTotalRounds() == tournament.getTotalRounds() &&
                            first.getDate().equals(tournament.getDate());

            if (sameTournament) {
                return;
            }
        }

        // nouveau tournoi toujours en haut
        history.add(0, tournament);

        // garder seulement les 3 derniers
        while (history.size() > 3) {
            history.remove(history.size() - 1);
        }

        try {
            FileOutputStream fos = context.openFileOutput(HISTORY_FILE, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(new ArrayList<>(history));
            oos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public List<Tournament> loadTournamentHistory() {
        try {
            FileInputStream fis = context.openFileInput(HISTORY_FILE);
            ObjectInputStream ois = new ObjectInputStream(fis);
            List<Tournament> history = (List<Tournament>) ois.readObject();
            ois.close();
            fis.close();
            return history != null ? history : new ArrayList<>();
        } catch (IOException | ClassNotFoundException e) {
            return new ArrayList<>();
        }
    }

    public void clearAllTournaments() {
        context.deleteFile(FILE_NAME);
        context.deleteFile(HISTORY_FILE);
    }
}