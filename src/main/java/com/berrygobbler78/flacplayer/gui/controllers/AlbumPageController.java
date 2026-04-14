package com.berrygobbler78.flacplayer.gui.controllers;

import com.berrygobbler78.flacplayer.App;
import com.berrygobbler78.flacplayer.util.ImageUtils;
import com.berrygobbler78.flacplayer.util.records.Album;
import com.berrygobbler78.flacplayer.util.records.Song;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class AlbumPageController {
    private static final Logger logger = LogManager.getLogger();

    @FXML
    private ImageView image;
    @FXML
    private Label title, artist;
    @FXML
    private VBox songList;

    public void setAlbum(Album album) {
        image.setImage(ImageUtils.pathToImage(album.artPath()).orElse(new Image(ImageUtils.getWarningURL())));
        title.setText(album.title());
        artist.setText(album.artist().title());
        songList.getChildren().clear();

        for (Song song : album.songs()) {
            try {
                var loader = new FXMLLoader();
                loader.setLocation(App.class.getResource("fxml/songItem.fxml"));

                BorderPane borderPane = loader.load();
                borderPane.setStyle("-fx-background-radius: 5");
                borderPane.setOnMouseEntered(_ -> borderPane.setStyle("-fx-background-color: -fx-secondary"));
                borderPane.setOnMouseExited(_ -> borderPane.setStyle("-fx-background-color: -fx-background"));
                borderPane.prefWidthProperty().bind(songList.widthProperty());

                songList.getChildren().add(borderPane);

                SongItemController songItemController = loader.getController();
                songItemController.setup(song);
            } catch (IOException e) {
                logger.error("Failed to add song {} | {}", song.title(), e.getMessage());
            }
        }
    }
}
