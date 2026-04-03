package com.berrygobbler78.flacplayer.controllers;

import com.berrygobbler78.flacplayer.EffectsEngine;
import com.berrygobbler78.flacplayer.util.handlers.ResourceHandler;
import com.berrygobbler78.flacplayer.util.records.Album;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
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

public class TabManager {
    private static final Logger logger = LogManager.getLogger();

    private LandingController landingController;
    private SearchPageController searchPageController;

    private final TabPane tabPane;

    // Default pages
    private Tab search, library, home, cdTools;

    public TabManager(LandingController landingController, StackPane contentContainer, TabPane tabPane, Tab home, Tab search, Tab library, Tab cdTools) {
        this.landingController = landingController;
        this.tabPane = tabPane;

        this.home = home;
        this.search = search;
        this.library = library;
        this.cdTools = cdTools;

        for(Tab t : tabPane.getTabs()) {
            EffectsEngine.setPressedEffect(t);
        }

        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(ResourceHandler.getResourceURL("fxml/searchPage.fxml"));
            Node node = loader.load();

            AnchorPane.setTopAnchor(node, 0.0);
            AnchorPane.setBottomAnchor(node, 0.0);
            AnchorPane.setLeftAnchor(node, 0.0);
            AnchorPane.setRightAnchor(node, 0.0);

            search.setContent(node);

            searchPageController = loader.getController();
            searchPageController.setTabManager(this);
        } catch (Exception e) {
            logger.error("Failed to load search page: {}", e.getMessage());
        }

        tabPane.getSelectionModel().selectedIndexProperty().addListener((obs, oldIdx, newIdx) -> {
            Tab newTab = tabPane.getTabs().get(newIdx.intValue());
            Node newContent = newTab.getContent();

            if (newContent != null) {
                double startY = (newIdx.intValue() > oldIdx.intValue()) ? 200 : -200;
                newContent.setTranslateY(startY);

                TranslateTransition tt = new TranslateTransition(Duration.millis(250), newContent);
                tt.setToY(0);
                tt.play();
            }
        });
    }

    public void newAlbumPage(Album album) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(ResourceHandler.getResourceURL("fxml/albumPage.fxml"));
            Node node = loader.load();

            AnchorPane.setTopAnchor(node, 0.0);
            AnchorPane.setBottomAnchor(node, 0.0);
            AnchorPane.setLeftAnchor(node, 0.0);
            AnchorPane.setRightAnchor(node, 0.0);

            Tab albumTab = new Tab();
            albumTab.setContent(node);
            albumTab.setGraphic(new ImageView(new Image(
                    String.valueOf(ResourceHandler.getResourceURL(album.artPath())),
                    20, 20,
                    false, false)));

            AlbumPageController albumPageController = loader.getController();
            albumPageController.setAlbum(album);

            tabPane.getTabs().add(albumTab);
            tabPane.getSelectionModel().select(albumTab);
        } catch (Exception e) {
            logger.error("Failed to load album page: {}", e.getMessage());
        }
    }
}
