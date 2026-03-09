package com.berrygobbler78.flacplayer.util;

import com.berrygobbler78.flacplayer.configuration.PlaylistDataHandler;
import com.berrygobbler78.flacplayer.util.Constants.*;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.berrygobbler78.flacplayer.controllers.MainController;
import com.berrygobbler78.flacplayer.controllers.PreviewTabController;
import com.berrygobbler78.flacplayer.util.handlers.MediaTransportHandler;
import com.berrygobbler78.flacplayer.util.handlers.ResourceHandler;
import com.berrygobbler78.flacplayer.util.records.Album;
import com.berrygobbler78.flacplayer.util.records.Song;
import io.github.selemba1000.JMTCParameters;
import io.github.selemba1000.JMTCPlayingState;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

public final class MusicPlayer {
   private final Logger LOGGER = Logger.getLogger(MusicPlayer.class.getName());

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
   private PlaylistDataHandler.Playlist currentPlaylist;
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
       currentAlbum = album;
       currentParentType = PARENT_TYPE.ALBUM;
   }

   public void setPlaylist(PlaylistDataHandler.Playlist playlist) {
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
               // for(String song : currentPlaylist.getSongs()) {
               //     if(song.equals(currentPlaylist.getSongs().get(index))) {
               //         loadSong(song, playAfter);
               //         add = true;
               //     } else if(add) {
               //         nextSongsQueue.add(song);
               //     } else  {
               //         previousSongsQueue.add(song);
               //     }
               // }
       }


       if(shuffleStatus == SHUFFLE_STATUS.SHUFFLE) {
           shuffle();
       }

       LOGGER.info(
               "Generated new parent queue!\n" +
                       String.format("NextQueue length = [%s]\n", nextSongsQueue.size()) +
                       String.format("PrevQueue length = [%s]\n", previousSongsQueue.size())
               );
   }

   public void clearQueues() {
       previousSongsQueue.clear();
       nextSongsQueue.clear();
   }

   // Play utils

   public void playFirstSong() {
       LOGGER.log(Level.INFO, "Playing first song");
       generateParentQueue(0, true);
   }

   public void playSongNum(int index) {
       LOGGER.log(Level.INFO, String.format("Playing [%s]", index));
       generateParentQueue(index, true);
   }

   public void pauseTimeline() {
       if (songTimeline != null) {
           songTimeline.pause();
       }
   }

   public void loadSong(Song song, boolean playAfter) {
       LOGGER.info(String.format("Loading song: [%s]", song.title()));

       Song requestedSong = song;

       if(mediaPlayer != null) {
           mediaPlayer.dispose();
       }

       if(songTimeline != null) {
           songTimeline.stop();
       }

       CompletableFuture.runAsync(() -> {
           try {
               currentSong = requestedSong;
               String wavPath = FileUtils.flacToWav(currentSong.path()).getAbsolutePath();

               Platform.runLater(() -> {
                   if(requestedSong != currentSong) return;
                   mediaPlayer = new MediaPlayer(new Media(new File(wavPath).toURI().toString()));
                   mediaPlayer.setOnEndOfMedia(this::next);

                   mediaPlayer.setOnPlaying(()-> {
                       musicPlayerStatus = MUSIC_PLAYER_STATUS.PLAYING;
                       if(songTimeline != null) {
                           songTimeline.play();
                       }

                       // Update gui
                       mainController.updateBottomBar();
                       if(previewTabController != null) {
                           previewTabController.setPaused(false);
                       }

                       // Update transport
                       mediaTransportHandler.setState(JMTCPlayingState.PLAYING);
                       updateJMTC();

                   });

                   mediaPlayer.setOnPaused(()-> {
                       musicPlayerStatus = MUSIC_PLAYER_STATUS.PAUSED;
                       if(songTimeline != null) {
                           songTimeline.pause();
                       }

                       // Update gui
                       mainController.updateBottomBar();
                       if(previewTabController != null) {
                           previewTabController.setPaused(true);
                       }

                       // Update transport
                       mediaTransportHandler.setState(JMTCPlayingState.PAUSED);
                       updateJMTC();

                   });

                   mediaPlayer.setOnStopped(()-> {
                       musicPlayerStatus = MUSIC_PLAYER_STATUS.STOPPED;
                       if(songTimeline != null) {
                           songTimeline.stop();
                       }

                       // Update gui
                       mainController.updateBottomBar();
                       if(previewTabController != null) {
                           previewTabController.setPaused(true);
                       }

                       // Update transport
                       mediaTransportHandler.setState(JMTCPlayingState.STOPPED);
                       updateJMTC();

                   });

                   if(playAfter) play();

                   LOGGER.info("Loading done...");
               });

           } catch (IOException e) {
               e.printStackTrace();
           }
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
           return;
       }

       // Make a new timeline if not open
       if(songTimeline == null || songTimeline.getStatus() == Timeline.Status.STOPPED) {
           songTimeline = new Timeline(new KeyFrame(Duration.millis(200), _ -> {
                       mainController.setCurrentTrackTime((int) mediaPlayer.getCurrentTime().toSeconds());
                       mainController.setSongProgressSliderPos((int) mediaPlayer.getCurrentTime().toMillis(), (int) mediaPlayer.getTotalDuration().toMillis());
                   }
           ));

           songTimeline.setCycleCount(Timeline.INDEFINITE);
       }

       if (mediaPlayer.getStatus() != MediaPlayer.Status.PAUSED || mediaPlayer.getStatus() == MediaPlayer.Status.STOPPED) {
           mediaPlayer.setOnReady(() -> {
               // Player is ready to play the media
               mainController.setTotTrackTime((int) mediaPlayer.getTotalDuration().toSeconds());

               songTimeline.play();
               mediaPlayer.play();

               LOGGER.info("First play");
           });
       } else if (mediaPlayer.getStatus() == MediaPlayer.Status.PAUSED) {
           songTimeline.play();
           mediaPlayer.play();

           LOGGER.info("Resume");
       }

       if(previewTabController != null) {
           previewTabController.setPaused(false);
       }

   }

   public void pause() {
       if(mediaPlayer == null) return;

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
       if(mediaPlayer != null) {
           mediaPlayer.stop();

           if(previewTabController != null) {
               previewTabController.setPaused(true);
           }
       }
   }

   public void next() {
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
       if(mediaPlayer == null) {
           return;
       }

       Duration newTime = Duration.ZERO;

       try{
           newTime = mediaPlayer.getStartTime().add(mediaPlayer.getTotalDuration().multiply(pos / 100.0));
       } catch (NullPointerException e){
           LOGGER.log(Level.WARNING, mediaPlayer.toString() + ".getStartTime() produced NullPointerException.");
       }

       mediaPlayer.seek(newTime);

       if(songTimeline != null) {
           songTimeline.play();
       }
   }
}
