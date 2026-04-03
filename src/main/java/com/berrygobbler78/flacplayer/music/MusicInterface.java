package com.berrygobbler78.flacplayer.music;
import com.berrygobbler78.flacplayer.controllers.LandingController;
import com.berrygobbler78.flacplayer.util.handlers.ResourceHandler;
import com.berrygobbler78.flacplayer.util.records.Album;
import com.berrygobbler78.flacplayer.util.records.Playlist;
import com.berrygobbler78.flacplayer.util.records.Song;
import io.github.selemba1000.JMTCPlayingState;
import javafx.scene.image.Image;
import javafx.scene.media.MediaPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class MusicInterface {
    private final static Logger logger = LogManager.getLogger();

    private final MediaTransportHandler mediaTransportHandler;

    private final QueueManager queueManager = new QueueManager();
    private final PlaybackManager playbackManager;

    // Controllers
    private final LandingController controller;

    public MusicInterface(LandingController controller) {
        this.controller = controller;
        playbackManager = new PlaybackManager(this, queueManager);
        mediaTransportHandler = new MediaTransportHandler("JMTC", "JMTC", playbackManager, queueManager);
    }

    // Play utils

    public void playFirstSong() {
        playSongNum(0);
    }

    public void playSongNum(int index) {
       queueManager.generateQueueAtIndex(index - 1);
       playbackManager.load(true, false);
    }

    public void playSong(Song song) {
        queueManager.setCurrentSong(song);
        playbackManager.load(true, false);
    }

    public void play() {
        playbackManager.play();
    }

    public void pause() {
        playbackManager.pause();
    }

    public void stop() {
        playbackManager.stop();
    }

    public void next() {
        playbackManager.next();
    }

    public void previous() {
        playbackManager.previous();
    }

    /**
     Method to play or pause the current song
     @return true if playing, false if paused
     */
    public boolean playPause() {
        if(playbackManager.getStatus() == MediaPlayer.Status.PLAYING) {
            pause();
            return false;
        } else {
            play();
            return true;
        }
    }

    public void updateBottomBar(Song song) {
        controller.updateBottomBar(
                new Image(String.valueOf(ResourceHandler.getResourceURL(song.album().artPath()))),
                song.title(),
                song.artist().name());
    }

    public void updatePaused(boolean paused) {
        controller.setPaused(paused);
    }

    public void updateJMTC() {
       Song currentSong = queueManager.getCurrentSong();
       if(currentSong == null) return;

       mediaTransportHandler.setProperties(
               currentSong.title(),
               currentSong.artist().name(),
               currentSong.album().title(),
               currentSong.artist().name(),
               currentSong.album().songs().size(),
               currentSong.track(),
               ResourceHandler.getResourceFile(currentSong.album().artPath())
       );
    }

    public void updateJMTCState(JMTCPlayingState state) {
        mediaTransportHandler.setState(state);
    }

    public MediaPlayer.Status getStatus() {
        return playbackManager.getStatus();
    }

    public QueueManager.REPEAT_STATUS getRepeatStatus() {
        return queueManager.getRepeatStatus();
    }

    public void setRepeatStatus(QueueManager.REPEAT_STATUS status) {
        queueManager.setRepeatStatus(status);
    }

    public void cycleRepeatStatus() {
        queueManager.cycleRepeatStatus();
    }

    public void toggleShuffleStatus() {
        queueManager.toggleShuffleStatus();
    }

    public void setShuffleStatus(boolean shuffle) {
        queueManager.setShuffleStatus(shuffle);
    }

    public Song getCurrentSong() {
        return queueManager.getCurrentSong();
    }

    public void setVolume(float v) {
        playbackManager.setVolume(v);
    }

    public void setParent(Album album) {
        queueManager.setAlbum(album);
    }

    public void setParent(Playlist playlist) {
        queueManager.setPlaylist(playlist);
    }

    public void addToUserQueue(Song song) {
        queueManager.addToUserQueue(song);
    }
}
