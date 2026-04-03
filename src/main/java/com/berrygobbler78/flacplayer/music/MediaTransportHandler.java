package com.berrygobbler78.flacplayer.music;

import io.github.selemba1000.*;
import javafx.concurrent.Task;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MediaTransportHandler {

    private final JMTC jmtc;
    private String currentSong = "";
    private Path currentArtwork;

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public MediaTransportHandler(String playerName, String playerPath, PlaybackManager playbackManager, QueueManager queueManager) {
        jmtc = JMTC.getInstance(new JMTCSettings(playerName, playerPath));
        if(playbackManager == null) return;

        JMTCCallbacks callbacks = new JMTCCallbacks();
        callbacks.onPlay = playbackManager::play;
        callbacks.onPause = playbackManager::pause;
        callbacks.onStop = playbackManager::stop;
        callbacks.onNext = playbackManager::next;
        callbacks.onPrevious = playbackManager::previous;
        callbacks.onLoop = _ -> queueManager.cycleRepeatStatus();
        callbacks.onShuffle = _ -> queueManager.toggleShuffleStatus();


        jmtc.setCallbacks(callbacks);
        jmtc.setMediaType(JMTCMediaType.Music);
        jmtc.setMediaProperties(new JMTCMusicProperties(
                "Unknown", "Unknown Artist", "Unknown Album", "Unknown Artist",
                new String[]{}, 1, 1, null
        ));
    }

    public void setEnabled(boolean enabled) {
        if(enabled) {
            jmtc.setEnabled(true);
            jmtc.setEnabledButtons(new JMTCEnabledButtons(true, true, true, true, true));
            jmtc.updateDisplay();
        }
    }

    public void setProperties(String songTitle, String songArtist,
                              String parentTitle, String parentArtist,
                              int tracks, int index, File coverArt) {

        if(jmtc.getEnabled() == false) setEnabled(true);

        Task<Void> update = new Task<>() {
            @Override
            protected Void call() throws Exception {
                File artworkFile = null;

                if (coverArt != null && !songTitle.equals(currentSong)) {
                    currentSong = songTitle;

                    // Create a temp file for artwork
                    File temp = File.createTempFile("currentArt", ".tmp");
                    temp.deleteOnExit(); // automatically clean up on exit

                    Files.copy(coverArt.toPath(), temp.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    currentArtwork = temp.toPath();
                }

                if (currentArtwork != null) {
                    artworkFile = currentArtwork.toFile();
                }

                jmtc.setMediaProperties(new JMTCMusicProperties(
                        songTitle,
                        songArtist,
                        parentTitle,
                        parentArtist,
                        new String[]{},  //  TODO: Add genres
                        tracks,
                        index,
                        artworkFile
                ));
                jmtc.updateDisplay();

                return null;
            }
        };

        executor.submit(update);
        jmtc.updateDisplay();
    }

    public void setTimeline(long start, long end, long seekStart, long seekEnd) {
        jmtc.setTimelineProperties(new JMTCTimelineProperties(start, end, seekStart, seekEnd));
        jmtc.updateDisplay();
    }

    public void setState(JMTCPlayingState state) {
        jmtc.setPlayingState(state);
        jmtc.updateDisplay();
    }

    public static void shutdown() {
        executor.shutdown();
    }
}