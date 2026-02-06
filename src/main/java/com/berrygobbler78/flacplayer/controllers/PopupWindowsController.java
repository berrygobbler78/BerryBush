package com.berrygobbler78.flacplayer.controllers;

import com.berrygobbler78.flacplayer.App;
import com.berrygobbler78.flacplayer.userdata.Playlist;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.List;

public class PopupWindowsController {
    @FXML
    private TextField playlistNameField;
    @FXML
    private TreeView<String> playlistTreeView;
    private TreeItem<String> root = new TreeItem<>();

    private Stage stage;
    private MainController controller;
    private String songPath;
    private boolean add;

    private HashMap<TreeItem<String>, Playlist> playlistHashMap = new HashMap<>();

    public void setValues(Stage stage, MainController controller) {
        this.stage = stage;
        this.controller = controller;
    }
    public void setValues(Stage stage, String songPath, boolean add) {
        this.stage = stage;
        this.songPath = songPath;
        this.add = add;
    }

    @FXML
    private void selectPlaylist(){
        playlistHashMap.get(playlistTreeView.getSelectionModel().getSelectedItem()).addSong(songPath);
        App.saveReferences();
        stage.close();
    }

    // private void

    public void showPlaylists(List<Playlist> playlists) {
        playlistHashMap.clear();
        root.getChildren().clear();

        for(Playlist playlist : playlists){
            TreeItem<String> item = new TreeItem<>(playlist.getName());
            playlistHashMap.put(item, playlist);
            root.getChildren().add(item);
        }

        playlistTreeView.setRoot(root);
        playlistTreeView.setShowRoot(false);
    }

    @FXML
    private void enterPlaylistName(){
        App.references.addPlaylist(new Playlist(playlistNameField.getText()));
        App.savePlaylists();
        stage.close();

        controller.refreshTreeView();
    }
}
