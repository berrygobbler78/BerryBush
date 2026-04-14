package com.berrygobbler78.flacplayer.util;

import com.berrygobbler78.flacplayer.App;
import com.berrygobbler78.flacplayer.util.records.RecordHandler;
import com.berrygobbler78.flacplayer.util.records.Album;
import com.berrygobbler78.flacplayer.util.records.Artist;
import com.berrygobbler78.flacplayer.util.records.Song;
import javafx.scene.image.Image;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.flac.metadatablock.MetadataBlockDataPicture;
import org.jaudiotagger.tag.flac.FlacTag;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class ImageUtils {
    private static final Logger logger = LogManager.getLogger();

    private static final String WARNING = App.class.getResource("graphics/warning.png").toString();

    public static void refreshAllArt(boolean force) {
        refreshAlbumArt(force);
        refreshArtistArt(force);
        refreshPlaylistArt();
    }

    public static void refreshArtistArt(boolean force) {
        logger.info("Refreshing artist art...");

        if(force) deleteArt(false, true);
        var dir = ResourceHandler.get(ResourceHandler.ResourceType.ARTIST_ART);
        
        for(Artist artist : RecordHandler.getArtists()) {
            var artistDir = new File(dir, FileUtils.makeFolderSafe(artist.title()));
            markDirectory(artistDir);

            var files = artistDir.listFiles(f -> f.getName().endsWith(".png") || f.getName().endsWith(".jpg"));
            if(files == null || files.length == 0) continue;

            if(files.length > 1) {
                logger.warn("More than one image found for artist '{}', using first found: {}", artist.title(), files[0].getAbsolutePath());
            }

            var imageCheck = bufferedImageFromPath(files[0].getAbsolutePath());
            if(imageCheck.isEmpty()) continue;
            var image = imageCheck.get();

            image = resizeBufferedImage(image, 600, 600);
            image = makeRoundedCorner(image, 50);

            try {
                ImageIO.write(image, "png", new File(artistDir, "art.png"));
            } catch (IOException e) {
                logger.error("Failed to write artist cover at '{}' | {}", files[0].getAbsolutePath(), e.getMessage());
            }
        }

    }

    public static void refreshAlbumArt(boolean force) {
        logger.info("Refreshing album art...");

        if(force) deleteArt(true, false);

        for(Album album : RecordHandler.getAlbums()) {
            var dir = ResourceHandler.get(ResourceHandler.ResourceType.ALBUM_ART);
            var artistDir = new File(dir, FileUtils.makeFolderSafe(album.artist().title()));
            markDirectory(artistDir);

            var albumDir = new File(artistDir, FileUtils.makeFolderSafe(album.title()));
            markDirectory(albumDir);

            var imageFile = new File(albumDir, "art.png");

            BufferedImage extractedArt;

            if(!imageFile.exists()) {
                logger.error("No cover image files found at '{}', generating new image", album.artPath());
                try {
                    if(imageFile.createNewFile()) logger.debug("Created new image file at '{}'", imageFile.getPath());
                } catch (IOException e) {
                    logger.error("Failed to create new image file at '{}' | {}", imageFile.getPath(), e.getMessage());
                }

                try{
                    Optional<BufferedImage> bufferedImageFromSong = bufferedImageFromSong(album.songs().getFirst());
                    if(bufferedImageFromSong.isEmpty()) continue;
                    extractedArt = bufferedImageFromSong.get();
                    var coverBufferedImage = resizeBufferedImage(extractedArt, 600, 600);
                    ImageIO.write(makeRoundedCorner(coverBufferedImage, 50), "png", imageFile);
                } catch (Exception e) {
                    logger.error("Failed to write album cover at '{}' | {}", imageFile.getPath(), e.getMessage());
                    continue;
                }

                logger.info("Generated album cover for '{}'", album.title());
            }
        }

        logger.info("Album art has been refreshed");
    }

    public static void markDirectory(File file) {
        if(!file.exists()) {
            if ((file.mkdirs())) {
                logger.debug("Directory '{}' created", file.getName());
            } else {
                logger.debug("Failed to create directory for '{}'", file.getName());
            }
        }
    }

    public static void deleteArt(boolean album, boolean artist) {
        logger.info("Deleting art cache");

        if(artist) {
            var dir = ResourceHandler.get(ResourceHandler.ResourceType.ARTIST_ART);
            var files = dir.listFiles();

            if(files == null) return;

            for(File f : files) {
                if(f.delete()) {
                    logger.debug("Deleted artist file: {}", f.getName());
                }
            }

        }

        if(album) {
            var dir = ResourceHandler.get(ResourceHandler.ResourceType.ALBUM_ART);
            var files = dir.listFiles();

            if(files == null) return;

            for(File f : files) {
                if(f.delete()) {
                    logger.debug("Deleted album file: {}", f.getName());
                }
            }

        }
    }

    public static void refreshPlaylistArt() {
        logger.info("Refreshing playlist art...");

        var playlistDir = ResourceHandler.get(ResourceHandler.ResourceType.PLAYLIST_ART);

        // for(Playlist playlist : PlaylistDataHandler.getPlaylists()) {
        //     File playlistFolder = new File(playlistDir, playlist.getName().toLowerCase().replace(' ', '-').trim());
        //     if(!playlistFolder.exists() || !playlistFolder.isDirectory()) {
        //         if(playlistFolder.mkdirs()) {
        //             logger.info("Created playlist folder at '{}'", playlistFolder.getAbsolutePath());
        //         } else {
        //             logger.error("Failed to create playlist folder at {}", playlistFolder.getAbsolutePath());
        //         }
        //     }
        // }

        // TODO: Generate image from top 4 songs if no image is provided

        logger.info("Playlist art has been refreshed.");
    }


    /**
        This method is pretty slow, so try to use it as little as possible
        @param song returns a buffered image from the song's embedded path
    */
    public static Optional<BufferedImage> bufferedImageFromSong(Song song) throws Exception {
        logger.debug("Getting image from song '{}'", song.title());
        var audioFile = AudioFileIO.read(new File(song.path()));
        var tag = (FlacTag) audioFile.getTag();
        if(tag == null || tag.getImages().isEmpty()) {
            logger.error("No embedded cover art found for path: {}", song.path());
            return Optional.empty();
        }

        MetadataBlockDataPicture coverPicture = tag.getImages().getFirst();
        return Optional.of(ImageIO.read(ImageIO.createImageInputStream(new ByteArrayInputStream(coverPicture.getImageData()))));
    }

    public static Optional<Image> pathToImage(String path) {
        if (path == null || path.isEmpty()) return Optional.empty();

        var file = new File(path);
        if (!file.exists()) {
            logger.error("Image file not found at: {}", path);
            return Optional.empty();
        }

        return Optional.of(new Image(file.toURI().toString(), true));
    }

    public static BufferedImage makeRoundedCorner(BufferedImage image, int cornerRadius) {
        int w = image.getWidth();
        int h = image.getHeight();

        var output = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        var g2 = output.createGraphics();
        g2.setComposite(AlphaComposite.Src);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, cornerRadius, cornerRadius));
        g2.setComposite(AlphaComposite.SrcAtop);
        g2.drawImage(image, 0, 0, null);
        g2.dispose();

        return output;
    }

    public static BufferedImage resizeBufferedImage(BufferedImage bi, int width, int height) {
        java.awt.Image tempImage = bi.getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH);

        bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = bi.createGraphics();
        g2d.drawImage(tempImage, 0, 0, null);
        g2d.dispose();

        return bi;
    }

    public static Optional<BufferedImage> bufferedImageFromPath(String path) {
        try {
            return Optional.of(ImageIO.read(new File(path)));
        } catch (IOException e) {
            logger.error("Failed to read image from path: {}", path);
            return Optional.empty();
        }
    }

    public static String getWarningURL() {
        logger.debug("Using warning image as fallback");
        return WARNING;
    }
}
