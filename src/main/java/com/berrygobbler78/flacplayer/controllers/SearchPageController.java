package com.berrygobbler78.flacplayer.controllers;

import com.berrygobbler78.flacplayer.SearchManager;
import com.berrygobbler78.flacplayer.util.handlers.RecordHandler;
import com.berrygobbler78.flacplayer.util.handlers.ResourceHandler;
import com.berrygobbler78.flacplayer.util.records.Album;
import com.berrygobbler78.flacplayer.util.records.Artist;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Comparator;
import java.util.List;

public class SearchPageController {
    private static final Logger logger = LogManager.getLogger();

    @FXML
    private TextField searchBar;
    @FXML
    private GridPane albums, artists;
    @FXML
    private ScrollPane mainScrollPane, artistScrollPane, albumScrollPane;

    private TabManager tabManager;

    private SearchManager searchManager;

    @FXML
    private void initialize() {
        EventHandler<ScrollEvent> scrollRedirector = event -> {
            if (event.getDeltaY() != 0 && mainScrollPane.getContent() != null) {
                mainScrollPane.getContent().fireEvent(event.copyFor(event.getSource(), mainScrollPane.getContent()));
                event.consume();
            }
        };

        artistScrollPane.addEventFilter(ScrollEvent.SCROLL, scrollRedirector);
        albumScrollPane.addEventFilter(ScrollEvent.SCROLL, scrollRedirector);

        updateAlbums();
        updateArtists();

        searchManager = new SearchManager(searchBar);
    }

    private void updateAlbums() {
        albums.getChildren().clear();
        try {
            List<Album> albumList = RecordHandler.getAlbumList();
            Node[] nodes = new Node[albumList.size()];

            int col = 0;
            int row = 0;

            for(int i = 0; i < nodes.length; i++){
                Album album = albumList.get(i);
                try {
                    FXMLLoader loader = new FXMLLoader();
                    loader.setLocation(ResourceHandler.getResourceURL("fxml/genericItem.fxml"));
                    nodes[i] = loader.load();
                    nodes[i].setOnMouseClicked(_ -> tabManager.newAlbumPage(album));

                    GenericItemController genericItemController = loader.getController();
                    genericItemController.setItemData(album);
                    genericItemController.bindHeightProperty(mainScrollPane.heightProperty());

                    int finalI = i;
                    int finalRow = row;
                    int finalCol = col;
                    Platform.runLater(() -> albums.add(nodes[finalI], finalCol, finalRow));

                    row++;
                    if(row > 1) {
                        row = 0;
                        col++;
                    }
                    logger.debug("Album added '{}'", album.title());
                } catch (Exception e) {
                    logger.error("Failed to load '{}' : {}", album.title(), e.getMessage());
                }
            }
        } catch (Exception e) {
            logger.error("Album list refresh failed : {}", e.getMessage());
        }
    }

    private void updateArtists() {
        artists.getChildren().clear();
        try {
            List<Artist> artistList = RecordHandler.getArtistList();
            artistList.sort(Comparator.comparing(Artist::name));

            Node[] nodes = new Node[artistList.size()];

            int col = 0;
            int row = 0;

            for(int i = 0; i < nodes.length; i++){
                Artist artist = artistList.get(i);

                try {
                    FXMLLoader loader = new FXMLLoader();
                    loader.setLocation(ResourceHandler.getResourceURL("fxml/genericItem.fxml"));
                    nodes[i] = loader.load();
                    nodes[i].setOnMouseClicked(_ -> tabManager.newAlbumPage(artist.albums().getFirst()));

                    GenericItemController genericItemController = loader.getController();
                    genericItemController.setItemData(artist);
                    genericItemController.bindHeightProperty(mainScrollPane.heightProperty());

                    int finalI = i;
                    int finalRow = row;
                    int finalCol = col;
                    Platform.runLater(() -> {
                        artists.add(nodes[finalI], finalCol, finalRow);
                    });

                    row++;

                    if(row > 1) {
                        row = 0;
                        col++;
                    }
                    logger.debug("Artist added '{}'", artist.name());
                } catch (Exception e) {
                    logger.error("Failed to load '{}' : {}", artist.name(), e.getMessage());
                }
            }
        } catch (Exception e) {
            logger.error("Artist list refresh failed : {}", e.getMessage());
        }
    }

    public void setTabManager(TabManager tabManager) {
        this.tabManager = tabManager;
    }
}
