package com.berrygobbler78.flacplayer.controllers;

import com.berrygobbler78.flacplayer.util.handlers.ResourceHandler;
import com.berrygobbler78.flacplayer.util.records.Album;
import com.berrygobbler78.flacplayer.util.records.Song;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AlbumPageController {
    private static final Logger logger = LogManager.getLogger();

    @FXML
    private ImageView image;
    @FXML
    private Label title, artist;
    @FXML
    private VBox songList;

    @FXML
    public void initialize() {

    }

    public void bindHeightProperty(ReadOnlyDoubleProperty property) {
    }

    public void setAlbum(Album album) {
        image.setImage(new ImageView(String.valueOf(ResourceHandler.getResourceURL(album.artPath()))).getImage());
        title.setText(album.title());
        artist.setText(album.artist().name());
        songList.getChildren().clear();

        for (Song song : album.songs()) {
            try {
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(ResourceHandler.getResourceURL("fxml/songItem.fxml"));
                BorderPane node = loader.load();
                node.setStyle("-fx-background-radius: 5");
                node.setOnMouseEntered(_ -> node.setStyle("-fx-background-color: -fx-secondary"));
                node.setOnMouseExited(_ -> node.setStyle("-fx-background-color: -fx-background"));
                node.prefWidthProperty().bind(songList.widthProperty());

                songList.getChildren().add(node);

                SongItemController songItemController = loader.getController();
                songItemController.setup(song);
            } catch (Exception e) {
                logger.error("Failed to add song {} : {}", song.title(), e.getMessage());
            }
        }
    }
}
