package com.berrygobbler78.flacplayer.util;

import com.berrygobbler78.flacplayer.util.handlers.RecordHandler;
import com.berrygobbler78.flacplayer.util.handlers.ResourceHandler;
import com.berrygobbler78.flacplayer.util.records.Album;
import javafx.scene.image.Image;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jaudiotagger.audio.AudioFile;
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

public class ImageUtils {
    private static final Logger logger = LogManager.getLogger();

    public static void refreshAllArt(boolean force) {
        refreshAlbumArt(force);
        refreshPlaylistArt();
    }

    public static void refreshAlbumArt(boolean force) {
        logger.info("Refreshing album art...");

        if(force) deleteArt();

        for(Album album : RecordHandler.getAlbumList()) {
            File dir = ResourceHandler.getResourceFile("cache/album-art");
            File artistDir = new File(dir, FileUtils.makeFolderSafe(album.artist().name()));
            if(!artistDir.exists()) {
                artistDir.mkdirs();
            }

            File albumDir = new File(artistDir, FileUtils.makeFolderSafe(album.title()));
            if(!albumDir.exists()) {
                albumDir.mkdirs();
            }

            File imageFile = new File(albumDir, "coverImage.png");
            File iconFile = new File(albumDir, "coverIcon.png");

            BufferedImage extractedArt = null;

            if(!imageFile.exists()) {
                logger.error("No cover image files found at '{}', generating new image", album.imagePath());
                try {
                    imageFile.createNewFile();
                } catch (IOException e) {
                    logger.error("Failed to create new image file at '{}' : {}", imageFile.getPath(), e.getMessage());
                }

                try{
                    extractedArt = bufferedImageFromSong(album.songs().getFirst().path());

                    BufferedImage coverBufferedImage = resizeBufferedImage(extractedArt, 600, 600);
                    ImageIO.write(makeRoundedCorner(coverBufferedImage, 50), "png", imageFile);
                } catch (Exception e) {
                    logger.error("Failed to write image at '{}' : {}", imageFile.getPath(), e.getMessage());
                    continue;
                }

                logger.info("Generated cover image for '{}'", album.title());
            }

            if(!iconFile.exists()) {
                logger.error("No cover icon files found at '{}', generating new image", album.imagePath());
                try {
                    iconFile.getParentFile().mkdirs();
                    iconFile.createNewFile();
                } catch (IOException e) {
                    logger.error("Failed to create new icon file at '{}' : {}", iconFile.getPath(), e.getMessage());
                }

                try {
                    if (extractedArt == null) {
                        extractedArt = bufferedImageFromSong(album.songs().getFirst().path());
                    }
                    BufferedImage

                            coverBufferedImage = resizeBufferedImage(extractedArt, 20, 20);
                    ImageIO.write(makeRoundedCorner(coverBufferedImage, 2), "png", iconFile);
                } catch (Exception e) {
                    logger.error("Failed to write icon at '{}' : {}", iconFile.getPath(), e.getMessage());
                    continue;
                }

                logger.info("Generated cover icon for '{}'", album.title());
            }
        }

        logger.info("Album art has been refreshed");
    }

    public static void deleteArt() {
        logger.info("Deleting art cache");

        File dir = ResourceHandler.getResourceFile("cache/album-art");
        File[] files = dir.listFiles();
        if(files == null) return;

        for(File f : files) {
            f.delete();
        }
    }

    public static void refreshPlaylistArt() {
        logger.info("Refreshing playlist art...");

        File playlistDir = ResourceHandler.getResourceFile("cache/playlist-art");
        if(!playlistDir.exists() || !playlistDir.isDirectory()) {
            logger.error("Playlist directory does not exist or is not a directory '{}'", playlistDir.getAbsolutePath());
            return;
        }

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

    // This method is pretty slow, so try to use it as little as possible
    public static BufferedImage bufferedImageFromSong(String songPath) throws Exception {
        logger.debug("Getting image from '{}'", songPath);
        AudioFile audioFile = AudioFileIO.read(new File(songPath));
        FlacTag tag = (FlacTag) audioFile.getTag();
        if(tag == null || tag.getImages().isEmpty()) {
            throw new IllegalStateException("No embedded cover art found for path: " + songPath);
        }

        MetadataBlockDataPicture coverPicture = tag.getImages().getFirst();
        return ImageIO.read(ImageIO.createImageInputStream(new ByteArrayInputStream(coverPicture.getImageData())));
    }

    public static Image pathToImage(String path) {
        logger.debug("Getting image from '{}'", path);
        return new Image(ResourceHandler.getResourceURL(path).toString(), true);
    }

    public static BufferedImage makeRoundedCorner(BufferedImage image, int cornerRadius) {
        int w = image.getWidth();
        int h = image.getHeight();

        BufferedImage output = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = output.createGraphics();
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
}
