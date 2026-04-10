package com.berrygobbler78.flacplayer.util.records;

import com.berrygobbler78.flacplayer.configuration.PlaylistDataHandler;
import com.berrygobbler78.flacplayer.configuration.UserDataHandler;
import com.berrygobbler78.flacplayer.util.FileUtils;
import com.berrygobbler78.flacplayer.util.ResourceHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

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

        var rootDir = new File(UserDataHandler.getConfig(UserDataHandler.ConfigLocation.PATH));
        if(!rootDir.exists() || !rootDir.isDirectory()) {
            logger.fatal("Root directory does not exist or is not a directory at '{}'", rootDir.getAbsolutePath());
            return;
        }

        try (Stream<Path> inStream = Files.walk(Path.of(UserDataHandler.getConfig(UserDataHandler.ConfigLocation.PATH))) ){
            inStream.filter(path -> path.toString().endsWith(".flac"))
                    .forEach(path -> {
                        try {
                            var songFile = path.toFile();
                            var audioFile = AudioFileIO.read(new File(songFile.getPath()));
                            var tag = audioFile.getTag();

                            if (tag == null) return;

                            var songTitle = tag.getFirst(FieldKey.TITLE);
                            songTitle = (songTitle == null || songTitle.isBlank()) ? "Unknown" : songTitle;

                            var albumStr = tag.getFirst(FieldKey.ALBUM);
                            albumStr = (albumStr == null || albumStr.isBlank()) ? "Unknown" : albumStr;

                            var artistStr = tag.getFirst(FieldKey.ARTIST);
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

                            int disc;
                            String discStr = tag.getFirst(FieldKey.DISC_NO);
                            discStr = (discStr == null || discStr.isBlank()) ? "1" : discStr;
                            disc = Integer.parseInt(discStr);

                            Artist artist = null;
                            for(Artist a : ARTIST_LIST) {
                                if (a.title().equals(artistStr)) {
                                    artist = a;
                                    break;
                                }
                            }

                            if(artist == null) {
                                artist = new Artist(
                                        artistStr,
                                        new ArrayList<>(),
                                        ResourceHandler.getCache().getPath() +
                                                File.separator +
                                                "artist-art" +
                                                File.separator +
                                                FileUtils.makeFolderSafe(artistStr) +
                                                File.separator +
                                                "art.png"
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
                                String cachePath = ResourceHandler.getCache().getPath() +
                                        File.separator +
                                        "album-art" +
                                        File.separator +
                                        FileUtils.makeFolderSafe(artistStr) +
                                        File.separator +
                                        FileUtils.makeFolderSafe(albumStr) +
                                        File.separator +
                                        "art.png";

                                album = new Album(
                                        albumStr,
                                        artist,
                                        new ArrayList<>(),
                                        cachePath
                                );

                                artist.albums().add(album);
                                ALBUM_LIST.add(album);
                            }

                            Song song = null;
                            for(Song s : album.songs()) {
                                if (s.title().equals(songTitle)) {
                                    song = s;
                                    break;
                                }
                            }

                            if(song == null) {
                                song = new Song(
                                        songTitle,
                                        album,
                                        track,
                                        disc,
                                        songFile.getPath()
                                );

                                album.songs().add(song);
                                SONG_LIST.add(song);
                            }

                            logger.debug("'{}' added to '{}' with artist '{}'", song.title(), album.title(), artist.title());
                        } catch (Exception e) {
                            logger.error("Failed to read metadata from '{}'", path);
                        }
                    });
        } catch (IOException e) {
            logger.error("Failed to traverse archive root | {}", e.getMessage());
        }

        for(Album a : ALBUM_LIST) {
            a.songs().sort(Comparator.comparingInt(Song::track));
        }

        PlaylistDataHandler.initialize();

        PLAYLIST_LIST.addAll(PlaylistDataHandler.getPlaylists());

        logger.info("Cache has been refreshed");
    }
}
