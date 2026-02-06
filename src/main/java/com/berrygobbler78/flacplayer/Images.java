package com.berrygobbler78.flacplayer;

import javafx.scene.image.Image;

import java.util.Objects;

public class Images {
//    General Icons
    private static final Image warningImage =
        new Image(Objects.requireNonNull(Images.class.getResourceAsStream("/com/berrygobbler78/flacplayer/graphics/warning.png")));
    private static final Image cdImage =
            new Image(Objects.requireNonNull(Images.class.getResourceAsStream("/com/berrygobbler78/flacplayer/graphics/cd.png")));
    private static final Image songImage =
            new Image(Objects.requireNonNull(Images.class.getResourceAsStream("/com/berrygobbler78/flacplayer/graphics/song.png")));
    private static final Image userImage =
            new Image(Objects.requireNonNull(Images.class.getResourceAsStream("/com/berrygobbler78/flacplayer/graphics/user.png")));

//    Interactable
    private static final Image playImage =
        new Image(Objects.requireNonNull(Images.class.getResourceAsStream("/com/berrygobbler78/flacplayer/graphics/play.png")));
    private static final Image pauseImage =
            new Image(Objects.requireNonNull(Images.class.getResourceAsStream("/com/berrygobbler78/flacplayer/graphics/pause.png")));

    // Bottom bar
    private static final Image repeatUnselectedImage =
            new Image(Objects.requireNonNull(Images.class.getResourceAsStream("/com/berrygobbler78/flacplayer/graphics/repeat_unselected.png")));
    private static final Image repeatSelectedImage =
            new Image(Objects.requireNonNull(Images.class.getResourceAsStream("/com/berrygobbler78/flacplayer/graphics/repeat_selected.png")));
    private static final Image repeatOneSelectedImage =
            new Image(Objects.requireNonNull(Images.class.getResourceAsStream("/com/berrygobbler78/flacplayer/graphics/repeat_one_selected.png")));

    private static final Image shuffleUnselectedImage =
            new Image(Objects.requireNonNull(Images.class.getResourceAsStream("/com/berrygobbler78/flacplayer/graphics/shuffle_unselected.png")));
    private static final Image shuffleSelectedImage =
            new Image(Objects.requireNonNull(Images.class.getResourceAsStream("/com/berrygobbler78/flacplayer/graphics/shuffle_selected.png")));


    public enum IMAGE {
        WARNING(warningImage), CD(cdImage), SONG(songImage), USER(userImage),
        PLAY(playImage), PAUSE(pauseImage),
        REPEAT_UNSELECTED(repeatUnselectedImage), REPEAT_ALL(repeatSelectedImage),REPEAT_ONE(repeatOneSelectedImage),
        SHUFFLE_UNSELECTED(shuffleUnselectedImage), SHUFFLE_SELECTED(shuffleSelectedImage);

        private Image image;
        IMAGE(Image image) {
            this.image = image;
        };

        public Image get() {
            return image;
        }
    }
}