package com.berrygobbler78.flacplayer.util.handlers;

import com.berrygobbler78.flacplayer.util.MusicPlayer;
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

    public MediaTransportHandler(String playerName, String playerPath, MusicPlayer musicPlayer) {
        jmtc = JMTC.getInstance(new JMTCSettings(playerName, playerPath));

        JMTCCallbacks callbacks = new JMTCCallbacks();
        callbacks.onPlay = musicPlayer::play;
        callbacks.onPause = musicPlayer::pause;
        callbacks.onStop = musicPlayer::stop;
        callbacks.onNext = musicPlayer::next;
        callbacks.onPrevious = musicPlayer::previous;
        callbacks.onLoop = _ -> musicPlayer.cycleRepeatStatus();
        callbacks.onShuffle = _ -> musicPlayer.toggleShuffleStatus();


        jmtc.setCallbacks(callbacks);
        jmtc.setMediaType(JMTCMediaType.Music);
        jmtc.setMediaProperties(new JMTCMusicProperties(
                "Unknown", "Unknown Artist", "Unknown Album", "Unknown Artist",
                new String[]{}, 1, 1, null
        ));
        jmtc.setEnabled(true);
        jmtc.setEnabledButtons(new JMTCEnabledButtons(true, true, true, true, true));
        jmtc.updateDisplay();
    }

    public void setProperties(String songTitle, String songArtist,
                              String parentTitle, String parentArtist,
                              int tracks, int index, File coverArt) {

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
                        new String[]{},  // genres can be added here
                        tracks,
                        index,
                        artworkFile
                ));
                jmtc.updateDisplay();

                return null;
            }
        };

        executor.submit(update);
    }

    public void setTimeline(long start, long end, long seekStart, long seekEnd) {
        jmtc.setTimelineProperties(new JMTCTimelineProperties(start, end, seekStart, seekEnd));
        jmtc.updateDisplay();
    }

    public void setState(JMTCPlayingState state) {
        jmtc.setPlayingState(state);
        jmtc.updateDisplay();
    }

    public void setEnabled(boolean enabled) {
        jmtc.setEnabled(enabled);
    }

    public static void shutdown() {
        executor.shutdown();
    }
}