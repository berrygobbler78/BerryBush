module com.berrygobbler78.flacplayer {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires javafx.graphics;

    requires org.controlsfx.controls;
    requires java.desktop;
    requires jflac.codec;
    requires jaudiotagger;
    requires java.xml.crypto;
    requires com.pixelduke.fxthemes;
    requires com.jfoenix;

    opens com.berrygobbler78.flacplayer to javafx.fxml;
    exports com.berrygobbler78.flacplayer;
}