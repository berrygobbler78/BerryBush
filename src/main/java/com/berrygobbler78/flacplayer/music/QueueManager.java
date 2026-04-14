package com.berrygobbler78.flacplayer.music;
import com.berrygobbler78.flacplayer.util.records.Album;
import com.berrygobbler78.flacplayer.util.records.Playlist;
import com.berrygobbler78.flacplayer.util.records.Song;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class QueueManager {
    private static final Logger logger = LogManager.getLogger(QueueManager.class);

    // Current information
    private Song currentSong = null;

    public enum PARENT_TYPE { ALBUM, PLAYLIST }
    private PARENT_TYPE currentParentType;
    private Playlist currentPlaylist;
    private Album currentAlbum;

    // Queueing
    private final ArrayList<Song> previousSongsQueue = new ArrayList<>();
    private final ArrayList<Song> userQueue = new ArrayList<>();
    private final ArrayList<Song> nextSongsQueue = new ArrayList<>();

    private final Random random = new Random();

    // Statuses
    public enum REPEAT_STATUS {
        OFF,
        REPEAT_ALL,
        REPEAT_ONE
    }

    public enum SHUFFLE_STATUS {
        OFF,
        SHUFFLE
    }

    private SHUFFLE_STATUS shuffleStatus = SHUFFLE_STATUS.OFF;
    private REPEAT_STATUS repeatStatus = REPEAT_STATUS.OFF;

    // Setting Parents

    public synchronized void setAlbum(Album album) {
        logger.info("Music player album set to '{}'", album.title());
        currentAlbum = album;
        currentParentType = PARENT_TYPE.ALBUM;
    }

    public synchronized void setPlaylist(Playlist playlist) {
        logger.info("Music player playlist set to '{}'", playlist.title());
        currentPlaylist = playlist;
        currentParentType = PARENT_TYPE.PLAYLIST;
    }

    public synchronized void addToUserQueue(Song song) {
        userQueue.add(song);
    }

    public synchronized void clearQueues() {
        logger.info("Clearing queues...");

        previousSongsQueue.clear();
        nextSongsQueue.clear();
    }

    public synchronized void shuffle() {
        logger.info("Shuffling...");

        var temp = new ArrayList<>(nextSongsQueue);
        nextSongsQueue.clear();
        while(!temp.isEmpty()) nextSongsQueue.add(temp.remove(random.nextInt(temp.size())));
    }

    // Queueing

    public synchronized void generateQueueAtIndex(int index) {
        clearQueues();

        switch (currentParentType) {
            case ALBUM -> generateQueue(index, currentAlbum.songs());
            case PLAYLIST -> generateQueue(index, currentPlaylist.songs());
        }

        logger.info("Generated new parent queue : NextQueue '{}' : PreviousQueue '{}'", nextSongsQueue.size(), previousSongsQueue.size());
    }

    public synchronized void generateQueue(int index, List<Song> songs) {
        var count = 0;
        var add = false;

        for(Song s : songs) {
            if(count == index) {
                currentSong = s;
                add = true;
                count++;
                continue;
            }

            if(add) nextSongsQueue.add(s);
            else previousSongsQueue.addFirst(s);
            count++;
        }

        if(shuffleStatus == SHUFFLE_STATUS.SHUFFLE) shuffle();
    }

    public synchronized Song getCurrentSong() {
        return currentSong;
    }

    public synchronized void setCurrentSong(Song song) {
        clearQueues();
        currentSong = song;
    }

    public synchronized Optional<Song> getNextSong(boolean remove) {
        if(repeatStatus == REPEAT_STATUS.REPEAT_ONE) return Optional.empty();
        // User queue takes priority
        if(!userQueue.isEmpty() && remove) {
            currentSong = userQueue.getFirst();
            return Optional.of(userQueue.removeFirst());
        }
        if(!userQueue.isEmpty()) return Optional.of(userQueue.getFirst());

        if(repeatStatus == REPEAT_STATUS.REPEAT_ALL) {
            if(currentParentType == PARENT_TYPE.PLAYLIST) {
                nextSongsQueue.addAll(currentPlaylist.songs());
            } else {
                nextSongsQueue.addAll(currentAlbum.songs());
            }
        }

        if(nextSongsQueue.isEmpty()) return Optional.empty();
        if(remove) {
            previousSongsQueue.addFirst(currentSong);
            currentSong = nextSongsQueue.removeFirst();
            return Optional.of(currentSong);
        }
        return Optional.of(nextSongsQueue.getFirst());
    }

    public synchronized Optional<Song> getPreviousSong(boolean remove) {
        if(previousSongsQueue.isEmpty()) return Optional.empty();
        if(!remove) return Optional.of(previousSongsQueue.getFirst());
        nextSongsQueue.addFirst(currentSong);
        currentSong = previousSongsQueue.removeFirst();
        return Optional.of(currentSong);
    }

    // Repeat status control

    public synchronized void cycleRepeatStatus() {
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

        logger.info("Repeat set to '{}'", repeatStatus);
    }

    public synchronized void setRepeatStatus(REPEAT_STATUS status) {
        repeatStatus = status;
    }

    public synchronized REPEAT_STATUS getRepeatStatus() {
        return repeatStatus;
    }

    // Shuffle status control

    public synchronized void toggleShuffleStatus() {
        switch (shuffleStatus) {
            case OFF ->   shuffleStatus = SHUFFLE_STATUS.SHUFFLE;
            case SHUFFLE ->   shuffleStatus = SHUFFLE_STATUS.OFF;
        }

        logger.info("Shuffle toggled to '{}'", shuffleStatus);

        if(shuffleStatus == SHUFFLE_STATUS.SHUFFLE && !nextSongsQueue.isEmpty()) {
            shuffle();
        }
    }

    public synchronized void setShuffleStatus(boolean shuffle) {
        logger.info("Shuffle set to '{}'", (shuffle ? "ON" : "OFF"));

        if(shuffle) {
            shuffleStatus = SHUFFLE_STATUS.SHUFFLE;
            shuffle();
        } else {
            shuffleStatus = SHUFFLE_STATUS.OFF;
            switch (currentParentType) {
                case ALBUM -> generateQueueAtIndex(currentSong.track() - 1);
                case PLAYLIST -> generateQueueAtIndex(currentSong.track() - 1); //FIXME: Should be currentSong.index()
            }

        }
    }
}
