package com.berrygobbler78.flacplayer;

import java.io.*;
import java.net.URISyntaxException;
import java.util.concurrent.*;
import java.util.logging.Level;

import com.berrygobbler78.flacplayer.configuration.UserDataHandler;
import com.berrygobbler78.flacplayer.gui.controllers.SetupWizardController;
import com.berrygobbler78.flacplayer.music.MusicInterface;

import com.berrygobbler78.flacplayer.util.ImageUtils;
import com.berrygobbler78.flacplayer.util.ResourceHandler;
import com.berrygobbler78.flacplayer.util.records.RecordHandler;
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

    public enum OS {
        LINUX,
        WINDOWS_10,
        WINDOWS_11,
        MAC
    }

    private static OS currentOS;

    private static MusicInterface musicInterface;

    private static final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    @Override
    public void start(Stage stage) throws IOException {
        java.util.logging.Logger.getLogger("org.jaudiotagger").setLevel(Level.SEVERE);

         currentOS = switch (System.getProperty("os.name")) {
            case "Linux" -> OS.LINUX;
            case "Windows 10" -> OS.WINDOWS_10;
            case "Windows 11" -> OS.WINDOWS_11;
            case "Mac" -> OS.MAC;
             default -> throw new IllegalStateException("Unexpected value: " + System.getProperty("os.name"));
         };

        ResourceHandler.initialize();
        UserDataHandler.initialize();

        if(UserDataHandler.getConfig(UserDataHandler.ConfigLocation.PATH).equals(File.separator)  || UserDataHandler.getConfig(UserDataHandler.ConfigLocation.USERNAME).equals(".")) new SetupWizardController(stage);

        RecordHandler.cache();
        ImageUtils.refreshAllArt(false);

        setupStage(stage);
    }

    private void setupStage(Stage stage) throws IOException {
        logger.info("Initializing...");
        var fxmlLoader = new FXMLLoader(App.class.getResource("fxml/landing.fxml"));
        var scene = new Scene(fxmlLoader.load());
        musicInterface = new MusicInterface(fxmlLoader.getController());
        scene.getStylesheets().add(String.valueOf(App.class.getResource("css/landing.css")));
        scene.getStylesheets().add(String.valueOf(App.class.getResource("css/bottom-bar.css")));

        stage.setTitle("BerryBush");
        stage.setScene(scene);
        stage.getIcons().add(new Image(String.valueOf(App.class.getResource("graphics/berries.png"))));
        stage.show();
        stage.setOnCloseRequest(_ -> {
            musicInterface.cleanUp();
            executorService.shutdown();
        });


        if(currentOS == OS.WINDOWS_11) {
            var themeWindowManager = (Win11ThemeWindowManager) ThemeWindowManagerFactory.create();
            themeWindowManager.setWindowFrameColor(stage, Color.web("#121212"));
            themeWindowManager.setDarkModeForWindowFrame(stage, true);
        }

        logger.info("Stage created.");
    }

    public static void submitTask(Runnable task) {
        executorService.submit(task);
    }

    public static OS getOS() {
        return currentOS;
    }

    public static MusicInterface getMusicInterface() {
        return musicInterface;
    }
}