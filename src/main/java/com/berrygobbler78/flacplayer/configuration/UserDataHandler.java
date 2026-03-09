package com.berrygobbler78.flacplayer.configuration;

import com.berrygobbler78.flacplayer.util.handlers.ResourceHandler;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class UserDataHandler {
    private static final Logger logger = LogManager.getLogger();
    private static CommentedFileConfig fileConfig;

    private static final String USERNAME = "general.username";
    private static final String PATH = "files.path";

    public static void initialize() {
        logger.debug("Initializing {}", UserDataHandler.class.getName());

        fileConfig = CommentedFileConfig.builder(new File(ResourceHandler.getRoot(), "user-data.toml"))
                .sync()
                .autosave()
                .autoreload()
                .build();
        fileConfig.load();

        generateDefaults();
    }

    public static void generateDefaults() {
        logger.info("Generating defaults...");

        // Need first time load
        try {
            fileConfig.load();
        } catch (Exception e) {
            logger.error("Error with creating config. Clearing... : {}", e.getMessage());
            fileConfig.clear();
        }

        // General settings
        setIfMissing(USERNAME, ".");

        // Files settings
        setIfMissing(PATH, "/");

        /*
        * n = number
        * add separators like -, /, ,, .,
        * e = end (.flac, .mp3, ...)
        * a = artist
        */
        // setIfMissing("files.naming", "user");

        // Need first time save
        fileConfig.save();
    }

    private static <T> void setIfMissing(String path, T value) {
        if (!fileConfig.contains(path)) {
            fileConfig.set(path, value);
        }
    }

    public static String getUsername() {
        ensureInitialized();
        return fileConfig.get(USERNAME);
    }

    public static void setUsername(String username) {
        ensureInitialized();
        fileConfig.set(USERNAME, username);
    }

    public static String getPath() {
        ensureInitialized();
        return fileConfig.get(PATH);
    }

    public static void setPath(String path) {
        ensureInitialized();
        fileConfig.set(PATH, path);
    }

    private static void ensureInitialized() {
        if (fileConfig == null) {
            throw new IllegalStateException("UserDataHandler not initialized!");
        }
    }
}