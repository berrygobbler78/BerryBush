package com.berrygobbler78.flacplayer.configuration;

import com.berrygobbler78.flacplayer.util.handlers.ResourceHandler;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.file.FileConfig;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PlaylistDataHandler {
    private static final Logger logger = LogManager.getLogger();

    private static final HashMap<Playlist, FileConfig> PLAYLIST_CONFIG_MAP = new HashMap<>();

    public static class Playlist {
        private String name;
        private final List<String> songs;

        public Playlist(String name, List<String> songs) {
            this.name = name;
            this.songs = songs;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<String> getSongs() {
            return songs;
        }

        public void addSong(String song) {
            songs.add(song);
        }

        public void removeSong(String song) {
            songs.remove(song);
        }
    }

    public static void initialize() {
        loadPlaylists();
    }

    public static void loadPlaylists() {
        logger.info("Loading playlists...");

        File[] playlistFileArray;
        try {
            playlistFileArray = ResourceHandler.getResourceFile("cache/playlists").listFiles(f -> f.getName().endsWith(".toml"));
        } catch (NullPointerException e) {
            logger.error("No playlist directory exists : {}", e.getMessage());
            return;
        }

        if(playlistFileArray == null) {
            logger.info("Playlist directory is empty : Returning...");
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

                setIfMissing(fileCfg, "playlist.name", "unknown");
                setIfMissing(fileCfg, "playlist.songs", new ArrayList<>());

                // Use nested keys for [playlist] section
                String name = fileCfg.get("playlist.name");
                List<String> songs = fileCfg.get("playlist.songs");

                Playlist p = new Playlist(name, songs);
                PLAYLIST_CONFIG_MAP.put(p, fileCfg);

                fileCfg.save();

                logger.info("Found playlist '{}' at path: {}", p.name, file.getPath());
            } catch (IOException e) {
                logger.error("Failed to get playlist at path : {} : {}", file.getPath(), e.getMessage());
            }
        }

        logger.info("Finished loading playlists");
    }

    private static <T> void setIfMissing(FileConfig fileConfig, String path, T value) {
        if (!fileConfig.contains(path)) {
            fileConfig.set(path, value);
        }
    }

    public static void createPlaylist(String name, List<String> songs) {
        Playlist playlist = new Playlist(name, songs);

        name = name.toLowerCase().replace(" ", "-");

        try(
            CommentedFileConfig fileConfig = CommentedFileConfig
                .builder(ResourceHandler.getResourceFile(String.format("cache/playlists/%s.toml", name)))
                .sync()
                .autosave()
                .autoreload()
                .build()
        ) {
            fileConfig.set("name", playlist.name);
            fileConfig.set("songs", playlist.songs);
        }
    }

    public void removePlaylist(Playlist playlist) {
        try (FileConfig cfg = PLAYLIST_CONFIG_MAP.remove(playlist)) {
            if (cfg != null && cfg.getFile().exists()) {
                if (!cfg.getFile().delete()) {
                    logger.error("Failed to remove playlist '{}'", playlist.getName());
                }
            }
        }
    }

    public static List<Playlist> getPlaylists() {
        return List.copyOf(PLAYLIST_CONFIG_MAP.keySet());
    }

}
