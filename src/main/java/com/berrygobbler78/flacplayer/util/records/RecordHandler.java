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

    private static volatile List<Artist> ARTISTS = List.of();
    private static volatile List<Album> ALBUMS = List.of();
    private static volatile List<Song> SONGS = List.of();
    private static volatile List<Playlist> PLAYLISTS = List.of();

    private static final Object lock = new Object();

    public static List<Artist> getArtists() {
        synchronized (lock) { return ARTISTS; }
    }
    public static List<Album> getAlbums() {
        synchronized (lock) { return ALBUMS; }
    }
    public static List<Song> getSongs() {
        synchronized (lock) { return SONGS; }
    }
    public static List<Playlist> getPlaylists() {
        synchronized (lock) { return PLAYLISTS; }
    }

    public static void cache() {
        logger.info("Caching...");

        synchronized (lock) {
            var newArtists = new ArrayList<Artist>();
            var newAlbums = new ArrayList<Album>();
            var newSongs = new ArrayList<Song>();

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

                            var songStr = tag.getFirst(FieldKey.TITLE);
                            songStr = (songStr == null || songStr.isBlank()) ? "Unknown" : songStr;

                            var albumStr = tag.getFirst(FieldKey.ALBUM);
                            albumStr = (albumStr == null || albumStr.isBlank()) ? "Unknown" : albumStr;

                            var artistStr = tag.getFirst(FieldKey.ARTIST);
                            artistStr = (artistStr == null || artistStr.isBlank()) ? "Unknown" : artistStr;

                            artistStr = artistStr
                                    .replaceAll("(?i)(feat\\.|ft\\.|featuring).*", "")
                                    .replaceAll("[,&/\\-].*", "")
                                    .trim();

                            short track = 0;
                            String trackStr = tag.getFirst(FieldKey.TRACK);
                            if(trackStr != null) {
                                trackStr = trackStr.split("/")[0].trim();
                                track = trackStr.matches("\\d+") ? (short) Integer.parseInt(trackStr) : 0;
                            }

                            short disc;
                            String discStr = tag.getFirst(FieldKey.DISC_NO);
                            discStr = (discStr == null || discStr.isBlank()) ? "1" : discStr;
                            disc = (short) Integer.parseInt(discStr);

                            String finalArtistStr = artistStr;
                            Artist artist = newArtists.stream()
                                    .filter(a -> a.title().equals(finalArtistStr))
                                    .findFirst()
                                    .orElse(null);

                            if(artist == null) {
                                artist = new Artist(artistStr, new ArrayList<>(),
                                        ResourceHandler.get(ResourceHandler.ResourceType.ARTIST_ART).getPath() +
                                                File.separator +
                                                FileUtils.makeFolderSafe(artistStr) +
                                                File.separator +
                                                "art.png"
                                );

                                newArtists.add(artist);
                            }

                            String finalArtistStr1 = artistStr;
                            String finalAlbumStr = albumStr;
                            Album album = newAlbums.stream()
                                    .filter(a -> a.artist().title().equals(finalArtistStr1) && a.title().equals(finalAlbumStr))
                                    .findFirst()
                                    .orElse(null);

                            if(album == null) {
                                String cachePath = ResourceHandler.get(ResourceHandler.ResourceType.ALBUM_ART).getPath()+
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
                                newAlbums.add(album);
                            }

                            String finalSongStr = songStr;
                            Song song = newSongs.stream()
                                    .filter(s -> s.album().title().equals(finalArtistStr) && s.title().equals(finalSongStr))
                                    .findFirst()
                                    .orElse(null);

                            if(song == null) {
                                song = new Song(
                                        songStr,
                                        album,
                                        track,
                                        disc,
                                        songFile.getPath()
                                );

                                album.songs().add(song);
                                newSongs.add(song);
                            }

                            logger.debug("'{}' added to '{}' with artist '{}'", song.title(), album.title(), artist.title());
                        } catch (Exception e) {
                            logger.error("Failed to read metadata from '{}' | {}", path, e.getMessage());
                        }
                    });
        } catch (IOException e) {
            logger.error("Failed to traverse archive root | {}", e.getMessage());
        }

        ARTISTS = List.copyOf(newArtists);
        ALBUMS = List.copyOf(newAlbums);
        SONGS = List.copyOf(newSongs);

        PlaylistDataHandler.initialize();
        PLAYLISTS = List.copyOf(PlaylistDataHandler.getPlaylists());

        for(Album a : ALBUMS) a.songs().sort(Comparator.comparingInt(Song::track));

        logger.info("Cache has been refreshed");
    }
}
