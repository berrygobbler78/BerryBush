package com.berrygobbler78.flacplayer;

import java.io.*;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

//import com.berrygobbler78.flacplayer.userdata.Playlist;
import com.berrygobbler78.flacplayer.configuration.PlaylistDataHandler;
import com.berrygobbler78.flacplayer.configuration.UserDataHandler;
import com.berrygobbler78.flacplayer.util.Constants;
import com.berrygobbler78.flacplayer.util.FileUtils;

import com.berrygobbler78.flacplayer.util.handlers.MediaTransportHandler;
import com.berrygobbler78.flacplayer.util.handlers.ResourceHandler;
import com.berrygobbler78.flacplayer.util.handlers.RecordHandler;
import com.pixelduke.window.ThemeWindowManagerFactory;
import com.pixelduke.window.Win11ThemeWindowManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.controlsfx.dialog.Wizard;
import org.controlsfx.dialog.WizardPane;

public class App extends Application {
    private static final Logger LOGGER = Logger.getLogger(App.class.getName());

    private static Stage primaryStage;

    public enum OS {
        LINUX,
        WINDOWS_10,
        WINDOWS_11,
        MAC
    }

    private static OS currentOS;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException, URISyntaxException {
    switch (System.getProperty("os.name")) {
        case "Linux" -> currentOS = OS.LINUX;
        case "Windows 10" -> currentOS = OS.WINDOWS_10;
        case "Windows 11" -> currentOS = OS.WINDOWS_11;
        case "Mac" -> currentOS = OS.MAC;
    }

    deleteTempFile();

    ResourceHandler.initialize();
    UserDataHandler.initialize();
    PlaylistDataHandler.initialize();

    if(UserDataHandler.getPath().equals(File.separator)  || UserDataHandler.getUsername().equals(".")) setupWizard();

    RecordHandler.cache();

    FXMLLoader fxmlLoader =
           new FXMLLoader(ResourceHandler.getResourceURL("fxml/revised.fxml"));

    Scene scene = new Scene(fxmlLoader.load());
    scene.getStylesheets().add(App.class.getResource("css/styles.css").toExternalForm());

    primaryStage = stage;
       primaryStage.setTitle("BerryBush");
       primaryStage.setScene(scene);
       primaryStage.getIcons().add(Constants.IMAGES.BERRIES.get());
       primaryStage.show();
       primaryStage.setOnCloseRequest(_ -> MediaTransportHandler.shutdown());

    if(currentOS == OS.WINDOWS_11) {
       Win11ThemeWindowManager themeWindowManager =
               (Win11ThemeWindowManager) ThemeWindowManagerFactory.create(); // For coloring window border
       themeWindowManager.setWindowFrameColor(primaryStage, Color.web("#121212"));
       themeWindowManager.setDarkModeForWindowFrame(primaryStage, true);
    }

    LOGGER.info("Stage created.");
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    static void deleteTempFile() {
        try{
            File tempFile = ResourceHandler.getResourceFile("cache/temp.wav");

            if(tempFile.delete()) {
                LOGGER.log(Level.INFO, "Deleted temp file.");
            } else {
                LOGGER.log(Level.WARNING, "Failed to delete temp file.");
            }
        } catch(NullPointerException e) {
            LOGGER.log(Level.INFO, "Temp file does not exist.");
        }
    }

    static void setupWizard() {
        Wizard wizard = new Wizard();

        // Page 1
        Label pathQuestionLabel = new Label("Enter Directory Location:");

        TextField pathTextField = new TextField();
            pathTextField.setEditable(true);
            pathTextField.setPromptText("Select Directory");
            pathTextField.setMinWidth(300.0);
            pathTextField.setMaxHeight(10.0);
            pathTextField.setMaxWidth(300.0);

        Button directoryChooserButton = new Button();
        switch (currentOS) {
            case WINDOWS_11 ->
                directoryChooserButton.setOnAction(_ ->
                        pathTextField.setText(FileUtils.openDirectoryChooser(primaryStage, "Choose directory", "C://").getAbsolutePath()));
            case LINUX ->
                    directoryChooserButton.setOnAction(_ ->
                            pathTextField.setText(FileUtils.openDirectoryChooser(primaryStage, "Choose directory", "/home").getAbsolutePath()));

        }

            directoryChooserButton.setText("Open Explorer");

        HBox pathHbox = new HBox();
            pathHbox.getChildren().addAll(pathTextField, directoryChooserButton);
            pathHbox.setSpacing(10.0);

        VBox pathVbox = new VBox();
            pathVbox.getChildren().addAll(pathQuestionLabel, pathHbox);

        WizardPane pathPage = new WizardPane();
            pathPage.setContent(pathVbox);

        // Page2
        Label usernameQuestionLabel = new Label("Enter Username:");

        TextField usernameTextField = new TextField();
            usernameTextField.setEditable(true);
            usernameTextField.setPromptText("Username...");
            usernameTextField.setMinWidth(300.0);
            usernameTextField.setMaxHeight(10.0);
            usernameTextField.setMaxWidth(300.0);

        VBox usernameVbox = new VBox();
            usernameVbox.getChildren().addAll(usernameQuestionLabel, usernameTextField);

        WizardPane usernamePage = new WizardPane();
            usernamePage.setContent(usernameVbox);

        // Wizard settings
        wizard.setTitle("BerryBush Setup Wizard");
        wizard.setFlow(new Wizard.LinearFlow(usernamePage, pathPage));

        // Show wizard and wait, set userData
        wizard.showAndWait().ifPresent(result -> {
            if (result == ButtonType.FINISH) {
                UserDataHandler.setPath(pathTextField.getText());
                UserDataHandler.setUsername(usernameTextField.getText());
            }
        });
    }

    public static OS getCurrentOS() {
        return currentOS;
    }
}