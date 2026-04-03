package com.berrygobbler78.flacplayer;

import java.io.*;
import java.net.URISyntaxException;

import com.berrygobbler78.flacplayer.configuration.UserDataHandler;
import com.berrygobbler78.flacplayer.controllers.SetupWizardController;
import com.berrygobbler78.flacplayer.music.MusicInterface;

import com.berrygobbler78.flacplayer.util.ImageUtils;
import com.berrygobbler78.flacplayer.music.MediaTransportHandler;
import com.berrygobbler78.flacplayer.util.handlers.ResourceHandler;
import com.berrygobbler78.flacplayer.util.handlers.RecordHandler;
import com.pixelduke.window.ThemeWindowManagerFactory;
import com.pixelduke.window.Win11ThemeWindowManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class App extends Application {
    private static final Logger logger = LogManager.getLogger();

    private static Stage primaryStage;

    public enum OS {
        LINUX,
        WINDOWS_10,
        WINDOWS_11,
        MAC
    }

    private static OS currentOS;

    private static MusicInterface musicInterface;

    @Override
    public void start(Stage stage) throws IOException, URISyntaxException {
        java.util.logging.Logger.getLogger("org.jaudiotagger").setLevel(java.util.logging.Level.SEVERE);

        switch (System.getProperty("os.name")) {
            case "Linux" -> currentOS = OS.LINUX;
            case "Windows 10" -> currentOS = OS.WINDOWS_10;
            case "Windows 11" -> currentOS = OS.WINDOWS_11;
            case "Mac" -> currentOS = OS.MAC;
        }

        ResourceHandler.initialize();
        UserDataHandler.initialize();

        if(UserDataHandler.getPath().equals(File.separator)  || UserDataHandler.getUsername().equals(".")) new SetupWizardController(stage);

        RecordHandler.cache();
        ImageUtils.refreshAllArt(false);

        setupStage(stage);
    }

    private void setupStage(Stage stage) throws IOException {
        logger.info("Initializing...");
        FXMLLoader fxmlLoader = new FXMLLoader(ResourceHandler.getResourceURL("fxml/landing.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        musicInterface = new MusicInterface(fxmlLoader.getController());
        scene.getStylesheets().add(ResourceHandler.getResourceURL("css/landing.css").toExternalForm());
        scene.getStylesheets().add(ResourceHandler.getResourceURL("css/bottom-bar.css").toExternalForm());

        primaryStage = stage;
        primaryStage.setTitle("BerryBush");
        primaryStage.setScene(scene);
        primaryStage.getIcons().add(new Image(ResourceHandler.getResourceURL("graphics/berries.png").toString()));
        primaryStage.show();
        primaryStage.setOnCloseRequest(_ -> MediaTransportHandler.shutdown());

        if(currentOS == OS.WINDOWS_11) {
            Win11ThemeWindowManager themeWindowManager =
                    (Win11ThemeWindowManager) ThemeWindowManagerFactory.create(); // For coloring the window border
            themeWindowManager.setWindowFrameColor(primaryStage, Color.web("#121212"));
            themeWindowManager.setDarkModeForWindowFrame(primaryStage, true);
        }

        logger.info("Stage created.");
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static OS getOS() {
        return currentOS;
    }

    public static MusicInterface getMusicInterface() {
        return musicInterface;
    }
}