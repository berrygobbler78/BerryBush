module com.berrygobbler.flacplayer {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires javafx.graphics;
    requires javafx.swing;

    requires org.controlsfx.controls;
    requires java.desktop;
    requires jflac.codec;
    requires jaudiotagger;
    requires java.xml.crypto;
    requires com.pixelduke.fxthemes;
    requires com.jfoenix;
    requires com.sun.jna.platform;
    requires com.sun.jna;
    requires java.logging;
    requires jdk.dynalink;
    requires JavaMediaTransportControls;
    requires com.electronwill.nightconfig.core;
    requires jdk.compiler;
    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.core;
    requires java.sql;


    opens com.berrygobbler78.flacplayer.configuration to com.electronwill.nightconfig.core;
    opens com.berrygobbler78.flacplayer to javafx.fxml;
    opens com.berrygobbler78.flacplayer.gui.controllers to javafx.fxml;
    exports com.berrygobbler78.flacplayer;
    exports com.berrygobbler78.flacplayer.gui.controllers to javafx.fxml;
    exports com.berrygobbler78.flacplayer.util;
    opens com.berrygobbler78.flacplayer.util to javafx.fxml;
    exports com.berrygobbler78.flacplayer.util.records;
    exports com.berrygobbler78.flacplayer.music;
    opens com.berrygobbler78.flacplayer.music to javafx.fxml;
    exports com.berrygobbler78.flacplayer.gui.managers;
    opens com.berrygobbler78.flacplayer.gui.managers to javafx.fxml;
    exports com.berrygobbler78.flacplayer.gui;
    opens com.berrygobbler78.flacplayer.gui to javafx.fxml;
    opens com.berrygobbler78.flacplayer.util.records to javafx.fxml;
}