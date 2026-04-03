package com.berrygobbler78.flacplayer;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

public class EffectsEngine {
    // Grey (#888888)
    private static final ColorAdjust GREY_TINT = new ColorAdjust(0, 0, -0.43, 0);
    private static final ColorAdjust EMPTY_TINT = new ColorAdjust(0, 0, 0, 0);

    public static void setPressedEffect(ImageView imageView) {
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(100), imageView);

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
        ImageView imageView = (ImageView) tab.getGraphic();
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(250), imageView);

        tab.selectedProperty().addListener((_, _, newValue) -> {
            if(newValue) {
                scaleTransition.setToX(0.95);
                scaleTransition.setToY(0.95);
                scaleTransition.playFromStart();

                scaleTransition.setOnFinished(_ -> {
                    scaleTransition.setToX(1);
                    scaleTransition.setToY(1);
                    scaleTransition.playFromStart();
                });
            }
        });
    }

    public static void slideInContent(Node content) {
        // Start from the right (e.g., 100 pixels offset)
        content.setTranslateX(100);
        content.setOpacity(0);

        var tt = new TranslateTransition(Duration.millis(300), content);
        tt.setToX(0);

        var ft = new FadeTransition(Duration.millis(300), content);
        ft.setToValue(1);

        var pt = new ParallelTransition(tt, ft);
        pt.play();
    }
}
