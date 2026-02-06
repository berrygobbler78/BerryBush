package com.berrygobbler78.flacplayer;

import com.berrygobbler78.flacplayer.Enums.*;

import java.io.File;
import java.util.*;

import com.berrygobbler78.flacplayer.controllers.MainController;
import com.berrygobbler78.flacplayer.controllers.PreviewTabController;
import com.berrygobbler78.flacplayer.userdata.Playlist;
import com.berrygobbler78.flacplayer.util.FileUtils;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

public final class MusicPlayer {
    private String albumPath;
    private File albumFile;

    private Playlist playlist;

    private String currentSongPath;

    private final String wavPath = "src/main/resources/com/berrygobbler78/flacplayer/cache/temp.wav";

    private ArrayList<String> nextSongsQueue;
    private ArrayList<String> previousSongsQueue;
    private ArrayList<String> userQueue;

    private boolean repeatAllSelected = false;
    private boolean repeatOneSelected = false;

    private boolean shuffleSelected = false;

    private MainController controller;
    private PreviewTabController previewTabController;

    private MediaPlayer mediaPlayer;

    private Timeline songTimeline;

    private Enums.PARENT_TYPE parentType;
    private Enums.REPEAT_STATUS repeatStatus = Enums.REPEAT_STATUS.OFF;

    private Random random = new Random();

    private boolean playing = false;

    public MusicPlayer() {

        nextSongsQueue = new ArrayList<>();
        userQueue = new ArrayList<>();
        previousSongsQueue = new ArrayList<>();
    }

    public void setController(MainController controller) {
        this.controller = controller;
    }

    public void addToUserQueue(String songPath) {
        File file =  new File(songPath);

        if(file.isDirectory()) {
            try {
                if(file.getAbsolutePath().equals(albumPath)) {
                    return;
                }
                for (File f : file.listFiles(FileUtils.getFileFilter(FILTER_TYPE.FLAC))) {
                    userQueue.add(f.getAbsolutePath());
                }
            } catch (NullPointerException e) {
                System.err.println("Folder provided is empty: " + e.getMessage());
            }
        } else if(file.getName().endsWith(".flac")) {
            userQueue.addLast(file.getAbsolutePath());
        }

        if(!isPlaying()) {
            loadSong(userQueue.getFirst());
        }
    }

    public void playFirstSong() {
        switch(parentType) {
            case ALBUM:
                loadSong(new File(albumPath).listFiles()[0].getAbsolutePath());
                play();
                break;
            case PLAYLIST:
                loadSong(playlist.getSongList().getFirst());
                play();
                break;
        }

        refreshSongQueue();
    }

    public void playSongNum(int num) {
        switch(parentType) {
            case ALBUM:
                loadSong(albumFile.listFiles()[num].getAbsolutePath());
                play();
                break;
            case PLAYLIST:
                loadSong(playlist.getSongList().get(num));
                play();
                break;
        }

        refreshSongQueue();
    }

    public void setParentTypeAlbum(String albumPath) {
        this.albumPath = albumPath;
        this.albumFile = new File(this.albumPath);
        this.parentType = PARENT_TYPE.ALBUM;
    }

    public void setParentTypePlaylist(Playlist playlist) {
        this.playlist = playlist;
        this.parentType = PARENT_TYPE.PLAYLIST;
    }

    public void pauseTimeline() {
        if (songTimeline != null) {
            songTimeline.pause();
        }
    }

    public void refreshSongQueue() {
        nextSongsQueue = new ArrayList<>();
        previousSongsQueue = new ArrayList<>();
        boolean add = false;

        switch (parentType) {
            case ALBUM:
                for(File file : albumFile.listFiles(FileUtils.getFileFilter(FILTER_TYPE.FLAC))) {
                    if(add) {
                        nextSongsQueue.add(file.getAbsolutePath());
                    } else {
                        previousSongsQueue.add(file.getAbsolutePath());
                    }
                    if(file.getAbsolutePath().equals(currentSongPath)) {
                        add = true;
                    }
                }
                break;
            case PLAYLIST:
                for(String path : playlist.getSongList()) {
                    if(add) {
                        nextSongsQueue.add(path);
                    } else  {
                        previousSongsQueue.add(path);
                    }
                    if(path.equals(currentSongPath)) {
                        add = true;
                    }
                }
        }


        if(shuffleSelected) {
            shuffle();
        }
    }

    public void loadSong(String songPath) {
        if(mediaPlayer != null) {
            mediaPlayer.dispose();
        }

        if(songTimeline != null) {
            songTimeline.stop();
        }

        // System.out.println("Loading song " + fileUtils.getSongTitle(f));

        FileUtils.flacToWav(songPath, "src/main/resources/com/berrygobbler78/flacplayer/cache/temp.wav");
        mediaPlayer = new MediaPlayer(new Media(new File(wavPath).toURI().toString()));
        mediaPlayer.setOnEndOfMedia(this::next);
        mediaPlayer.setOnPlaying(()-> {
            playing = true;
            controller.updateBottomBar();
            if(previewTabController != null) {
                previewTabController.setPlayPauseImageViewPaused(false);
            }
        });
        mediaPlayer.setOnPaused(()-> {
            playing = false;
            controller.updateBottomBar();
            if(previewTabController != null) {
                previewTabController.setPlayPauseImageViewPaused(true);
            }
        });
        mediaPlayer.setOnStopped(()-> {
            playing = false;
            controller.updateBottomBar();
            if(previewTabController != null) {
                previewTabController.setPlayPauseImageViewPaused(true);
            }
        });

        currentSongPath = songPath;
    }

    public void setPreviewTabController(PreviewTabController controller) {
        if(previewTabController != null) {
            previewTabController.setPlayPauseImageViewPaused(true);
        }
        this.previewTabController = controller;
    }

    public String getCurrentSongPath() {
        return currentSongPath;
    }

    public void play() {
        if(mediaPlayer != null) {

            // Make a new timeline if not open
            if(songTimeline == null || songTimeline.getStatus() == Timeline.Status.STOPPED) {
                songTimeline = new Timeline(new KeyFrame(Duration.millis(200), ae -> {
                            controller.setCurrentTrackTime((int) mediaPlayer.getCurrentTime().toSeconds());
                            controller.setSongProgressSliderPos((int) mediaPlayer.getCurrentTime().toMillis(), (int) mediaPlayer.getTotalDuration().toMillis());
                        }
                ));
                songTimeline.setCycleCount(Timeline.INDEFINITE);
            }

            if(mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                return;
            }

            if (mediaPlayer.getStatus() != MediaPlayer.Status.PAUSED || mediaPlayer.getStatus() == MediaPlayer.Status.STOPPED) {
                mediaPlayer.setOnReady(() -> {
                    // Player is ready to play the media
                    controller.setTotTrackTime((int) mediaPlayer.getTotalDuration().toSeconds());

                    songTimeline.play();
                    mediaPlayer.play();
                    playing = true;
                });
            } else if (mediaPlayer.getStatus() == MediaPlayer.Status.PAUSED) {
                songTimeline.play();
                mediaPlayer.play();
                playing = true;

            }

            if(previewTabController != null) {
                previewTabController.setPlayPauseImageViewPaused(false);
            }
        }
    }

    public void pause() {
        if(mediaPlayer != null) {
            songTimeline.pause();
            controller.setCurrentTrackTime((int) mediaPlayer.getCurrentTime().toSeconds());
            mediaPlayer.pause();
            playing = false;

            if(previewTabController != null) {

                previewTabController.setPlayPauseImageViewPaused(true);
            }
        }
    }

    public void stop() {
        if(mediaPlayer != null) {
            songTimeline.stop();
            mediaPlayer.stop();
            playing = false;

            if(previewTabController != null) {
                previewTabController.setPlayPauseImageViewPaused(true);
            }
        }
    }

    public void next() {
        if(repeatOneSelected) {
            stop();
            mediaPlayer.seek(Duration.ZERO);
            play();
        } else {
            stop();
            if(!userQueue.isEmpty()) {
                previousSongsQueue.addFirst(currentSongPath);
                loadSong(userQueue.getFirst());
                userQueue.removeFirst();
                play();
            } else if(!nextSongsQueue.isEmpty()) {
                previousSongsQueue.addFirst(currentSongPath);
                loadSong(nextSongsQueue.getFirst());
                nextSongsQueue.removeFirst();
                play();
            } else if(repeatAllSelected) {
                previousSongsQueue.addFirst(currentSongPath);
                playFirstSong();
            }
        }
    }

    public void previous() {
        if (mediaPlayer.getCurrentTime().toSeconds() > 3) {
            mediaPlayer.seek(Duration.ZERO);
            play();
        } else if(!previousSongsQueue.isEmpty()) {
            nextSongsQueue.addFirst(currentSongPath);
            loadSong(previousSongsQueue.getFirst());
            previousSongsQueue.removeFirst();
            play();
        }
    }

    public void setRepeatStatus(Enums.REPEAT_STATUS repeatStatus) {
        switch(repeatStatus) {
            case OFF:
                repeatAllSelected = false;
                repeatOneSelected = false;
                break;
            case REPEAT_ALL:
                repeatAllSelected = true;
                repeatOneSelected = false;
                break;
            case REPEAT_ONE:
                repeatAllSelected = false;
                repeatOneSelected = true;
                break;
        }
    }

    public void setShuffleStatus(boolean shuffleStatus) {
        shuffleSelected = shuffleStatus;
    }

    public void shuffle() {
        ArrayList<String> temp = new ArrayList<>();
        temp.addAll(nextSongsQueue);
        nextSongsQueue.clear();
        while(!temp.isEmpty()) {
            nextSongsQueue.add(temp.remove(random.nextInt(temp.size())));
        }
    }

    public void closeMediaPlayer() {
        mediaPlayer.dispose();
    }

    public boolean isPlaying() {
        System.out.println(playing);
        return playing;
    }

    public int getSongPosFromSlider(int value) {
        if(mediaPlayer != null) {
            return (int) mediaPlayer.getStartTime().add(mediaPlayer.getTotalDuration().multiply(value / 100.0)).toSeconds();
        } else {
            return 0;
        }
    }

    public void changeSongPos(double pos) {
        if(mediaPlayer != null) {
            mediaPlayer.seek(mediaPlayer.getStartTime().add(mediaPlayer.getTotalDuration().multiply(pos / 100.0)));
            if(songTimeline != null) {
                songTimeline.play();
            }
        }
    }
}
