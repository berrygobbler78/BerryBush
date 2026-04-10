package com.berrygobbler78.flacplayer.music;

import com.berrygobbler78.flacplayer.App;
import io.github.selemba1000.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class MediaTransportHandler {

    private final JMTC jmtc;
    private String currentSong = "";
    private Path currentArtwork;

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
        jmtc.setEnabled(enabled);
        jmtc.setEnabledButtons(new JMTCEnabledButtons(enabled, enabled, enabled, enabled, enabled));
        jmtc.updateDisplay();
    }

    public void setProperties(
            String songTitle, String songArtist, String parentTitle, String parentArtist,
            int tracks, int index, File coverArt)
    {

        if(!jmtc.getEnabled()) setEnabled(true);

        App.submitTask(() -> {
            File artworkFile = null;

            if (coverArt != null && !songTitle.equals(currentSong)) {
                currentSong = songTitle;

                File temp = File.createTempFile("currentArt", ".tmp");
                temp.deleteOnExit();

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
        });
    }

    public void setTimeline(long start, long end, long seekStart, long seekEnd) {
        jmtc.setTimelineProperties(new JMTCTimelineProperties(start, end, seekStart, seekEnd));
        jmtc.updateDisplay();
    }

    public void setState(JMTCPlayingState state) {
        jmtc.setPlayingState(state);
        jmtc.updateDisplay();
    }
}