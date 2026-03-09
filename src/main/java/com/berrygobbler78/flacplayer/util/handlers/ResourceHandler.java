package com.berrygobbler78.flacplayer.util.handlers;

import com.berrygobbler78.flacplayer.App;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Logger;

public class ResourceHandler {
    private static final Logger LOGGER =  Logger.getLogger(ResourceHandler.class.getName());
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
                LOGGER.info("Created new cache file with path: " + cacheFile.getAbsolutePath());
                cache = App.class.getResource("cache");
            }
        }

        if(cache == null) {
            LOGGER.severe("CACHE IS NULL! UH OH!");
            throw new NullPointerException("Cache directory is null");
        }

        URL playlists = App.class.getResource("cache" + File.separator + "playlists");
        if(playlists == null) {
            File cacheFile = new File(new File(cache.toURI()), "playlists");
            if(cacheFile.mkdirs()) {
                LOGGER.info("Created new playlists file with path: " + cacheFile.getAbsolutePath());
            }
        }

        URL albumArt = App.class.getResource("cache" + File.separator + "albumArt");
        if(albumArt == null) {
            File cacheFile = new File(new File(cache.toURI()), "album-art");
            if(cacheFile.mkdirs()) {
                LOGGER.info("Created new album-art file with path: " + cacheFile.getAbsolutePath());
            }
        }

        URL playlistArt = App.class.getResource("cache" + File.separator + "playlistArt");
        if(playlistArt == null) {
            File cacheFile = new File(new File(cache.toURI()), "playlist-art");
            if(cacheFile.mkdirs()) {
                LOGGER.info("Created new playlist-art file with path: " + cacheFile.getAbsolutePath());
            }
        }
    }

    public static File getRoot() {
        return resourcesFile;
    }

    public static URL getResourceURL(String resource) throws NullPointerException{
        URL resourceURL = App.class.getResource(resource);
        if(resourceURL == null) {
            LOGGER.warning("Failed to acquire resource URL: " + resource);
            throw new NullPointerException();
        }

        return resourceURL;
    }

    public static File getResourceFile(String resource) throws NullPointerException{
        URL resourceURL = getResourceURL(resource);

        try {
            return new File(resourceURL.toURI());

        } catch (URISyntaxException e) {
            LOGGER.warning("Failed to acquire resource file: " + resource + "Error: " + e);
            throw new NullPointerException();
        }
    }

    public static String getResourcesPath(String resource) {
        return getResourceURL(resource).getPath();
    }
}
