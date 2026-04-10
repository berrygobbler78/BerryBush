package com.berrygobbler78.flacplayer.gui.controllers;

import com.berrygobbler78.flacplayer.App;
import com.berrygobbler78.flacplayer.gui.managers.SearchManager;
import com.berrygobbler78.flacplayer.gui.managers.TabManager;
import com.berrygobbler78.flacplayer.util.records.RecordHandler;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.GridPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Callable;

public class SearchPageController {
    private static final Logger logger = LogManager.getLogger();

    @FXML
    private TextField searchBar;
    @FXML
    private GridPane albumContainer, artistContainer;
    @FXML
    private ScrollPane mainScrollPane, artistScrollPane, albumScrollPane;

    private TabManager tabManager;

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

        new SearchManager(searchBar);
    }

    private void updateAlbums() {
        albumContainer.getChildren().clear();

        Callable<Void> update = () -> {
            var albums = RecordHandler.getAlbumList();
            var nodes = new Node[albums.size()];

            int col = 0;
            int row = 0;

            for(int i = 0; i < nodes.length; i++){
                var album = albums.get(i);
                try {
                    var loader = new FXMLLoader();
                    loader.setLocation(App.class.getResource("fxml/genericItem.fxml"));
                    nodes[i] = loader.load();
                    nodes[i].setOnMouseClicked(e -> tabManager.newAlbumPage(album, !e.isControlDown()));

                    int finalRow = row;
                    int finalCol = col;
                    int finalI = i;

                    Platform.runLater(() -> {
                        GenericItemController genericItemController = loader.getController();
                        genericItemController.setItemData(album);
                        genericItemController.bindHeightProperty(mainScrollPane.heightProperty());

                        albumContainer.add(nodes[finalI], finalCol, finalRow);
                    });

                    row++;
                    if(row > 1) {
                        row = 0;
                        col++;
                    }
                    logger.debug("Album added '{}'", album.title());
                } catch (Exception e) {
                    logger.error("Failed to load album '{}' : {}", album.title(), e.getMessage());
                }
            }
            return null;
        };

        App.submitTask(update);
    }

    private void updateArtists() {
        artistContainer.getChildren().clear();

        var update = new Task<Void>() {
            @Override
            protected Void call() {
                var artists = RecordHandler.getArtistList();
                var nodes = new Node[artists.size()];

                int col = 0;
                int row = 0;

                for(int i = 0; i < nodes.length; i++){
                    var artist = artists.get(i);
                    try {
                        var loader = new FXMLLoader();
                        loader.setLocation(App.class.getResource("fxml/genericItem.fxml"));
                        nodes[i] = loader.load();
                        nodes[i].setOnMouseClicked(e -> tabManager.newAlbumPage(artist.albums().getFirst(), !e.isControlDown()));

                        int finalI = i;
                        int finalRow = row;
                        int finalCol = col;

                        Platform.runLater(() -> {
                            GenericItemController genericItemController = loader.getController();
                            genericItemController.setItemData(artist);
                            genericItemController.bindHeightProperty(mainScrollPane.heightProperty());

                            artistContainer.add(nodes[finalI], finalCol, finalRow);
                        });

                        row++;
                        if(row > 1) {
                            row = 0;
                            col++;
                        }
                        logger.debug("Artist added '{}'", artist.title());
                    } catch (Exception e) {
                        logger.error("Failed to load artist '{}' | {}", artist.title(), e.getMessage());
                    }
                }
                return null;
            }

            @Override
            protected void failed() {
                super.failed();
                logger.error("Artist list refresh failed");
            }
        };

        new Thread(update).start();
    }

    public void setTabManager(TabManager tabManager) {
        this.tabManager = tabManager;
    }
}
