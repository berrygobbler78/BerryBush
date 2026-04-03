package com.berrygobbler78.flacplayer.controllers;

import com.berrygobbler78.flacplayer.App;
import com.berrygobbler78.flacplayer.util.handlers.ResourceHandler;
import com.berrygobbler78.flacplayer.util.records.Song;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class SongItemController {
    @FXML
    private Label titleLabel, artistLabel;
    @FXML
    private ImageView albumImageView, playButtonImageView;

    private Song song;

    public void setup(Song song) {
        this.song = song;

        playButtonImageView.hoverProperty().addListener((_, _, newValue) -> {
            if (newValue) {
                playButtonImageView.setStyle("-fx-opacity: 1;");
                albumImageView.setStyle("-fx-opacity: 0.5;");
            } else {
                playButtonImageView.setStyle("-fx-opacity: 0;");
                albumImageView.setStyle("-fx-opacity: 1;");
            }
        });

        playButtonImageView.setOnMouseClicked(_ -> {
            App.getMusicInterface().setParent(song.album());
            App.getMusicInterface().playSongNum(song.track());
        });

        titleLabel.setText(song.title());
        artistLabel.setText(song.artist().name());
        albumImageView.setImage(new Image(String.valueOf(ResourceHandler.getResourceURL(song.album().artPath())), 40, 40, false, false));
    }
}
