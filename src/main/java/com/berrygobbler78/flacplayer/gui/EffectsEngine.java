package com.berrygobbler78.flacplayer.gui;

import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.scene.control.Tab;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

public class EffectsEngine {
    // Grey (#888888)
    private static final ColorAdjust GREY_TINT = new ColorAdjust(0, 0, -0.43, 0);
    private static final ColorAdjust EMPTY_TINT = new ColorAdjust(0, 0, 0, 0);

    public static void setPressedEffect(ImageView imageView) {
        var scaleTransition = new ScaleTransition(Duration.millis(100), imageView);

        imageView.setOnMousePressed(_ -> {
            imageView.setEffect(GREY_TINT);
            scaleTransition.setToX(0.95);
            scaleTransition.setToY(0.95);
            scaleTransition.playFromStart();
        });

        imageView.setOnMouseReleased(_ -> {
            imageView.setEffect(EMPTY_TINT);
            scaleTransition.setToX(1);
            scaleTransition.setToY(1);
            scaleTransition.playFromStart();
        });
    }

    public static void setPressedEffect(Tab tab) {
        var imageView = (ImageView) tab.getGraphic();
        var scaleTransition = new ScaleTransition(Duration.millis(250), imageView);

        Platform.runLater(() -> tab.selectedProperty().addListener((_, _, newValue) -> {
            if(newValue) {
                scaleTransition.setToX(0.95);
                scaleTransition.setToY(0.95);
                scaleTransition.playFromStart();
                scaleTransition.setOnFinished(_ -> {

                scaleTransition.setToX(1);
                scaleTransition.setToY(1);
                scaleTransition.playFromStart();
            });
        }}));
    }
}
