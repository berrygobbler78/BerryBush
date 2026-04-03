package com.berrygobbler78.flacplayer.music;

import com.berrygobbler78.flacplayer.util.FileUtils;
import com.berrygobbler78.flacplayer.util.records.Song;
import io.github.selemba1000.JMTCPlayingState;
import javafx.animation.Timeline;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
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

    private URI currentSongURI;
    private URI nextSongURI;

    private Song currentSong;

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
        Song currSong = queueManager.getCurrentSong();
        Song nextSong = queueManager.getNextSong(false);

        if (!skipCurrent && currSong == null) {
            logger.warn("No current song available to load.");
            return;
        }

        Service<Void> loadCurrentService = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<>() {
                    @Override
                    protected Void call() {
                        logger.info("Loading '{}'", currSong.title());

                        File currTemp = null;
                        try {
                            currTemp = FileUtils.flacToWav(currSong.path());
                        } catch (IOException e) {
                            cancel();
                        }

                        currentSongURI = currTemp.toURI();
                        currentMediaPlayer = new MediaPlayer(new Media(currentSongURI.toString()));
                        initMediaPlayer(currentMediaPlayer);

                        if (playOnFinish) play();
                        return null;
                    }
                };
            }
        };

        Service<Void> loadNextService = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<>() {
                    @Override
                    protected Void call() {

                        logger.info("Pre-loading '{}'", nextSong.title());

                        File nextTemp = null;
                        try {
                            nextTemp = FileUtils.flacToWav(nextSong.path());
                        } catch (IOException e) {
                            cancel();
                        }

                        nextSongURI = nextTemp.toURI();
                        nextMediaPlayer = new MediaPlayer(new Media(nextSongURI.toString()));
                        initMediaPlayer(nextMediaPlayer);
                        return null;
                    }
                };
            }
        };

        if(!skipCurrent) {
            stop();
            loadCurrentService.start();
        }

        if(nextSong != null) loadNextService.start();
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

        File file = new File(uri);
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
        new File(currentSongURI).delete();
        currentMediaPlayer = nextMediaPlayer;
        currentMediaPlayer.play();

        currentSong = queueManager.getNextSong(true);
        load(false, true);
    }

    public void previous() {
        if(currentMediaPlayer == null) return;
        logger.info("Playing previous...");

        currentMediaPlayer.dispose();
        new File(currentSongURI).delete();

        currentSong = queueManager.getPreviousSong(true);
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
