package com.berrygobbler78.flacplayer.controllers;

import com.berrygobbler78.flacplayer.*;
import com.berrygobbler78.flacplayer.Images.IMAGE;
import com.berrygobbler78.flacplayer.userdata.Playlist;
import com.berrygobbler78.flacplayer.util.FileUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;

public class PreviewTabController implements Initializable {

    @FXML
    private ImageView imageView, playPauseImageView;
    @FXML
    private Label titleLabel, artistLabel;
    @FXML
    private VBox songItemVBox, vbox;

    private Enums.PARENT_TYPE type;
    private File file;
    private Playlist playlist;
    private MainController controller;
    private MusicPlayer musicPlayer;


    private double font = 36;
    private boolean doAction = true;
    private final int error = 5;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.musicPlayer = App.musicPlayer;
    }

    @FXML
    private void playPreview() {
        if(type == Enums.PARENT_TYPE.ALBUM) {
            musicPlayer.setParentTypeAlbum(file.getPath());
            musicPlayer.playFirstSong();
            musicPlayer.setPreviewTabController(this);
        } else if(type == Enums.PARENT_TYPE.PLAYLIST) {
            musicPlayer.setParentTypePlaylist(playlist);
            musicPlayer.playFirstSong();
            musicPlayer.setPreviewTabController(this);
        }
    }

    @FXML
    private void addToQueue() {
        musicPlayer.addToUserQueue(file.getAbsolutePath());
    }

    public void setAlbumValues(File file) {
        this.file = file;
        type = Enums.PARENT_TYPE.ALBUM;
        try {
            imageView.setImage(FileUtils.getCoverImage(file.getAbsolutePath(), Enums.FILE_TYPE.ALBUM));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        titleLabel.setText(file.getName());
        artistLabel.setText(file.getParentFile().getName());
    }

    public void setPlaylistValues(Playlist playlist) {
        this.playlist = playlist;
        type = Enums.PARENT_TYPE.PLAYLIST;
        try {
            imageView.setImage(FileUtils.getCoverImage(playlist.getPath(), Enums.FILE_TYPE.PLAYLIST));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        titleLabel.setText(playlist.getName());
        artistLabel.setText(App.references.getUserName());
    }

    public void setPlayPauseImageViewPaused(boolean paused) {
        if(paused) {
            playPauseImageView.setImage(IMAGE.PLAY.get());
        } else  {
            playPauseImageView.setImage(IMAGE.PAUSE.get());
        }
    }

    public void refreshSongItemVBox() {
        songItemVBox.getChildren().clear();

        if(type == Enums.PARENT_TYPE.ALBUM) {
            try {

                ArrayList<File> songListArray =
                        new ArrayList<>(Arrays.asList(Objects.requireNonNull(file.listFiles(FileUtils.getFileFilter(Enums.FILTER_TYPE.FLAC)))));

                Node[] nodes = new Node[songListArray.size()];

                for(int i = 0; i < nodes.length; i++){
                    FXMLLoader loader = new FXMLLoader();
                    loader.setLocation(new File("src/main/resources/com/berrygobbler78/flacplayer/fxml/songItem.fxml").toURI().toURL());
                    nodes[i] = loader.load();

                    SongItemController songItemController = loader.getController();

                    File song = songListArray.get(i);

                    songItemController.setItemInfo(
                            i + 1,
                            song.getAbsolutePath(),
                            Enums.PARENT_TYPE.ALBUM
                    );

                    songItemController.setControllers(controller, this);

                    songItemVBox.getChildren().add(nodes[i]);
                }
            } catch (IOException e) {
                System.err.println("Song list failed with exception: " + e);
            }
        } else if(type == Enums.PARENT_TYPE.PLAYLIST) {
            try {
                int nodesLength = 0;

                for(String song : playlist.getSongList()) {
                    nodesLength ++;
                }

                Node[] nodes = new Node[nodesLength];

                for(int i = 0; i < nodes.length; i++){
                    FXMLLoader loader = new FXMLLoader();
                    loader.setLocation(Path.of("src/main/resources/com/berrygobbler78/flacplayer/fxml/songItem.fxml").toUri().toURL());
                    nodes[i] = loader.load();

                    SongItemController songItemController = loader.getController();

                    File song = new File(playlist.getSongList().get(i));

                    songItemController.setItemInfo(
                            i + 1,
                            song.getAbsolutePath(),
                            Enums.PARENT_TYPE.PLAYLIST
                    );

                    songItemController.setControllers(controller, this);

                    songItemVBox.getChildren().add(nodes[i]);
                }
            } catch (IOException e) {
                System.err.println("Song list failed with exception: " + e);
            }

        }
    }

    public void setMainController(MainController controller) {
        this.controller = controller;
    }

    public Enums.PARENT_TYPE getType() {
        return type;
    }

    public Playlist getPlaylist() {
        return playlist;
    }

}
