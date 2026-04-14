package com.berrygobbler78.flacplayer.util;

import com.berrygobbler78.flacplayer.App;

import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;

import java.io.*;

public class FileUtils {
    private static final Logger logger = LogManager.getLogger();

    public enum FILTER_TYPE {
        FOLDER(File::isDirectory),
        FLAC(f -> f.getName().toLowerCase().endsWith(".flac")),
        MP3(f -> f.getName().toLowerCase().endsWith(".mp3"));

        final FileFilter filter;

        FILTER_TYPE(FileFilter filter) {
            this.filter = filter;
        }

        public FileFilter get() {
            return filter;
        }
    }

    public static File fileChooser(Stage stage, String title, String directoryPath, String extensionDesc, String extension) {
        var dir = new File(directoryPath);
        if(!dir.exists() || !dir.isDirectory()) dir = new File(System.getProperty("user.home"));

        var fileChooser = new FileChooser();
            fileChooser.setTitle(title);
            fileChooser.setInitialDirectory(dir);
            fileChooser.getExtensionFilters().addAll(new ExtensionFilter(extensionDesc, extension));

        return fileChooser.showOpenDialog(stage);
    }

    public static File openDirectoryChooser(Stage stage, String title, String atPath) {
        var directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle(title);
        directoryChooser.setInitialDirectory(new File(atPath));

        return directoryChooser.showDialog(stage);
    }

    public static void openFileExplorer(String path) {
        try {
            switch (App.getOS()) {
                case LINUX -> Runtime.getRuntime().exec(new String[]{"xdg-open", path});
                case WINDOWS_11 -> Runtime.getRuntime().exec(new String[]{"explorer.exe", "/select,", path});
            }
        } catch (IOException e) {
            logger.error("Failed to open file explorer for '{}' : {}", path, e.getMessage());
        }
    }

    public static String makeFolderSafe(String in) {
        return in.toLowerCase().replace(" ", "-").replace(".", "");
    }
}
