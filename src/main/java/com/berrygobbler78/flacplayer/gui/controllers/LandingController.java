package com.berrygobbler78.flacplayer.gui.controllers;

import com.berrygobbler78.flacplayer.App;
import com.berrygobbler78.flacplayer.gui.EffectsEngine;
import com.berrygobbler78.flacplayer.gui.managers.TabManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LandingController {
    private static final Logger logger = LogManager.getLogger();

    @FXML
    private ImageView shuffleImageView, repeatImageView, previousImageView, nextImageView, playPauseImageView;
    @FXML
    private TabPane navigationBar;
    @FXML
    private Tab searchTab;

    @FXML
    private ImageView currentArt;
    @FXML
    private Label currentTitle, currentArtist;
    @FXML
    private StackPane contentContainer;

    @FXML
    private void initialize() {
        new TabManager(this, contentContainer, navigationBar, null, searchTab, null, null);
        setupBottomBar();
    }

    private void setupBottomBar() {
        EffectsEngine.setPressedEffect(shuffleImageView);
        shuffleImageView.setOnMousePressed(_ -> App.getMusicInterface().toggleShuffleStatus());

        EffectsEngine.setPressedEffect(repeatImageView);
        repeatImageView.setOnMousePressed(_ -> App.getMusicInterface().cycleRepeatStatus());

        EffectsEngine.setPressedEffect(previousImageView);
        previousImageView.setOnMousePressed(_ -> App.getMusicInterface().previous());

        EffectsEngine.setPressedEffect(nextImageView);
        nextImageView.setOnMousePressed(_ -> App.getMusicInterface().next());

        EffectsEngine.setPressedEffect(playPauseImageView);
        playPauseImageView.setOnMousePressed(_ -> setPaused(App.getMusicInterface().playPause()));
    }

    public void setPaused(boolean paused) {
        playPauseImageView.setImage(new Image(String.valueOf(paused ? App.class.getResource("graphics/playback/play.png") : App.class.getResource("graphics/playback/pause.png"))));
    }

    public void updateBottomBar(Image image, String title, String artist) {
        logger.debug("Bottom bar updated to '{}' & '{}'", title, artist);
        currentArt.setImage(image);
        currentTitle.setText(title);
        currentArtist.setText(artist);
    }
}