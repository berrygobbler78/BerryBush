package com.berrygobbler78.flacplayer.util.handlers;

import com.berrygobbler78.flacplayer.App;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

public class ResourceHandler {
    private static final Logger logger = LogManager.getLogger();
    private static File resourcesFile;

    public static void initialize() throws URISyntaxException, NullPointerException {
        // FIXME: Generate outside of jar

        URL resourcesURL = App.class.getResource("");
        if(resourcesURL == null) {
            throw new NullPointerException();
        }
        resourcesFile = new File(resourcesURL.toURI());

        URL cache = App.class.getResource("cache");
        if(cache == null) {
            File cacheFile = new File(resourcesFile, "cache");
            if(cacheFile.mkdirs()) {
                logger.debug("Created new cache file at '{}'", cacheFile.getAbsolutePath());
                cache = App.class.getResource("cache");
            }
        }

        if(cache == null) {
            logger.fatal("CACHE IS NULL! UH OH!");
            throw new NullPointerException("Cache directory is null");
        }

        File cacheFile = new File(cache.toURI());

        checkIfPresent("artist-art", "cache/artist-art", cacheFile);
        checkIfPresent("album-art", "cache/album-art", cacheFile);
        checkIfPresent("playlist-art", "cache/playlist-art", cacheFile);
        checkIfPresent("playlists", "cache/playlists", cacheFile);
    }

    public static void checkIfPresent(String name, String inputURL, File parent) throws URISyntaxException {
        URL resourceURL = App.class.getResource(inputURL);
        if(resourceURL == null) {
            File newFile = new File(parent, name);
            if(newFile.mkdirs()) {
                logger.debug("Created new '{}' file at '{}'", name, newFile.getAbsolutePath());
            }
        }
    }

    public static File getRoot() {
        return resourcesFile;
    }

    public static URL getResourceURL(String resource) {
        URL resourceURL = App.class.getResource(resource);
        if(resourceURL == null) {
            logger.error("Failed to acquire resource URL with input '{}' : URL is null", resource);
            return null;
        }

        return resourceURL;
    }

    public static File getResourceFile(String resource) {
        URL resourceURL = getResourceURL(resource);
        if(resourceURL == null) return null;

        try {
            return new File(resourceURL.toURI());
        } catch (URISyntaxException | NullPointerException e) {
            logger.error("Failed to acquire resource file with input '{}' : {}", resource, e.getMessage());
            return null;
        }
    }
}
