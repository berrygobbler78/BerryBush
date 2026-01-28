package com.berrygobbler78.flacplayer;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.*;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

public final class MusicPlayer {

    private File directory;
    private String directoryPath;

    private String currentSongPath;

    private final String wavPath = "src/main/resources/com/berrygobbler78/flacplayer/cache/temp.wav";

    private ArrayList<String> nextSongsQueue;
    private ArrayList<String> previousSongsQueue;
    private ArrayList<String> userQueue;

    public final FileUtils fileUtils;

    private boolean repeatAllSelected = false;
    private boolean repeatOneSelected = false;

    private boolean shuffleSelected = false;

    private Controller controller;
    private PreviewTabController previewTabController;

    private MediaPlayer mediaPlayer;

    private Timeline songTimeline;

    private ParentType parentType;
    private RepeatStatus repeatStatus = RepeatStatus.OFF;

    private Random random = new Random();

    private final FileFilter flacFilter = new FileFilter() {
        public boolean accept(File f)
        {
            return f.getName().endsWith("flac");
        }
    };

    public MusicPlayer() {
        fileUtils = App.fileUtils;

        nextSongsQueue = new ArrayList<>();
        userQueue = new ArrayList<>();
        previousSongsQueue = new ArrayList<>();
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }

    public void addToUserQueue(File file) {
        if(file.isDirectory()) {
            try {
                if(file.getAbsolutePath().equals(directory.getAbsolutePath())) {
                    return;
                }
                for (File f : file.listFiles(flacFilter)) {
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
        loadSong(directory.listFiles()[0].getAbsolutePath());
        refreshAlbumSongQueue();
    }

    public void playSongNum(int num) {
        loadSong(directory.listFiles()[num].getAbsolutePath());

        refreshAlbumSongQueue();
    }

    public void setDirectoryPath(String directoryPath, ParentType parentType) {
        System.out.println(directoryPath);
        this.directoryPath = directoryPath;
        this.directory = new File(directoryPath);

        switch (parentType) {
            case ALBUM:
                parentType = ParentType.ALBUM;
                break;
            case PLAYLIST:
                parentType = parentType.PLAYLIST;
                break;
            default:
                System.err.println("Invalid parent type");
                break;
        }

    }

    public void pauseTimeline() {
        if (songTimeline != null) {
            songTimeline.pause();
        }
    }

    public void refreshAlbumSongQueue() {
        nextSongsQueue = new ArrayList<>();
        previousSongsQueue = new ArrayList<>();

        boolean add = false;
        for(File file : directory.listFiles(flacFilter)) {
            if(add) {
                nextSongsQueue.add(file.getAbsolutePath());
            } else {
                previousSongsQueue.add(file.getAbsolutePath());
            }
            if(file.getAbsolutePath().equals(currentSongPath)) {
                add = true;
            }
        }

        if(shuffleSelected) {
            shuffle();
        }
    }

    public void loadSong(String songPath) {
        File f = new File(songPath);
        if(mediaPlayer != null) {
            mediaPlayer.dispose();
        }

        if(songTimeline != null) {
            songTimeline.stop();
        }

        System.out.println("Loading song " + fileUtils.getSongTitle(f));

        fileUtils.flacToWav(songPath, "src/main/resources/com/berrygobbler78/flacplayer/cache/temp.wav");
        mediaPlayer = new MediaPlayer(new Media(new File(wavPath).toURI().toString()));
        mediaPlayer.setOnEndOfMedia(this::next);

        currentSongPath = songPath;
    }

    public void SetPreviewTabController(PreviewTabController controller) {
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
            if(songTimeline == null || songTimeline.getStatus() == Timeline.Status.STOPPED) {
                System.out.println("New timeline");
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
                    controller.updateBottomBar();
                });
            } else if (mediaPlayer.getStatus() == MediaPlayer.Status.PAUSED) {
                songTimeline.play();
                mediaPlayer.play();

                System.out.println("Playing song " + fileUtils.getSongArtist(new File(currentSongPath)));
            }

            if(previewTabController != null) {
                System.out.println("hello bithces");
                previewTabController.setPlayPauseImageViewPaused(false);
            }

            controller.setCurrentPlayPauseImageViewPaused(false);
            controller.updateBottomBar();
        }
    }

    public void pause() {
        if(mediaPlayer != null) {
            songTimeline.pause();
            controller.setCurrentTrackTime((int) mediaPlayer.getCurrentTime().toSeconds());
            mediaPlayer.pause();

            if(previewTabController != null) {

                previewTabController.setPlayPauseImageViewPaused(true);
            }

            controller.setCurrentPlayPauseImageViewPaused(true);
        }
    }

    public void stop() {
        if(mediaPlayer != null) {
            songTimeline.stop();
            mediaPlayer.stop();

            if(previewTabController != null) {
                previewTabController.setPlayPauseImageViewPaused(true);
            }

            controller.setCurrentPlayPauseImageViewPaused(true);
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
                currentSongPath = directory.listFiles(flacFilter)[0].getAbsolutePath();
                refreshAlbumSongQueue();
                loadSong(currentSongPath);
                play();
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

    public void setRepeatStatus(RepeatStatus repeatStatus) {
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
        return mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING;
    }

    public String getSongTitle() {
        try {
            return new File(currentSongPath).getName().replace(".flac", "").substring(new File(currentSongPath).getName().indexOf(".")+1, new File(currentSongPath).getName().replace(".flac", "").lastIndexOf("-")).trim();

        } catch (Exception e) {
            System.err.println("Error getting song title " + e);
            return null;
        }
    }

    public String getArtistName() {
        try {
            return new File(new File(new File(currentSongPath).getParent()).getParent()).getName();
        } catch (Exception e) {
            System.err.println("Error getting artist name " + e);
            return null;
        }
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
