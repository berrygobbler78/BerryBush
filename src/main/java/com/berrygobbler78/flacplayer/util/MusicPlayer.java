package com.berrygobbler78.flacplayer.util;

import com.berrygobbler78.flacplayer.util.Constants.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import com.berrygobbler78.flacplayer.controllers.MainController;
import com.berrygobbler78.flacplayer.controllers.PreviewTabController;
import com.berrygobbler78.flacplayer.util.handlers.MediaTransportHandler;
import com.berrygobbler78.flacplayer.util.handlers.ResourceHandler;
import com.berrygobbler78.flacplayer.util.records.Album;
import com.berrygobbler78.flacplayer.util.records.Playlist;
import com.berrygobbler78.flacplayer.util.records.Song;
import io.github.selemba1000.JMTCPlayingState;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class MusicPlayer {
    private final static Logger logger = LogManager.getLogger();

   public enum REPEAT_STATUS {
       OFF,
       REPEAT_ALL,
       REPEAT_ONE
   }

   public enum SHUFFLE_STATUS {
       OFF,
       SHUFFLE
   }

   public enum MUSIC_PLAYER_STATUS {
       STOPPED,
       PLAYING,
       PAUSED
   }

   private Song currentSong;

   // Parent information
   private PARENT_TYPE currentParentType;
   private Playlist currentPlaylist;
   private Album currentAlbum;

   private final MediaTransportHandler mediaTransportHandler = new MediaTransportHandler("BerryBush", "/", this);

   // Queueing
   private ArrayList<Song> previousSongsQueue = new ArrayList<>();
   private ArrayList<Song> userQueue = new ArrayList<>();
   private ArrayList<Song> nextSongsQueue = new ArrayList<>();

   // Controllers
   private MainController mainController;
   private PreviewTabController previewTabController;

   // Utilities
   private MediaPlayer mediaPlayer;
   private Timeline songTimeline;

   // Statuses
   private SHUFFLE_STATUS shuffleStatus = SHUFFLE_STATUS.OFF;
   private REPEAT_STATUS repeatStatus = REPEAT_STATUS.OFF;
   private MUSIC_PLAYER_STATUS musicPlayerStatus = MUSIC_PLAYER_STATUS.STOPPED;

   private final Random random = new Random();

   public MusicPlayer(MainController mainController) {
       setMainController(mainController);
   }

   public MediaTransportHandler getMediaTransportHandler() {
       return mediaTransportHandler;
   }

   // Controllers

   public void setMainController(MainController controller) {
       mainController = controller;
   }

   public void setPreviewTabController(PreviewTabController controller) {
       previewTabController = controller;
   }

   public void setAlbum(Album album) {
       logger.info("Music player parent set to '{}'", album.title());
       currentAlbum = album;
       currentParentType = PARENT_TYPE.ALBUM;
   }

   public void setPlaylist(Playlist playlist) {
       logger.info("Music player parent set to '{}'", playlist.title());
       currentPlaylist = playlist;
       currentParentType = PARENT_TYPE.PLAYLIST;
   }

   // Queueing

   public void addToUserQueue(Song song) {
       userQueue.add(song);

       if(getMusicPlayerState() != MUSIC_PLAYER_STATUS.PLAYING) {
           loadSong(userQueue.getFirst(), true);
       }
   }

   public void generateParentQueue(int index, boolean playAfter) {
       clearQueues();

       boolean add = false;

       switch (currentParentType) {
           case ALBUM:
               int i = 0;
               for(Song song : currentAlbum.songs()) {
                   if(i == index) {
                       loadSong(song, playAfter);
                       add = true;
                   } else if(add) {
                       nextSongsQueue.add(song);
                   } else {
                       previousSongsQueue.add(song);
                   }
                   i++;
               }

               break;
           case PLAYLIST:
               int j = 0;
               for(Song song : currentPlaylist.songs()) {
                   if(j == index) {
                       loadSong(song, playAfter);
                       add = true;
                   } else if(add) {
                       nextSongsQueue.add(song);
                   } else {
                       previousSongsQueue.add(song);
                   }
                   j++;
               }
       }


       if(shuffleStatus == SHUFFLE_STATUS.SHUFFLE) {
           shuffle();
       }

       logger.info("Generated new parent queue!\n{}{}",
               String.format("NextQueue length = [%s]\n", nextSongsQueue.size()),
               String.format("PrevQueue length = [%s]\n", previousSongsQueue.size()));
   }

   public void clearQueues() {
       previousSongsQueue.clear();
       nextSongsQueue.clear();
   }

   // Play utils

   public void playFirstSong() {
       logger.info("Playing first song");
       generateParentQueue(0, true);
   }

   public void playSongNum(int index) {
       logger.info("Playing song '{}'", index);
       generateParentQueue(index, true);
   }

   public void pauseTimeline() {
       if (songTimeline != null) {
           songTimeline.pause();
       }
   }

    public void loadSong(Song song, boolean playAfter) {
        if (song == null) {
            logger.error("Cannot load null song");
            return;
        }

        logger.info("Loading '{}'", song.title());
        currentSong = song;

        Platform.runLater(() -> {
            if (mediaPlayer != null) {
                mediaPlayer.dispose();
                mediaPlayer = null;
            }

            if (songTimeline != null) {
                songTimeline.stop();
            }
        });

        CompletableFuture
                .supplyAsync(() -> {
                    try {
                        return FileUtils.flacToWav(song.path()).toURI().toString();
                    } catch (IOException e) {
                        logger.error("Failed to load '{}' : {}", song.title(), e.getMessage());
                        return null;
                    }
                })
                .thenAccept(mediaUri -> {
                    if (mediaUri == null) {
                        return;
                    }

                    Platform.runLater(() -> {
                        if (currentSong != song) {
                            logger.debug("Discarding stale load request for '{}'", song.title());
                            return;
                        }

                        mediaPlayer = new MediaPlayer(new Media(mediaUri));
                        mediaPlayer.setOnEndOfMedia(this::next);

                        mediaPlayer.setOnReady(() -> {
                            mainController.setTotTrackTime((int) mediaPlayer.getTotalDuration().toSeconds());

                            if (playAfter && currentSong == song) {
                                play();
                            }
                        });

                        mediaPlayer.setOnPlaying(() -> {
                            musicPlayerStatus = MUSIC_PLAYER_STATUS.PLAYING;
                            if (songTimeline != null) {
                                songTimeline.play();
                            }

                            mainController.updateBottomBar();
                            if (previewTabController != null) {
                                previewTabController.setPaused(false);
                            }

                            mediaTransportHandler.setState(JMTCPlayingState.PLAYING);
                            updateJMTC();
                        });

                        mediaPlayer.setOnPaused(() -> {
                            musicPlayerStatus = MUSIC_PLAYER_STATUS.PAUSED;
                            if (songTimeline != null) {
                                songTimeline.pause();
                            }

                            mainController.updateBottomBar();
                            if (previewTabController != null) {
                                previewTabController.setPaused(true);
                            }

                            mediaTransportHandler.setState(JMTCPlayingState.PAUSED);
                            updateJMTC();
                        });

                        mediaPlayer.setOnStopped(() -> {
                            musicPlayerStatus = MUSIC_PLAYER_STATUS.STOPPED;
                            if (songTimeline != null) {
                                songTimeline.stop();
                            }

                            mainController.updateBottomBar();
                            if (previewTabController != null) {
                                previewTabController.setPaused(true);
                            }

                            mediaTransportHandler.setState(JMTCPlayingState.STOPPED);
                            updateJMTC();
                        });
                    });
                });
    }

   public void setVolume(double volume) {
       if(mediaPlayer != null) {
           mediaPlayer.setVolume(volume);
       }
   }

   public Song getCurrentSong() {
       return currentSong;
   }

   public void play() {
       if(mediaPlayer == null) return;

       if(mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
           logger.info("Already playing, can't play...");
           return;
       }

       // Make a new timeline if not open
       if(songTimeline == null || songTimeline.getStatus() == Timeline.Status.STOPPED) {
           logger.debug("Started timeline");
           songTimeline = new Timeline(new KeyFrame(Duration.millis(200), _ -> {
                       mainController.setCurrentTrackTime((int) mediaPlayer.getCurrentTime().toSeconds());
                       mainController.setSongProgressSliderPos((int) mediaPlayer.getCurrentTime().toMillis(), (int) mediaPlayer.getTotalDuration().toMillis());
                   }
           ));

           songTimeline.setCycleCount(Timeline.INDEFINITE);
       }

       if (mediaPlayer.getStatus() == MediaPlayer.Status.STOPPED || mediaPlayer.getStatus() == MediaPlayer.Status.READY) {
           logger.info("First play...");
           mainController.setTotTrackTime((int) mediaPlayer.getTotalDuration().toSeconds());

           songTimeline.play();
           mediaPlayer.play();
       } else if (mediaPlayer.getStatus() == MediaPlayer.Status.PAUSED) {
           logger.info("Resuming...");

           songTimeline.play();
           mediaPlayer.play();
       }

       if(previewTabController != null) {
           previewTabController.setPaused(false);
       }

   }

   public void pause() {
       if(mediaPlayer == null) return;
       logger.info("Paused...");

       if(songTimeline != null) {
           songTimeline.pause();
       }

       mainController.setCurrentTrackTime((int) mediaPlayer.getCurrentTime().toSeconds());
       mediaPlayer.pause();

       if(previewTabController != null) {

           previewTabController.setPaused(true);
       }
   }

   public void stop() {
       if(mediaPlayer == null) return;
       logger.info("Stopped...");

       mediaPlayer.stop();

       if(previewTabController != null) {
           previewTabController.setPaused(true);
       }
   }

   public void next() {
       logger.info("Next...");
       if(repeatStatus == REPEAT_STATUS.REPEAT_ONE) {
           mediaPlayer.seek(Duration.ZERO);
           return;
       }

       stop();

       if(!userQueue.isEmpty()) {
           previousSongsQueue.addFirst(currentSong);
           loadSong(userQueue.getFirst(), true);
           userQueue.removeFirst();

       } else if(!nextSongsQueue.isEmpty()) {
           previousSongsQueue.addFirst(currentSong);
           loadSong(nextSongsQueue.getFirst(), true);
           nextSongsQueue.removeFirst();
       } else if(repeatStatus == REPEAT_STATUS.REPEAT_ALL) {
           previousSongsQueue.addFirst(currentSong);
           playFirstSong();
       } else if(currentParentType == PARENT_TYPE.PLAYLIST) {

       }
   }

   public void previous() {
       logger.info("Previous...");
       if(mediaPlayer == null) return;

       if (mediaPlayer.getCurrentTime().toSeconds() > 3) {
           mediaPlayer.seek(Duration.ZERO);
       } else if(!previousSongsQueue.isEmpty()) {
           nextSongsQueue.addFirst(currentSong);
           loadSong(previousSongsQueue.getFirst(), true);
           previousSongsQueue.removeFirst();
       }
   }

   // Repeat status control

   public void cycleRepeatStatus() {
       switch (repeatStatus) {
           case REPEAT_ONE:
               repeatStatus = REPEAT_STATUS.OFF;
               break;
           case REPEAT_ALL:
               repeatStatus = REPEAT_STATUS.REPEAT_ONE;
               break;
           case OFF:
               repeatStatus = REPEAT_STATUS.REPEAT_ALL;
               break;
       }

       mainController.updateRepeatButton();
       logger.info("Repeat set to '{}'", repeatStatus);
   }

   public void setRepeatStatus(REPEAT_STATUS status) {
       repeatStatus = status;

       mainController.updateRepeatButton();
   }

   public REPEAT_STATUS getRepeatStatus() {
       return repeatStatus;
   }

   // Shuffle status control

   public void toggleShuffleStatus() {
       switch (shuffleStatus) {
           case OFF ->   shuffleStatus = SHUFFLE_STATUS.SHUFFLE;
           case SHUFFLE ->   shuffleStatus = SHUFFLE_STATUS.OFF;
       }

       mainController.updateShuffleButton();

       logger.info("Shuffle set to '{}'", shuffleStatus);

       if(shuffleStatus == SHUFFLE_STATUS.SHUFFLE && !nextSongsQueue.isEmpty()) {
           shuffle();
       }
   }

   public void setShuffleStatus(boolean shuffle) {
       if(shuffle) {
           shuffleStatus = SHUFFLE_STATUS.SHUFFLE;
       } else {
           shuffleStatus = SHUFFLE_STATUS.OFF;
       }

       mainController.updateShuffleButton();
   }

   public SHUFFLE_STATUS getShuffleStatus() {
       return shuffleStatus;
   }

   public void shuffle() {
       logger.info("Shuffling...");
       ArrayList<Song> temp = new ArrayList<>(nextSongsQueue);
       nextSongsQueue.clear();

       while(!temp.isEmpty()) {
           nextSongsQueue.add(temp.remove(random.nextInt(temp.size())));
       }
   }

   public void updateJMTC() {
       if(currentSong == null) return;

       mediaTransportHandler.setProperties(
               currentSong.title(),
               currentSong.artist().name(),
               currentSong.album().title(),
               currentSong.artist().name(),
               currentSong.album().songs().size(),
               currentSong.track(),
               ResourceHandler.getResourceFile(currentSong.album().imagePath())
       );
   }

   public void closeMediaPlayer() {
       if(mediaPlayer == null) return;
       mediaPlayer.dispose();
   }

   public MUSIC_PLAYER_STATUS getMusicPlayerState() {
       return musicPlayerStatus;
   }

   public int getSongPosFromSlider(int value) {
       if(mediaPlayer == null) return -1;

       try {
           return (int) mediaPlayer.getStartTime().add(mediaPlayer.getTotalDuration().multiply(value / 100.0)).toSeconds();
       } catch (IllegalStateException e) {
           return -1;
       }
   }

   public void changeSongPos(double pos) {
       if(mediaPlayer == null) return;

       Duration newTime = Duration.ZERO;

       try{
           newTime = mediaPlayer.getStartTime().add(mediaPlayer.getTotalDuration().multiply(pos / 100.0));
       } catch (NullPointerException e){
           logger.error("getStartTime() produced NullPointerException");
       }

       mediaPlayer.seek(newTime);

       if(songTimeline != null) {
           songTimeline.play();
       }
   }
}
