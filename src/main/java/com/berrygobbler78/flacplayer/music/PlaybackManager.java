package com.berrygobbler78.flacplayer.music;

import com.berrygobbler78.flacplayer.App;
import com.berrygobbler78.flacplayer.util.FlacDecoder;
import com.berrygobbler78.flacplayer.util.ResourceHandler;
import com.berrygobbler78.flacplayer.util.records.Song;
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
import java.nio.file.Path;
import java.util.Optional;

public class PlaybackManager {
    private final static Logger logger = LogManager.getLogger();

    private final MusicInterface musicInterface;

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
        Optional<Song> next = queueManager.getNextSong(false);

        if (!skipCurrent && current == null) {
            logger.warn("No current song available to load.");
            return;
        }

        App.submitTask(() -> {
            if(!skipCurrent) {
                try {
                    if(currentMediaPlayer != null) dispose(currentMediaPlayer);
                    Optional<Path> temp = new FlacDecoder().flacToWav(current.path());
                    if (temp.isEmpty()) return;

                    Platform.runLater(() -> {
                        currentMediaPlayer = new MediaPlayer(new Media(temp.get().toUri().toString()));
                        initMediaPlayer(currentMediaPlayer);
                        if (playOnFinish) play();
                    });

                    logger.info("Loaded '{}' ", current.title());
                } catch (IOException e) {
                    logger.warn("Failed to load '{}' | {}", current.title(), e.getMessage());
                }
            }

            if(next.isPresent()) {
                try {
                    var temp = new FlacDecoder().flacToWav(next.get().path());
                    if (temp.isEmpty()) return;
                    nextMediaPlayer = new MediaPlayer(new Media(temp.get().toUri().toString()));
                    initMediaPlayer(nextMediaPlayer);

                    logger.warn("Pre-loaded '{}' ", next.get().title());
                } catch (IOException e) {
                    logger.warn("Failed to pre-load '{}' | {}", next.get().title(), e.getMessage());
                }
            }
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

    public void dispose(MediaPlayer mediaPlayer) {
        if(mediaPlayer == null) return;

        var file = new File(mediaPlayer.getMedia().getSource());
        if(file.delete()) {
            logger.debug("Disposed {}", file.getName());
        } else {
            logger.error("Failed to dispose {}", file.getName());
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
        if(currentMediaPlayer == null) return;

        if(queueManager.getRepeatStatus() == QueueManager.REPEAT_STATUS.REPEAT_ONE) {
            currentMediaPlayer.seek(Duration.ZERO);
            return;
        }

        logger.info("Playing next...");

        dispose(currentMediaPlayer);
        currentMediaPlayer = nextMediaPlayer;

        play();

        if(queueManager.getNextSong(true).isPresent()) load(false, true);
    }

    public void previous() {
        if(currentMediaPlayer == null) return;
        if(currentMediaPlayer.getCurrentTime().toSeconds() > 3) {
            seek(0);
            return;
        }

        if(queueManager.getPreviousSong(true).isEmpty()) {
            seek(0);
        } else {
            logger.info("Playing previous...");

            dispose(currentMediaPlayer);

            load(true, false);
        }
    }

    public void seek(double pos) {
        if(currentMediaPlayer == null) return;
        var seconds = Duration.seconds(pos / 100.0 * totalDuration);
        logger.info("Seeking to '{}'", seconds.toSeconds());
        currentMediaPlayer.seek(seconds);
    }

    public void setVolume(double volume) {
        if(currentMediaPlayer == null) return;
        logger.info("Setting volume to {}", volume);
        currentMediaPlayer.setVolume(volume);
    }

    public MediaPlayer.Status getStatus() {
        if(currentMediaPlayer == null) return MediaPlayer.Status.UNKNOWN;
        return currentMediaPlayer.getStatus();
    }

    public void clearTempFiles() {
        File[] tempFiles = ResourceHandler.get(ResourceHandler.ResourceType.TEMP).listFiles();
        if(tempFiles == null) return;
        for(File f : tempFiles) {
            if(f.delete()) {
                logger.debug("Deleted temp file: {}", f.getName());
            } else {
                logger.error("Failed to delete temp file: {}", f.getName());
            }
        }
    }
}
