package com.berrygobbler78.flacplayer.controllers;

import com.berrygobbler78.flacplayer.App;
import com.berrygobbler78.flacplayer.configuration.UserDataHandler;
import com.berrygobbler78.flacplayer.util.FileUtils;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.controlsfx.dialog.Wizard;
import org.controlsfx.dialog.WizardPane;

public class SetupWizardController {
    public SetupWizardController(Stage stage) {
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
        switch (App.getOS()) {
            case WINDOWS_11 ->
                    directoryChooserButton.setOnAction(_ ->
                            pathTextField.setText(FileUtils.openDirectoryChooser(stage, "Choose directory", "C://").getAbsolutePath()));
            case LINUX ->
                    directoryChooserButton.setOnAction(_ ->
                            pathTextField.setText(FileUtils.openDirectoryChooser(stage, "Choose directory", "/home").getAbsolutePath()));

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
}
