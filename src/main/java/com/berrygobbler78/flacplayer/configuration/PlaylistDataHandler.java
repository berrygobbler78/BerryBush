package com.berrygobbler78.flacplayer.configuration;

import com.berrygobbler78.flacplayer.util.FileUtils;
import com.berrygobbler78.flacplayer.util.records.RecordHandler;
import com.berrygobbler78.flacplayer.util.ResourceHandler;
import com.berrygobbler78.flacplayer.util.records.Playlist;
import com.berrygobbler78.flacplayer.util.records.Song;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.file.FileConfig;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PlaylistDataHandler {
    private static final Logger logger = LogManager.getLogger();

    private static final HashMap<Playlist, FileConfig> PLAYLIST_CONFIG_MAP = new HashMap<>();

    public static void initialize() {
        loadPlaylists();
    }

    public static void loadPlaylists() {
        logger.info("Loading playlists...");

        var playlistDir = new File(ResourceHandler.getCache(), "playlists");

        File[] playlistFileArray = playlistDir.listFiles(f -> f.getName().endsWith(".toml"));

        if(playlistFileArray == null) {
            logger.info("Playlist directory is empty");
            return;
        }

        for (File file : playlistFileArray) {
            try (
                    CommentedFileConfig fileCfg = CommentedFileConfig.builder(file)
                            .autoreload()
                            .sync()
                            .autosave()
                            .build()
            ) {
                if (fileCfg == null) throw new IOException("Empty file");

                fileCfg.load();

                setIfMissing(fileCfg, "title", "Unknown Title");
                setIfMissing(fileCfg, "user", "Unknown User");
                setIfMissing(fileCfg, "art-path", "");
                setIfMissing(fileCfg, "songs", new ArrayList<>());

                String name = fileCfg.get("title");
                String user = fileCfg.get("user");
                String art = fileCfg.get("art");
                List<String> songPaths = fileCfg.get("songs");

                var songs = new ArrayList<Song>();
                var songMap = RecordHandler.getSongList()
                        .stream()
                        .collect(Collectors.toMap(Song::path, s -> s));

                for (String p : songPaths) {
                    Song s = songMap.get(p);
                    if (s != null) {
                        songs.add(s);
                    }
                }
                var playlist = new Playlist(name, user, songs, art,null, null);

                PLAYLIST_CONFIG_MAP.put(playlist, fileCfg);

                fileCfg.save();

                logger.info("Found playlist '{}' at path: {}", playlist.title(), file.getPath());
            } catch (IOException e) {
                logger.error("Failed to get playlist at path : {} | {}", file.getPath(), e.getMessage());
            }
        }

        logger.info("Finished loading playlists");
    }

    private static <T> void setIfMissing(FileConfig fileConfig, String path, T value) {
        if (!fileConfig.contains(path)) {
            fileConfig.set(path, value);
        }
    }

    public static void createPlaylist(String name, String author, List<Song> songs) {
        if(songs == null) songs = new ArrayList<>();
        var file = new File(new File(ResourceHandler.getCache(), "playlists"), FileUtils.makeFolderSafe(name) + ".toml");
        try {
            if(file.createNewFile()) logger.info("Created new playlist file at '{}'", file.getPath());
        } catch (IOException e) {
            logger.error("Failed to create new file with path '{}' : {}", file.getPath(), e.getMessage());
            return;
        }
        Playlist playlist = new Playlist(name, author, songs, file.getPath(), null, null);


        try(CommentedFileConfig fileCfg = CommentedFileConfig.builder(file).build()) {
            fileCfg.load();
            fileCfg.set("title", playlist.title());
            fileCfg.set("user", playlist.author());
            fileCfg.set("songs", playlist.songs().stream().map(Song::path).toList());
            fileCfg.save();
        }

        RecordHandler.cache();
    }

    public static void removePlaylist(Playlist playlist) {
        if(PLAYLIST_CONFIG_MAP.get(playlist).getFile().delete()) logger.info("Deleted playlist file at '{}'", playlist.path());
        PLAYLIST_CONFIG_MAP.remove(playlist);
        RecordHandler.cache();
    }

    public static void save(Playlist playlist) {
        try(CommentedFileConfig fileCfg = CommentedFileConfig.builder(new File(playlist.path())).build()) {
            fileCfg.load();
            fileCfg.set("title", playlist.title());
            fileCfg.set("user", playlist.author());
            fileCfg.set("art", playlist.path());
            fileCfg.set("songs", playlist.songs().stream().map(Song::path).toList());
            fileCfg.save();
        }
    }

    public static List<Playlist> getPlaylists() {
        return List.copyOf(PLAYLIST_CONFIG_MAP.keySet());
    }

}
