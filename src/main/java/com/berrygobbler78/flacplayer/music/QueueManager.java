package com.berrygobbler78.flacplayer.music;
import com.berrygobbler78.flacplayer.util.records.Album;
import com.berrygobbler78.flacplayer.util.records.Playlist;
import com.berrygobbler78.flacplayer.util.records.Song;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

    public void setAlbum(Album album) {
        logger.info("Music player album set to '{}'", album.title());
        currentAlbum = album;
        currentParentType = PARENT_TYPE.ALBUM;
    }

    public void setPlaylist(Playlist playlist) {
        logger.info("Music player playlist set to '{}'", playlist.title());
        currentPlaylist = playlist;
        currentParentType = PARENT_TYPE.PLAYLIST;
    }

    public void addToUserQueue(Song song) {
        userQueue.add(song);
    }

    void clearQueues(boolean clearUserQueue) {
        logger.info("Clearing queues... : userQueue? '{}'", clearUserQueue);

        previousSongsQueue.clear();
        nextSongsQueue.clear();
        if(clearUserQueue) userQueue.clear();
    }

    public void shuffle() {
        logger.info("Shuffling...");

        var temp = new ArrayList<>(nextSongsQueue);
        nextSongsQueue.clear();
        while(!temp.isEmpty()) nextSongsQueue.add(temp.remove(random.nextInt(temp.size())));
    }

    // Queueing

    public void generateQueueAtIndex(int index) {
        clearQueues(false);

        switch (currentParentType) {
            case ALBUM -> generateQueue(index, currentAlbum.songs());
            case PLAYLIST -> generateQueue(index, currentPlaylist.songs());
        }

        logger.info("Generated new parent queue : NextQueue '{}' : PreviousQueue '{}'", nextSongsQueue.size(), previousSongsQueue.size());
    }

    public void generateQueue(int index, List<Song> songs) {
        var count = 0;
        var add = false;

        for(Song s : songs) {
            if(count == index) {
                currentSong = s;
                add = true;
            } else if(add) {
                nextSongsQueue.add(s);
            } else {
                previousSongsQueue.addFirst(s);
            }
            count++;
        }

        if(shuffleStatus == SHUFFLE_STATUS.SHUFFLE) shuffle();
    }

    public Song getCurrentSong() {
        return currentSong;
    }

    public void setCurrentSong(Song song) {
        clearQueues(false);
        currentSong = song;
    }

    public Song getNextSong(boolean remove) {
        if(repeatStatus == REPEAT_STATUS.REPEAT_ONE) return null;
        if(!userQueue.isEmpty() && remove) {
            currentSong = userQueue.getFirst();
            return userQueue.removeFirst();
        }
        if(!userQueue.isEmpty()) return userQueue.getFirst();
        if(repeatStatus == REPEAT_STATUS.REPEAT_ALL) {
            if(currentParentType == PARENT_TYPE.PLAYLIST) {
                nextSongsQueue.addAll(currentPlaylist.songs());
            } else {
                nextSongsQueue.addAll(currentAlbum.songs());
            }
        }
        if(remove) {
            previousSongsQueue.addFirst(currentSong);
            currentSong = nextSongsQueue.getFirst();
            return nextSongsQueue.removeFirst();
        }
        if(nextSongsQueue.isEmpty()) return null;
        return nextSongsQueue.getFirst();
    }

    public Song getPreviousSong(boolean remove) {
        if(remove) {
            nextSongsQueue.addFirst(currentSong);
            currentSong = previousSongsQueue.getFirst();
            return previousSongsQueue.removeFirst();
        }
        return previousSongsQueue.getFirst();
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

        logger.info("Repeat set to '{}'", repeatStatus);
    }

    public void setRepeatStatus(REPEAT_STATUS status) {
        repeatStatus = status;
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

        logger.info("Shuffle toggled to '{}'", shuffleStatus);

        if(shuffleStatus == SHUFFLE_STATUS.SHUFFLE && !nextSongsQueue.isEmpty()) {
            shuffle();
        }
    }

    public void setShuffleStatus(boolean shuffle) {
        logger.info("Shuffle set to '{}'", (shuffle ? "ON" : "OFF"));

        if(shuffle) {
            shuffleStatus = SHUFFLE_STATUS.SHUFFLE;
            shuffle();
        } else {
            shuffleStatus = SHUFFLE_STATUS.OFF;
            generateQueueAtIndex(0);
        }
    }
}
