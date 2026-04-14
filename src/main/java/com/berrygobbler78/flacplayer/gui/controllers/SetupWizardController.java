package com.berrygobbler78.flacplayer.gui.controllers;

import com.berrygobbler78.flacplayer.App;
import com.berrygobbler78.flacplayer.configuration.UserDataHandler;
import com.berrygobbler78.flacplayer.util.FileUtils;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.controlsfx.dialog.Wizard;
import org.controlsfx.dialog.WizardPane;

public class SetupWizardController {
    public SetupWizardController(Stage stage) {
        Wizard wizard = new Wizard();

        // Page 1
        var pathQuestion = new Label("Enter Directory Location:");

        var pathResponse = new TextField();
        pathResponse.setEditable(true);
        pathResponse.setPromptText("Select Directory");
        pathResponse.setMinWidth(300.0);
        pathResponse.setMaxHeight(10.0);
        pathResponse.setMaxWidth(300.0);

        //noinspection ExtractMethodRecommender
        var directoryChooser = switch (App.getOS()) {
            case WINDOWS_11, WINDOWS_10 -> {
                var b = new Button();
                b.setOnAction(_ -> pathResponse.setText(
                        FileUtils.openDirectoryChooser(stage, "Choose directory", "C://").getAbsolutePath()));
                yield b;
            }
            case LINUX -> {
                var b = new Button();
                b.setOnAction(_ -> pathResponse.setText(
                        FileUtils.openDirectoryChooser(stage, "Choose directory", "/home").getAbsolutePath()));
                yield b;
            }
            case MAC -> {
                var b = new Button();
                b.setOnAction(_ -> pathResponse.setText(
                        FileUtils.openDirectoryChooser(stage, "Choose directory", "/Users").getAbsolutePath()));
                yield b;
            }
        };

        directoryChooser.setText("Open Explorer");

        var pathHbox = new HBox();
        pathHbox.getChildren().addAll(pathResponse, directoryChooser);
        pathHbox.setSpacing(10.0);

        var pathVbox = new VBox();
        pathVbox.getChildren().addAll(pathQuestion, pathHbox);

        var pathPage = new WizardPane();
        pathPage.setContent(pathVbox);

        // Page2
        var usernameQuestion = new Label("Enter Username:");

        var usernameResponse = new TextField();
        usernameResponse.setEditable(true);
        usernameResponse.setPromptText("Username...");
        usernameResponse.setMinWidth(300.0);
        usernameResponse.setMaxHeight(10.0);
        usernameResponse.setMaxWidth(300.0);

        var usernameVbox = new VBox();
        usernameVbox.getChildren().addAll(usernameQuestion, usernameResponse);

        var usernamePage = new WizardPane();
        usernamePage.setContent(usernameVbox);

        // Wizard settings
        wizard.setTitle("BerryBush Setup Wizard");
        wizard.setFlow(new Wizard.LinearFlow(usernamePage, pathPage));

        // Show wizard and wait, set userData
        wizard.showAndWait().ifPresent(result -> {
            if (result == ButtonType.FINISH) {
                UserDataHandler.setConfig(UserDataHandler.ConfigLocation.PATH, pathResponse.getText());
                UserDataHandler.setConfig(UserDataHandler.ConfigLocation.USERNAME, usernameResponse.getText());
            }
        });
    }
}
