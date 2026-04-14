package com.berrygobbler78.flacplayer.gui.controllers;

import com.berrygobbler78.flacplayer.App;
import com.berrygobbler78.flacplayer.util.ImageUtils;
import com.berrygobbler78.flacplayer.util.records.Song;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;

public class SongItemController {
    @FXML
    private BorderPane container;
    @FXML
    private Label titleLabel, artistLabel;
    @FXML
    private ImageView albumImageView, playButtonImageView;

    public void setup(Song song) {
        container.setStyle("-fx-background-radius: 10"); // FIXME: Radius not being added
        container.hoverProperty().addListener((_, _, newValue) ->
                Platform.runLater(() -> {
                    if (newValue) {
                        container.setStyle("-fx-background-color: -fx-secondary");
                        playButtonImageView.setStyle("-fx-opacity: 1;");
                        albumImageView.setStyle("-fx-opacity: 0.5;");
                    } else {
                        container.setStyle("-fx-background-color: -fx-background");
                        playButtonImageView.setStyle("-fx-opacity: 0;");
                        albumImageView.setStyle("-fx-opacity: 1;");
                    }
                }));

        playButtonImageView.setOnMouseClicked(_ -> {
            App.getMusicInterface().setParent(song.album());
            App.getMusicInterface().playSongNum(song.track());
        });

        titleLabel.setText(song.title());
        artistLabel.setText(song.album().artist().title());
        albumImageView.setImage(ImageUtils.pathToImage(song.album().artPath()).orElse(new Image(ImageUtils.getWarningURL())));
        albumImageView.setFitHeight(40);
        albumImageView.setFitWidth(40);
    }
}
