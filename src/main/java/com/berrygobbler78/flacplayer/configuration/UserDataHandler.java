package com.berrygobbler78.flacplayer.configuration;

import com.berrygobbler78.flacplayer.util.ResourceHandler;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

import static com.berrygobbler78.flacplayer.configuration.UserDataHandler.ConfigLocation.*;

public class UserDataHandler {
    private static final Logger logger = LogManager.getLogger();
    private static CommentedFileConfig fileConfig;

    public enum ConfigLocation {
        USERNAME("general.username"),
        PATH("files.path");

        final String string;

        ConfigLocation(String s) {
            string = s;
        }

        String get() {
            return string;
        }
    }

    public static void initialize() {
        logger.debug("Initializing {}", UserDataHandler.class.getName());

        var configFile = new File(ResourceHandler.get(ResourceHandler.ResourceType.CACHE), "user-data.toml");
        if(!configFile.exists()) {
            try {
                if(configFile.createNewFile()) logger.info("Created new user data config file");
            } catch (IOException e) {
                logger.error("Failed to create new user data config file : {}", e.getMessage());
            }
        }

        fileConfig = CommentedFileConfig.builder(configFile)
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

        setIfMissing(USERNAME.get(), ".");
        setIfMissing(PATH.get(), "/");

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

    public static String getConfig(ConfigLocation location) {
        ensureInitialized();
        return fileConfig.get(location.get());
    }

    public static <T> void setConfig(ConfigLocation location, T value) {
        ensureInitialized();
        fileConfig.set(location.get(), value);
    }

    private static void ensureInitialized() {
        if (fileConfig == null) {
            throw new IllegalStateException("UserDataHandler not initialized!");
        }
    }
}