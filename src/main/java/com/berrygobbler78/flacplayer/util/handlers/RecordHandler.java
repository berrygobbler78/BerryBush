package com.berrygobbler78.flacplayer.util.handlers;

import com.berrygobbler78.flacplayer.configuration.UserDataHandler;
import com.berrygobbler78.flacplayer.util.FileUtils;
import com.berrygobbler78.flacplayer.util.records.Album;
import com.berrygobbler78.flacplayer.util.records.Artist;
import com.berrygobbler78.flacplayer.util.records.Song;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import java.io.File;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RecordHandler {
    private static final Logger LOGGER = Logger.getLogger(RecordHandler.class.getName());

    static {
        Logger.getLogger("org.jaudiotagger").setLevel(Level.SEVERE);
    }

    private static final List<Artist> ARTIST_LIST = Collections.synchronizedList(new ArrayList<>());;
    private static final List<Album> ALBUM_LIST = Collections.synchronizedList(new ArrayList<>());;
    private static final List<Song> SONG_LIST = Collections.synchronizedList(new ArrayList<>());;

    public static List<Artist> getArtistList() {
        return ARTIST_LIST;
    }

    public static List<Album> getAlbumList() {
        return ALBUM_LIST;
    }

    public static List<Song> getSongList() {
        return SONG_LIST;
    }

    public static void cache() {
        LOGGER.info("Caching...");

        ARTIST_LIST.clear();
        ALBUM_LIST.clear();
        SONG_LIST.clear();

        File rootDir = new File(UserDataHandler.getPath());
        if(!rootDir.exists() || !rootDir.isDirectory()) {
            LOGGER.severe("Root directory does not exist or is not a directory. Path:" + rootDir.getAbsolutePath());
            return;
        }

        File[] artistFolders = rootDir.listFiles(FileUtils.FILTER_TYPE.FOLDER.get());
        if(artistFolders == null || artistFolders.length == 0) {
            LOGGER.severe("No artist files found. Path:" + rootDir.getAbsolutePath());
            return;
        }

        for (File artistFolder : artistFolders) {
            File[] albumFolders = artistFolder.listFiles(FileUtils.FILTER_TYPE.FOLDER.get());
            if(albumFolders == null || albumFolders.length == 0) {
                LOGGER.warning("No album files found. Path:" + artistFolder.getAbsolutePath());
                continue;
            }

            for (File albumFolder : albumFolders) {
                File[] songFiles = albumFolder.listFiles(FileUtils.FILTER_TYPE.FLAC.get());
                if(songFiles == null || songFiles.length == 0) {
                    LOGGER.warning("Song files not found. Path:" + albumFolder.getAbsolutePath());
                    continue;
                }

                for(File songFile : songFiles) {
                    try {
                        AudioFile audioFile = AudioFileIO.read(new File(songFile.getPath()));
                        Tag tag = audioFile.getTag();

                        if (tag == null) continue;

                        String songStr = tag.getFirst(FieldKey.TITLE);
                        songStr = (songStr == null || songStr.isBlank()) ? "Unknown" : songStr;

                        String albumStr = tag.getFirst(FieldKey.ALBUM);
                        albumStr = (albumStr == null || albumStr.isBlank()) ? "Unknown" : albumStr;

                        String artistStr = tag.getFirst(FieldKey.ARTIST);
                        artistStr = (artistStr == null || artistStr.isBlank()) ? "Unknown" : artistStr;

                        artistStr = artistStr
                                .replaceAll("(?i)(feat\\.|ft\\.|featuring).*", "")
                                .replaceAll("[,&/\\-].*", "")
                                .trim();

                        int track = 0;
                        String trackStr = tag.getFirst(FieldKey.TRACK);
                        if(trackStr != null) {
                            trackStr = trackStr.split("/")[0].trim();
                            track = trackStr.matches("\\d+") ? Integer.parseInt(trackStr) : 0;
                        }

                        Artist artist = null;
                        for(Artist a : ARTIST_LIST) {
                            if (a.name().equals(artistStr)) {
                                artist = a;
                                break;
                            }
                        }

                        if(artist == null) {
                            artist = new Artist(
                                    artistStr,
                                    new ArrayList<>(),
                                    null,
                                    null
                            );

                            ARTIST_LIST.add(artist);
                        }

                        Album album = null;
                        for(Album a : artist.albums()) {
                            if (a.title().equals(albumStr)) {
                                album = a;
                                break;
                            }
                        }

                        if(album == null) {
                            String cachePath = "cache/album-art/" +
                                    FileUtils.makeFolderSafe(artistStr)+ "/" +
                                    FileUtils.makeFolderSafe(albumStr) + "/";

                            album = new Album(
                                    albumStr,
                                    artist,
                                    new ArrayList<>(),
                                    cachePath + "coverIcon.png",
                                    cachePath + "coverImage.png"
                            );

                            artist.albums().add(album);
                            ALBUM_LIST.add(album);
                        }

                        Song song = null;
                        for(Song s : album.songs()) {
                            if (s.title().equals(songStr)) {
                                song = s;
                                break;
                            }
                        }

                        if(song == null) {
                            song = new Song(
                                    songStr,
                                    album,
                                    artist,
                                    track,
                                    songFile.getPath()
                            );

                            album.songs().add(song);
                            SONG_LIST.add(song);
                        }

                        // LOGGER.info(song.title() + " added to " + album.title() + " with artist " + artist.name());
                    } catch (Exception e) {
                        LOGGER.warning("Failed to read metadata for: " + songFile.getPath());
                    }
                }
            }
        }

        for(Album a : ALBUM_LIST) {
            a.songs().sort(Comparator.comparingInt(Song::track));
        }

        LOGGER.info("Cache has been refreshed");
    }


}
