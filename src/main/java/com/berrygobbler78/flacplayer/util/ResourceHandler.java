package com.berrygobbler78.flacplayer.util;

import com.berrygobbler78.flacplayer.App;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class ResourceHandler {
    private static final Logger logger = LogManager.getLogger();
    private static File cacheDir;

    public static void initialize() throws NullPointerException {
        switch (App.getOS()) {
            case LINUX -> linuxSetup();
            case WINDOWS_10, WINDOWS_11 -> windowsSetup();
        }

        checkIfDirPresent("artist-art", "album-art", "playlist-art", "playlists");
    }

    public static void checkIfDirPresent(String... locations) {
        for(String location : locations) {
            var check = new File(cacheDir, location);
            if(!check.exists()) {
                if(check.mkdirs()) logger.info("Created new '{}' directory", location);
            }
        }
    }

    public static void linuxSetup() {
        File root;
        String xdgCache = System.getenv("XDG_CACHE_HOME");
        if (xdgCache != null && !xdgCache.isEmpty()) {
            root = new File(xdgCache);
        } else {
            String home = System.getenv("HOME");
            root =  new File(home, ".cache");
        }

        cacheDir = new File(root, "berry-bush");
        if(!cacheDir.exists() || !cacheDir.isDirectory()) {
            if(cacheDir.mkdirs()) logger.info("Created new Linux cache directory");
        }
    }

    public static void windowsSetup() {
        var root = new File(System.getenv("LOCALAPPDATA"));
        cacheDir = new File(root, "BerryBush");
    }

    public static File getCache() {
        return cacheDir;
    }
}
