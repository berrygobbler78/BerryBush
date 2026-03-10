package com.berrygobbler78.flacplayer.controllers;

import com.berrygobbler78.flacplayer.*;
import com.berrygobbler78.flacplayer.configuration.PlaylistDataHandler;
import com.berrygobbler78.flacplayer.util.Constants;
import com.berrygobbler78.flacplayer.util.ImageUtils;
import com.berrygobbler78.flacplayer.util.MusicPlayer;
import com.berrygobbler78.flacplayer.util.handlers.RecordHandler;
import com.berrygobbler78.flacplayer.util.records.Album;
import com.berrygobbler78.flacplayer.util.records.Playlist;
import com.berrygobbler78.flacplayer.util.records.Song;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.ResourceBundle;

public class SongItemController implements Initializable {
    private static final Logger logger = LogManager.getLogger();

   @FXML
   private Label songNumberLabel, songTitleLabel, songArtistLabel;
   @FXML
   private ImageView playIV, songAlbumIV;
   @FXML
   private StackPane stackPane;
   @FXML
   private Menu playlistMenu;

   private Song song;

   private MainController mainController;
   private PreviewTabController previewTabController;

   private Constants.PARENT_TYPE parentType;
   private Album album;
   private Playlist playlist;

   private MusicPlayer musicPlayer;

   private final MenuItem empty = new MenuItem("(empty)");

   @Override
   public void initialize(URL location, ResourceBundle resources) {
       empty.setDisable(true);
       playlistMenu.getItems().add(empty);

   }

   public void setItemInfo(int songNumber, Song song) {
       logger.debug("Settings item info for '{}'", song.title());
       this.song = song;

       songNumberLabel.setText(String.valueOf(songNumber));
       songAlbumIV.setImage(ImageUtils.pathToImage(song.album().imagePath()));
       songTitleLabel.setText(song.title());
       switch(parentType) {
           case PLAYLIST -> songArtistLabel.setText(song.artist().name() + " // " + song.album().title());
           case ALBUM -> songArtistLabel.setText(song.artist().name());
       }

       playlistMenu.setOnShowing(_ -> {
           if(RecordHandler.getPlaylistList().isEmpty()) return;
           playlistMenu.getItems().clear();
           for(Playlist playlist : RecordHandler.getPlaylistList()) {
               CheckMenuItem playlistMenuItem = getCheckMenuItem(song.path(), playlist);
                   if(playlist.songs().contains(song)) playlistMenuItem.setSelected(true);
                   playlistMenuItem.selectedProperty().addListener((_, wasSelected, isSelected) -> {
                       if (isSelected) {
                           playlist.songs().add(song);
                           PlaylistDataHandler.save(playlist);
                       }
                       if(wasSelected) {
                           playlist.songs().remove(song);
                           PlaylistDataHandler.save(playlist);
                           previewTabController.refreshSongs();
                       }
                   });
               playlistMenu.getItems().add(playlistMenuItem);
           }
       });
   }

   void setAlbum(Album album) {
       this.album = album;
       parentType = Constants.PARENT_TYPE.ALBUM;
   }

    void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
        parentType = Constants.PARENT_TYPE.PLAYLIST;
    }

   private static CheckMenuItem getCheckMenuItem(String songPath, Playlist playlist) {
       CheckMenuItem playlistMenuItem = new CheckMenuItem(playlist.title());

       if(playlist.songs().contains(songPath)) {
           playlistMenuItem.setSelected(true);
       }

       // playlistMenuItem.setOnAction(_ -> {
       //     if(playlistMenuItem.isSelected() && !playlist.getSongs().contains(songPath)) {
       //         playlist.addSong(songPath);
       //     } else if(playlist.getSongs().contains(songPath)) {
       //         playlist.removeSong(songPath);
       //     }
       // });

       return playlistMenuItem;
   }

   public void setControllers(MainController mainController, PreviewTabController previewTabController) {
       this.mainController = mainController;
       this.previewTabController = previewTabController;

       musicPlayer = mainController.getMusicPlayer();
   }

   @FXML
   private void playSong() {
       logger.info("Play '{}' pressed", song.title());
       switch(parentType) {
           case PLAYLIST:
               musicPlayer.setPlaylist(playlist);
               break;
           case ALBUM:
               musicPlayer.setAlbum(song.album());
       }

       musicPlayer.setPreviewTabController(previewTabController);
       musicPlayer.playSongNum(Integer.parseInt(songNumberLabel.getText())-1);
   }

   @FXML
   private void addToQueue() {
       musicPlayer.addToUserQueue(song);
   }
}
