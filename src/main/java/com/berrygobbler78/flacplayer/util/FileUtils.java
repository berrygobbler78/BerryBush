package com.berrygobbler78.flacplayer.util;

import com.berrygobbler78.flacplayer.App;

import com.berrygobbler78.flacplayer.configuration.UserDataHandler;
import com.berrygobbler78.flacplayer.util.records.Song;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import java.io.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileUtils {
    private static final Logger LOGGER = Logger.getLogger(FileUtils.class.getName());
    private static final FlacDecoder DECODER = new FlacDecoder();

    static {
        Logger.getLogger("org.jaudiotagger").setLevel(Level.SEVERE);
    }

    private static final ArrayList<Song> songList = new ArrayList<>();

    public enum FILE_TYPE{
        SONG,
        ALBUM,
        ARTIST,
        PLAYLIST
    }

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
            LOGGER.warning("Failed to read metadata for: " + song.getPath());
            return "Unknown";
        }
    }

    public static File flacToWav(String fileIn) throws IOException {
        LOGGER.info("Starting decoding for: " + fileIn);
        File temp = File.createTempFile("current", ".wav");
        temp.deleteOnExit();

        DECODER.flacToWav(fileIn, temp.getAbsolutePath());
        LOGGER.info("Done!");

        return temp;
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
            LOGGER.warning("Failed to open file explorer for path: " + path);
        }
    }

    public static String makeFolderSafe(String in) {
        return in
                .toLowerCase()
                .replace(" ", "-")
                .replace(".", "");
    }
}
