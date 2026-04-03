package com.berrygobbler78.flacplayer.controllers;

import com.berrygobbler78.flacplayer.util.handlers.ResourceHandler;
import com.berrygobbler78.flacplayer.util.records.Album;
import com.berrygobbler78.flacplayer.util.records.Artist;
import com.berrygobbler78.flacplayer.util.records.Song;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
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
        mainVBox.getStylesheets().add(ResourceHandler.getResourceURL("css/generic-item.css").toExternalForm());
        itemImage.setPreserveRatio(true);
    }

    public void bindHeightProperty(ReadOnlyDoubleProperty property) {
        itemImage.fitHeightProperty().bind(property.divide(4.5));
        itemImage.fitWidthProperty().bind(itemImage.fitHeightProperty());
        mainVBox.maxWidthProperty().bind(itemImage.fitWidthProperty().add(20));
    }

    public ImageView getImageView() {
        return itemImage;
    }

    public void setItemData(Artist artist) {
        title.setText(artist.name());
        subtitle.setText(artist.albums().size() + " albums");
        itemImage.setImage(new Image(
                String.valueOf(
                        (ResourceHandler.getResourceURL(artist.artPath()) == null) ?
                                ResourceHandler.getResourceURL(artist.albums().getFirst().artPath()) :
                                ResourceHandler.getResourceURL(artist.artPath())),
                200, 200,
                false, false));
    }

    public void setItemData(Album album) {
        title.setText(album.title());
        subtitle.setText(album.artist().name());
        itemImage.setImage(new Image(
                String.valueOf(ResourceHandler.getResourceURL(album.artPath())),
                200, 200,
                false, false));
    }

    public void setItemData(Song song) {
        title.setText(song.title());
        subtitle.setText(song.artist().name());
        itemImage.setImage(new Image(
                String.valueOf(ResourceHandler.getResourceURL(song.album().artPath())),
                200, 200,
                false, false));
    }
}
