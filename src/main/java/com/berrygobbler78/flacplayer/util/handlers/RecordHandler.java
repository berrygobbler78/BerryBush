package com.berrygobbler78.flacplayer.util.handlers;

import com.berrygobbler78.flacplayer.configuration.PlaylistDataHandler;
import com.berrygobbler78.flacplayer.configuration.UserDataHandler;
import com.berrygobbler78.flacplayer.util.FileUtils;
import com.berrygobbler78.flacplayer.util.records.Album;
import com.berrygobbler78.flacplayer.util.records.Artist;
import com.berrygobbler78.flacplayer.util.records.Playlist;
import com.berrygobbler78.flacplayer.util.records.Song;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import java.io.File;
import java.util.*;

public class RecordHandler {
    private static final Logger logger = LogManager.getLogger();

    private static final List<Artist> ARTIST_LIST = Collections.synchronizedList(new ArrayList<>());
    private static final List<Album> ALBUM_LIST = Collections.synchronizedList(new ArrayList<>());
    private static final List<Song> SONG_LIST = Collections.synchronizedList(new ArrayList<>());
    private static final List<Playlist> PLAYLIST_LIST = Collections.synchronizedList(new ArrayList<>());

    public static List<Artist> getArtistList() {
        return ARTIST_LIST;
    }
    public static List<Album> getAlbumList() {
        return ALBUM_LIST;
    }
    public static List<Song> getSongList() {
        return SONG_LIST;
    }
    public static List<Playlist> getPlaylistList() {
        return PLAYLIST_LIST;
    }

    public static void cache() {
        logger.info("Caching...");

        ARTIST_LIST.clear();
        ALBUM_LIST.clear();
        SONG_LIST.clear();
        PLAYLIST_LIST.clear();

        File rootDir = new File(UserDataHandler.getPath());
        if(!rootDir.exists() || !rootDir.isDirectory()) {
            logger.fatal("Root directory does not exist or is not a directory at '{}'", rootDir.getAbsolutePath());
            return;
        }

        File[] artistFolders = rootDir.listFiles(FileUtils.FILTER_TYPE.FOLDER.get());
        if(artistFolders == null || artistFolders.length == 0) {
            logger.fatal("No artist files found at '{}'", rootDir.getAbsolutePath());
            return;
        }

        for (File artistFolder : artistFolders) {
            File[] albumFolders = artistFolder.listFiles(FileUtils.FILTER_TYPE.FOLDER.get());
            if(albumFolders == null || albumFolders.length == 0) {
                logger.error("No album files found at '{}'", artistFolder.getAbsolutePath());
                continue;
            }

            for (File albumFolder : albumFolders) {
                File[] songFiles = albumFolder.listFiles(FileUtils.FILTER_TYPE.FLAC.get());
                if(songFiles == null || songFiles.length == 0) {
                    logger.error("Song files not found at '{}'", albumFolder.getAbsolutePath());
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

                        logger.debug("'{}' added to '{}' with artist '{}'", song.title(), album.title(), artist.name());
                    } catch (Exception e) {
                        logger.error("Failed to read metadata from '{}'", songFile.getPath());
                    }
                }
            }
        }

        for(Album a : ALBUM_LIST) {
            a.songs().sort(Comparator.comparingInt(Song::track));
        }

        PlaylistDataHandler.initialize();

        PLAYLIST_LIST.addAll(PlaylistDataHandler.getPlaylists());

        logger.info("Cache has been refreshed");
    }
}
