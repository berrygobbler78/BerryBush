package com.berrygobbler78.flacplayer.gui.managers;

import com.berrygobbler78.flacplayer.App;
import com.berrygobbler78.flacplayer.gui.EffectsEngine;
import com.berrygobbler78.flacplayer.gui.controllers.AlbumPageController;
import com.berrygobbler78.flacplayer.gui.controllers.LandingController;
import com.berrygobbler78.flacplayer.gui.controllers.SearchPageController;
import com.berrygobbler78.flacplayer.util.ImageUtils;
import com.berrygobbler78.flacplayer.util.records.Album;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;

public class TabManager {
    private static final Logger logger = LogManager.getLogger();

    private final TabPane tabPane;

    private final HashMap<Album, Tab> openAlbums = new HashMap<>();
    private final HashMap<Album, Tab> openArtists = new HashMap<>();


    public TabManager(LandingController landingController, StackPane contentContainer, TabPane tabPane, Tab home, Tab search, Tab library, Tab cdTools) {
        this.tabPane = tabPane;

        for(Tab t : tabPane.getTabs()) {
            EffectsEngine.setPressedEffect(t);
        }

        try {
            var loader = new FXMLLoader();
            loader.setLocation(App.class.getResource("fxml/searchPage.fxml"));
            Node node = loader.load();

            AnchorPane.setTopAnchor(node, 0.0);
            AnchorPane.setBottomAnchor(node, 0.0);
            AnchorPane.setLeftAnchor(node, 0.0);
            AnchorPane.setRightAnchor(node, 0.0);

            search.setContent(node);

            SearchPageController searchPageController = loader.getController();
            searchPageController.setTabManager(this);
        } catch (IOException e) {
            logger.error("Failed to load search page | {}", e.getMessage());
        }

        tabPane.getSelectionModel().selectedIndexProperty().addListener((_, oldIdx, newIdx) ->
                Platform.runLater(() -> {
                    var newTab = tabPane.getTabs().get(newIdx.intValue());
                    Node newContent = newTab.getContent();

                    if (newContent != null) {
                        double startY = (newIdx.intValue() > oldIdx.intValue()) ? 200 : -200;
                        newContent.setTranslateY(startY);

                        TranslateTransition tt = new TranslateTransition(Duration.millis(250), newContent);
                        tt.setToY(0);
                        tt.play();
                    }
                }));
    }

    public void newAlbumPage(Album album, boolean switchTo) {
        if(openAlbums.containsKey(album)) {
            if(switchTo)tabPane.getSelectionModel().select(openAlbums.get(album));
            return;
        }

        App.submitTask(() -> {
            var loader = new FXMLLoader();
            loader.setLocation(App.class.getResource("fxml/albumPage.fxml"));
            try {
                Node node = loader.load();
                AnchorPane.setTopAnchor(node, 0.0);
                AnchorPane.setBottomAnchor(node, 0.0);
                AnchorPane.setLeftAnchor(node, 0.0);
                AnchorPane.setRightAnchor(node, 0.0);

                var albumTab = new Tab();
                albumTab.setContent(node);
                albumTab.setClosable(true);
                albumTab.setOnClosed(_ -> openAlbums.remove(album));

                var iv = new ImageView(ImageUtils.pathToImage(album.artPath(), false).orElse(null));
                iv.setFitWidth(20);
                iv.setFitHeight(20);
                albumTab.setGraphic(iv);

                AlbumPageController albumPageController = loader.getController();
                albumPageController.setAlbum(album);

                Platform.runLater(() -> {
                    tabPane.getTabs().add(albumTab);
                    if(switchTo) tabPane.getSelectionModel().select(albumTab);
                });

                openAlbums.put(album, albumTab);
            } catch (IOException e) {
                logger.error("Failed to load album page | {}", e.getMessage());
            }
        });
    }
}
