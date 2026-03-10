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

        URL playlists = App.class.getResource("cache" + File.separator + "playlists");
        if(playlists == null) {
            File cacheFile = new File(new File(cache.toURI()), "playlists");
            if(cacheFile.mkdirs()) {
                logger.debug("Created new playlists file at '{}'", cacheFile.getAbsolutePath());
            }
        }

        URL albumArt = App.class.getResource("cache" + File.separator + "albumArt");
        if(albumArt == null) {
            File cacheFile = new File(new File(cache.toURI()), "album-art");
            if(cacheFile.mkdirs()) {
                logger.debug("Created new artist-art file at '{}'", cacheFile.getAbsolutePath());
            }
        }

        URL playlistArt = App.class.getResource("cache" + File.separator + "playlistArt");
        if(playlistArt == null) {
            File cacheFile = new File(new File(cache.toURI()), "playlist-art");
            if(cacheFile.mkdirs()) {
                logger.debug("Created new playlist-art file at '{}'", cacheFile.getAbsolutePath());
            }
        }
    }

    public static File getRoot() {
        return resourcesFile;
    }

    public static URL getResourceURL(String resource) {
        URL resourceURL = App.class.getResource(resource);
        if(resourceURL == null) {
            logger.error("Failed to acquire resource URL with input '{}'", resource);
            return null;
        }

        return resourceURL;
    }

    public static File getResourceFile(String resource) {
        URL resourceURL = getResourceURL(resource);

        try {
            return new File(resourceURL != null ? resourceURL.toURI() : null);

        } catch (URISyntaxException | NullPointerException e) {
            logger.error("Failed to acquire resource file with input '{}'", resource);
            return null;
        }
    }
}
