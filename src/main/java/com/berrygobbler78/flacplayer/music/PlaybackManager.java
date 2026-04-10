package com.berrygobbler78.flacplayer.music;

import com.berrygobbler78.flacplayer.App;
import com.berrygobbler78.flacplayer.util.FileUtils;
import io.github.selemba1000.JMTCPlayingState;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URI;

public class PlaybackManager {
    private final static Logger logger = LogManager.getLogger();

    private final MusicInterface musicInterface;

    private URI currentURI;
    private URI nextURI;

    private MediaPlayer currentMediaPlayer;
    private MediaPlayer nextMediaPlayer;

    private final QueueManager queueManager;

    // Timeline

    private Timeline timeline;
    private double totalDuration;

    public PlaybackManager(MusicInterface musicInterface, QueueManager queueManager) {
        this.musicInterface = musicInterface;
        this.queueManager = queueManager;
        // timeline = new Timeline(new KeyFrame(Duration.seconds(0.5), _ -> musicInterface.updateTimeline(
        //                 (float) currentMediaPlayer.getCurrentTime().toSeconds(),
        //                 (float) totalDuration)));
        // timeline.setCycleCount(Timeline.INDEFINITE);
    }

    public void load(boolean playOnFinish, boolean skipCurrent) {
        var current = queueManager.getCurrentSong();
        var next = queueManager.getNextSong(false);

        if (!skipCurrent && current == null) {
            logger.warn("No current song available to load.");
            return;
        }

        if(!skipCurrent) App.submitTask(() -> {
            logger.debug("Loading '{}'", current.title());

            try {
                File currTemp = FileUtils.flacToWav(current.path());

                if(currentMediaPlayer != null) currentMediaPlayer.dispose();

                Platform.runLater(() -> {
                    if(currentMediaPlayer != null) currentMediaPlayer.dispose();
                    currentMediaPlayer = new MediaPlayer(new Media(currTemp.toURI().toString()));
                    initMediaPlayer(currentMediaPlayer);
                    if (playOnFinish) play();
                });

                logger.info("Loaded '{}' ", current.title());
            } catch (IOException e) {
                logger.warn("Failed to load '{}' | {}", current.title(), e.getMessage());
            }

            return null;
        });

        if(next != null) App.submitTask(() -> {
            logger.debug("Pre-loading '{}'", next.title());

            try {
                File nextTemp = FileUtils.flacToWav(next.path());
                nextURI = nextTemp.toURI();
                if(nextMediaPlayer != null) nextMediaPlayer.dispose();
                nextMediaPlayer = new MediaPlayer(new Media(nextURI.toString()));
                initMediaPlayer(nextMediaPlayer);
                logger.warn("Pre-loaded '{}' ", next.title());
            } catch (IOException e) {
                logger.warn("Failed to pre-load '{}' | {}", next.title(), e.getMessage());
            }
            return null;
        });
    }

    public void initMediaPlayer(MediaPlayer mediaPlayer) {
        mediaPlayer.setOnEndOfMedia(() -> {
            next();

            musicInterface.updateJMTC();
            musicInterface.updateJMTCState(JMTCPlayingState.STOPPED);
        });

        mediaPlayer.setOnPaused(() -> {
            if(timeline != null) timeline.pause();

            musicInterface.updateJMTC();
            musicInterface.updateJMTCState(JMTCPlayingState.PAUSED);
            musicInterface.updatePaused(true);
        });

        mediaPlayer.setOnPlaying(() -> {
            if(timeline != null) timeline.play();

            musicInterface.updateJMTC();
            musicInterface.updateJMTCState(JMTCPlayingState.PLAYING);
            musicInterface.updatePaused(false);
        });

        mediaPlayer.setOnReady(() -> {
            totalDuration = mediaPlayer.getTotalDuration().toSeconds();

            musicInterface.updateJMTC();
            musicInterface.updateBottomBar(queueManager.getCurrentSong());
        });
    }

    public void disposeURI(URI uri) {
        if(uri == null) return;

        var file = new File(uri);
        if(file.delete()) {
            logger.info("Disposed {}", uri);
        } else {
            logger.error("Failed to dispose {}", uri);
        }
    }

    public void play() {
        if(currentMediaPlayer == null) return;
        logger.info("Playing...");
        currentMediaPlayer.play();
    }

    public void pause() {
        if(currentMediaPlayer == null) return;
        logger.info("Pausing...");
        currentMediaPlayer.pause();
    }

    public void stop() {
        if(currentMediaPlayer == null) return;
        logger.info("Stopping...");
        currentMediaPlayer.stop();
    }

    public void next() {
        if(nextMediaPlayer == null) return;
        logger.info("Playing next...");

        if(queueManager.getRepeatStatus() == QueueManager.REPEAT_STATUS.REPEAT_ONE) {
            currentMediaPlayer.seek(Duration.ZERO);
            return;
        }

        currentMediaPlayer.dispose();
        disposeURI(currentURI);

        currentMediaPlayer = nextMediaPlayer;
        currentURI = nextURI;
        currentMediaPlayer.play();

        queueManager.getNextSong(true);
        load(false, true);
    }

    public void previous() {
        if(currentMediaPlayer == null) return;
        logger.info("Playing previous...");

        currentMediaPlayer.dispose();
        disposeURI(currentURI);

        queueManager.getPreviousSong(true);
        load(true, false);
    }

    public void seek(double pos) {
        if(currentMediaPlayer == null) return;
        logger.info("Seeking to {}%", pos);
        currentMediaPlayer.seek(Duration.seconds(pos / 100.0));
    }

    public void setVolume(double volume) {
        if(currentMediaPlayer == null) return;
        logger.info("Setting volume to {}", volume);
        currentMediaPlayer.setVolume(volume);
    }

    public MediaPlayer.Status getStatus() {
        if(currentMediaPlayer == null) return null;
        return currentMediaPlayer.getStatus();
    }
}
