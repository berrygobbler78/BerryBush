package com.berrygobbler78.flacplayer.controllers;

import com.berrygobbler78.flacplayer.App;
import com.berrygobbler78.flacplayer.configuration.PlaylistDataHandler;
import com.berrygobbler78.flacplayer.configuration.PlaylistDataHandler.Playlist;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class PopupWindowsController {
   @FXML
   private TextField playlistNameField;

   private Stage stage;
   private MainController controller;

   public void setValues(Stage stage, MainController controller) {
       this.stage = stage;
       this.controller = controller;
   }

   @FXML
   private void enterPlaylistName(){
       PlaylistDataHandler.createPlaylist(playlistNameField.getText(), null);
       stage.close();

       controller.refreshTreeView();
   }
}
