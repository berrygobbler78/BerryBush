package com.berrygobbler78.flacplayer.util;

import com.berrygobbler78.flacplayer.App;

import com.berrygobbler78.flacplayer.util.handlers.ResourceHandler;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import java.io.*;

public class FileUtils {
    private static final Logger logger = LogManager.getLogger();

    private static final FlacDecoder DECODER = new FlacDecoder();

    public enum FILTER_TYPE {
        FOLDER(File::isDirectory),
        FLAC(f -> f.getName().toLowerCase().endsWith(".flac")),
        COVER_IMAGE(f -> f.getAbsolutePath().endsWith("coverImage.png")),
        COVER_ICON(f -> f.getAbsolutePath().endsWith("coverIcon.png"));

        final FileFilter filter;

        FILTER_TYPE(FileFilter filter) {
            this.filter = filter;
        }

        public FileFilter get() {
            return filter;
        }
    }

    public static String getMetadataField(File song, FieldKey key) {
        try {
            AudioFile audioFile = AudioFileIO.read(song);
            Tag tag = audioFile.getTag();

            if (tag == null) return "Unknown";

            String value = tag.getFirst(key);
            return (value == null || value.isBlank()) ? "Unknown" : value;

        } catch (Exception e) {
            logger.error("Failed to read metadata for '{}' : {}", song.getPath(), e.getMessage());
            return "Unknown";
        }
    }

    public static File flacToWav(String fileIn) throws IOException {
        logger.debug("Starting decoding at {}", fileIn);
        File cache = ResourceHandler.getResourceFile("cache");
        File tempFile = new File(cache, "temp.wav");

        DECODER.flacToWav(fileIn, tempFile.getAbsolutePath());
        logger.debug("Finished decoding");

        return tempFile;
    }

    public static File fileChooser(Stage stage, String title, String directoryPath, String extensionDesc, String extension) {
        File dir = new File(directoryPath);
        if(!dir.exists() || !dir.isDirectory()) dir = new File(System.getProperty("user.home"));

        FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle(title);
            fileChooser.setInitialDirectory(dir);
            fileChooser.getExtensionFilters().addAll(new ExtensionFilter(extensionDesc, extension));

        return fileChooser.showOpenDialog(stage);
    }

    public static File openDirectoryChooser(Stage stage, String title, String atPath) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle(title);
        directoryChooser.setInitialDirectory(new File(atPath));

        return directoryChooser.showDialog(stage);
    }

    public static void openFileExplorer(String path) {
        try {
            switch (App.getCurrentOS()) {
                case LINUX -> Runtime.getRuntime().exec(new String[]{"xdg-open", path});
                case WINDOWS_11 -> Runtime.getRuntime().exec(new String[]{"explorer.exe", "/select,", path});
            }
        } catch (IOException e) {
            logger.error("Failed to open file explorer for '{}' : {}", path, e.getMessage());
        }
    }

    public static String makeFolderSafe(String in) {
        return in
                .toLowerCase()
                .replace(" ", "-")
                .replace(".", "");
    }
}
