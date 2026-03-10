package com.berrygobbler78.flacplayer.configuration;

import com.berrygobbler78.flacplayer.util.FileUtils;
import com.berrygobbler78.flacplayer.util.handlers.RecordHandler;
import com.berrygobbler78.flacplayer.util.handlers.ResourceHandler;
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

        File[] playlistFileArray;
        try {
            playlistFileArray = ResourceHandler.getResourceFile("cache/playlists").listFiles(f -> f.getName().endsWith(".toml"));
        } catch (NullPointerException e) {
            logger.error("No playlist directory exists : {}", e.getMessage());
            return;
        }

        if(playlistFileArray == null) {
            logger.info("Playlist directory is empty, returning...");
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

                setIfMissing(fileCfg, "name", "Unknown Title");
                setIfMissing(fileCfg, "user", "Unknown User");
                setIfMissing(fileCfg, "songs", new ArrayList<>());

                String name = fileCfg.get("name");
                String user = fileCfg.get("user");
                List<String> songPaths = fileCfg.get("songs");

                List<Song> songs = new ArrayList<>();
                Map<String, Song> songMap = RecordHandler.getSongList()
                        .stream()
                        .collect(Collectors.toMap(Song::path, s -> s));

                for (String p : songPaths) {
                    Song s = songMap.get(p);
                    if (s != null) {
                        songs.add(s);
                    }
                }
                Playlist p = new Playlist(name, user, songs, fileCfg.getFile().getPath(),null, null);

                PLAYLIST_CONFIG_MAP.put(p, fileCfg);

                fileCfg.save();

                logger.info("Found playlist '{}' at path: {}", p.title(), file.getPath());
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

    public static void createPlaylist(String name, String author, List<Song> songs) {
        if(songs == null) songs = new ArrayList<>();
        File file = new File(ResourceHandler.getResourceFile("cache/playlists"), FileUtils.makeFolderSafe(name) + ".toml");
        try {
            file.createNewFile();
        } catch (IOException e) {
            logger.error("Failed to create new file with path '{}' : {}", file.getPath(), e.getMessage());
            return;
        }
        Playlist playlist = new Playlist(name, author, songs, file.getPath(), null, null);


        try(CommentedFileConfig fileCfg = CommentedFileConfig.builder(file).build()) {
            fileCfg.load();
            fileCfg.set("name", playlist.title());
            fileCfg.set("user", playlist.author());
            fileCfg.set("songs", playlist.songs().stream().map(Song::path).toList());
            fileCfg.save();
        }

        RecordHandler.cache();
    }

    public static void removePlaylist(Playlist playlist) {
        new File(playlist.path()).delete();
    }

    public static void save(Playlist playlist) {
        try(CommentedFileConfig fileCfg = CommentedFileConfig.builder(new File(playlist.path())).build()) {
            fileCfg.load();
            fileCfg.set("name", playlist.title());
            fileCfg.set("user", playlist.author());
            fileCfg.set("songs", playlist.songs().stream().map(Song::path).toList());
            fileCfg.save();
        }
    }

    public static List<Playlist> getPlaylists() {
        return List.copyOf(PLAYLIST_CONFIG_MAP.keySet());
    }

}
