package com.berrygobbler78.flacplayer.util;

import com.berrygobbler78.flacplayer.configuration.PlaylistDataHandler;
import com.berrygobbler78.flacplayer.util.handlers.RecordHandler;
import com.berrygobbler78.flacplayer.util.handlers.ResourceHandler;
import com.berrygobbler78.flacplayer.util.records.Album;
import com.berrygobbler78.flacplayer.util.records.Artist;
import javafx.scene.image.Image;
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
import java.util.logging.Logger;

public class ImageUtils {
    private static final Logger LOGGER = Logger.getLogger(ImageUtils.class.getName());

    public static void refreshAllArt() {
        refreshAlbumArt();
        refreshPlaylistArt();
    }

    public static void refreshAlbumArt() {
        LOGGER.info("Refreshing album art...");

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

            if(!imageFile.getParentFile().exists()) {
                try{
                    imageFile.getParentFile().createNewFile();
                    imageFile.getParentFile().mkdirs();
                } catch(IOException e) {
                    LOGGER.warning("Failed to create new image dir: " + e.getMessage());
                }
            }

            BufferedImage extractedArt = null;

            if(!imageFile.exists()) {
                LOGGER.warning("No cover image files found, generating new image. Path:" + album.imagePath());
                try {
                    imageFile.createNewFile();
                } catch (IOException e) {
                    LOGGER.warning("Failed to create new image file: " + e.getMessage());
                }

                try{
                    extractedArt = bufferedImageFromSong(album.songs().getFirst().path());

                    BufferedImage coverBufferedImage = resizeBufferedImage(extractedArt, 600, 600);
                    ImageIO.write(makeRoundedCorner(coverBufferedImage, 50), "png", imageFile);
                } catch (Exception e) {
                    LOGGER.warning("Error while writing cover image file for album: " + album.title() + " Error: " + e.getCause());
                    continue;
                }

                LOGGER.info("Generated cover image for " + album.title());
            }

            if(!iconFile.exists()) {
                LOGGER.warning("No cover icon files found, generating new icon. Path:" + album.iconPath());
                try {
                    iconFile.getParentFile().mkdirs();
                    iconFile.createNewFile();
                } catch (IOException e) {
                    LOGGER.warning("Failed to create new image file: " + e.getMessage());
                }

                try {
                    if (extractedArt == null) {
                        extractedArt = bufferedImageFromSong(album.songs().getFirst().path());
                    }
                    BufferedImage

                            coverBufferedImage = resizeBufferedImage(extractedArt, 20, 20);
                    ImageIO.write(makeRoundedCorner(coverBufferedImage, 2), "png", iconFile);
                } catch (Exception e) {
                    LOGGER.warning("Error while writing cover icon file for album: " + album.title() + " Error: " + e.getCause());
                    continue;
                }

                LOGGER.info("Generated cover icon for " + album.title());
            }
        }

        LOGGER.info("Album art has been refreshed");
    }

    public static void refreshPlaylistArt() {
        LOGGER.info("Refreshing playlist art...");

        File playlistDir = ResourceHandler.getResourceFile("cache/playlist-art");
        if(!playlistDir.exists() || !playlistDir.isDirectory()) {
            LOGGER.warning("Playlist directory does not exist or is not a directory. Path:" + playlistDir.getAbsolutePath());
            return;
        }

        for(PlaylistDataHandler.Playlist playlist : PlaylistDataHandler.getPlaylists()) {
            File playlistFolder = new File(playlistDir, playlist.getName().toLowerCase().replace(' ', '-').trim());
            if(!playlistFolder.exists() || !playlistFolder.isDirectory()) {
                if(playlistFolder.mkdirs()) {
                    LOGGER.info("Created playlist folder: " + playlistFolder.getAbsolutePath());
                } else {
                    LOGGER.warning("Failed to create playlist folder: " + playlistFolder.getAbsolutePath());
                }
            }
        }

        // TODO: Generate image from top 4 songs if no image is provided

        LOGGER.info("Playlist art has been refreshed.");
    }

    // This method is pretty slow so try to use it as little as possible
    public static BufferedImage bufferedImageFromSong(String songPath) throws Exception {
        LOGGER.info("Getting image from song path: " + songPath);
        AudioFile audioFile = AudioFileIO.read(new File(songPath));
        FlacTag tag = (FlacTag) audioFile.getTag();
        if(tag == null || tag.getImages().isEmpty()) {
            throw new IllegalStateException("No embedded cover art found for path: " + songPath);
        }

        MetadataBlockDataPicture coverPicture = tag.getImages().getFirst();
        return ImageIO.read(ImageIO.createImageInputStream(new ByteArrayInputStream(coverPicture.getImageData())));
    }

    public static Image pathToImage(String path) {
        LOGGER.info("Getting image for: " + path);
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
