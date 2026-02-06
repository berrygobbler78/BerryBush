package com.berrygobbler78.flacplayer.controllers;

import com.berrygobbler78.flacplayer.*;
import com.berrygobbler78.flacplayer.userdata.Playlist;
import com.berrygobbler78.flacplayer.userdata.References;
import com.berrygobbler78.flacplayer.util.FileUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ResourceBundle;

public class SongItemController implements Initializable {
    @FXML
    private Label songNumberLabel, songTitleLabel, songArtistLabel;
    @FXML
    private ImageView playIV, songAlbumIV;
    @FXML
    private StackPane stackPane;
    @FXML
    private Menu playlistMenu;

    private String songPath;

    private MusicPlayer musicPlayer;
    private MainController controller;
    private PreviewTabController previewTabController;

    private References references = App.references;

    private Enums.PARENT_TYPE parentType;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.musicPlayer = App.musicPlayer;
    }

    public void setItemInfo(int songNumber, String songPath, Enums.PARENT_TYPE parentType) {
        this.songNumberLabel.setText(String.valueOf(songNumber));
        this.songTitleLabel.setText(FileUtils.getSongTitle(songPath));
        switch(parentType) {
            case PLAYLIST -> {
                String text =  FileUtils.getSongArtist(songPath) + " // " + FileUtils.getSongAlbum(songPath);
                songArtistLabel.setText(text);
            }
            case ALBUM -> songArtistLabel.setText(FileUtils.getSongArtist(songPath));
        }
        try {
            this.songAlbumIV.setImage(FileUtils.getCoverImage(songPath, Enums.FILE_TYPE.SONG));
        } catch (IOException e) {
            System.err.println("Error loading cover image: " + e);
        }
        this.songPath = songPath;
        this.parentType = parentType;

        for(Playlist playlist : App.references.getPlaylists()) {
            CheckMenuItem playlistMenuItem = new CheckMenuItem(playlist.getName());

            if(playlist.getSongList().contains(songPath)) {
                playlistMenuItem.setSelected(true);
            }

            playlistMenuItem.setOnAction(_ -> {
                if(playlistMenuItem.isSelected() && !playlist.getSongList().contains(songPath)) {
                    playlist.addSong(songPath);
                } else if(playlist.getSongList().contains(songPath)) {
                    playlist.removeSong(playlist.getSongList().indexOf(songPath));
                }
            });

            playlistMenu.getItems().add(playlistMenuItem);
        }
    }

    void setControllers(MainController controller, PreviewTabController previewTabController) {
        this.controller = controller;
        this.previewTabController = previewTabController;
    }

    @FXML
    private void playSong() {
        switch(parentType) {
            case PLAYLIST:
                musicPlayer.setParentTypePlaylist(previewTabController.getPlaylist());
                break;
            case ALBUM:
                musicPlayer.setParentTypeAlbum(Path.of(songPath).getParent().toString());
        }

        musicPlayer.setPreviewTabController(previewTabController);
        musicPlayer.playSongNum(Integer.parseInt(songNumberLabel.getText())-1);
    }

    @FXML
    private void addToQueue() {
        musicPlayer.addToUserQueue(songPath);
    }
}
