package com.berrygobbler78.flacplayer.gui.controllers;

import com.berrygobbler78.flacplayer.App;
import com.berrygobbler78.flacplayer.util.ImageUtils;
import com.berrygobbler78.flacplayer.util.records.Album;
import com.berrygobbler78.flacplayer.util.records.Artist;
import com.berrygobbler78.flacplayer.util.records.Song;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class GenericItemController {
    @FXML
    private VBox mainVBox;
    @FXML
    private ImageView itemImage;
    @FXML
    private Label title, subtitle;

    @FXML
    private void initialize() {
        mainVBox.getStylesheets().add(String.valueOf(App.class.getResource("css/generic-item.css")));
        itemImage.setPreserveRatio(true);
    }

    public void bindHeightProperty(ReadOnlyDoubleProperty property) {
        itemImage.fitHeightProperty().bind(property.divide(4.5));
        itemImage.fitWidthProperty().bind(itemImage.fitHeightProperty());
        mainVBox.maxWidthProperty().bind(itemImage.fitWidthProperty().add(20));
    }

    public void setItemData(Artist artist) {
        title.setText(artist.title());
        subtitle.setText(artist.albums().size() + " albums");
        itemImage.setImage(
                (ImageUtils.pathToImage(artist.artPath(), true).isEmpty()) ?
                        ImageUtils.pathToImage(artist.albums().getFirst().artPath(), false).orElse(null) :
                        ImageUtils.pathToImage(artist.artPath(), false).orElse(null));
        itemImage.setFitHeight(200);
        itemImage.setFitWidth(200);

    }

    public void setItemData(Album album) {
        title.setText(album.title());
        subtitle.setText(album.artist().title());
        itemImage.setImage(ImageUtils.pathToImage(album.artPath(), false).orElse(null));
        itemImage.setFitHeight(200);
        itemImage.setFitWidth(200);
    }

    public void setItemData(Song song) {
        title.setText(song.title());
        subtitle.setText(song.album().artist().title());
        itemImage.setImage(ImageUtils.pathToImage(song.album().artPath(), false).orElse(null));
        itemImage.setFitHeight(200);
        itemImage.setFitWidth(200);
    }
}
