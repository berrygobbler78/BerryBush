package com.berrygobbler78.flacplayer.controllers;

import com.berrygobbler78.flacplayer.configuration.PlaylistDataHandler;
import com.berrygobbler78.flacplayer.configuration.UserDataHandler;
import com.berrygobbler78.flacplayer.util.Constants;

import com.berrygobbler78.flacplayer.util.ImageUtils;
import com.berrygobbler78.flacplayer.util.MusicPlayer;
import com.berrygobbler78.flacplayer.util.handlers.ResourceHandler;
import com.berrygobbler78.flacplayer.util.records.Album;
import com.berrygobbler78.flacplayer.util.records.Playlist;
import com.berrygobbler78.flacplayer.util.records.Song;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.nio.file.Path;
import java.util.*;

public class PreviewTabController implements Initializable {
    private static final Logger logger = LogManager.getLogger();

   @FXML
   private ImageView imageView, playPauseImageView;
   @FXML
   private Label titleLabel, artistLabel;
   @FXML
   private VBox songItemVBox, vbox;
   @FXML
   private MenuButton optionsMenuButton;

   private Constants.PARENT_TYPE type;

   private Album album;
   private Playlist playlist;

   private MainController controller;
   private MusicPlayer musicPlayer;

   @Override
   public void initialize(URL location, ResourceBundle resources) {
   }

   @FXML
   private void playPreview() {
       if(type == Constants.PARENT_TYPE.ALBUM) {
           musicPlayer.setAlbum(album);
           musicPlayer.setPreviewTabController(this);
           musicPlayer.playFirstSong();
       } else if(type == Constants.PARENT_TYPE.PLAYLIST) {
           musicPlayer.setPlaylist(playlist);
           musicPlayer.setPreviewTabController(this);
           musicPlayer.playFirstSong();
       }
   }

   @FXML
   private void addToQueue() {
       // musicPlayer.addToUserQueue(parentFile.getAbsolutePath());
   }

   public void setAlbumValues(Album album) {
       logger.info("Set tab to album '{}'", album.title());
       type = Constants.PARENT_TYPE.ALBUM;
       this.album = album;

       imageView.setImage(ImageUtils.pathToImage(album.imagePath()));
       titleLabel.setText(album.title());
       artistLabel.setText(album.artist().name());
   }

   public void setPlaylistValues(Playlist playlist) {
       logger.info("Set tab to playlist '{}'", playlist.title());
       this.playlist = playlist;

       type = Constants.PARENT_TYPE.PLAYLIST;

       // imageView.setImage(FileUtils.getCoverImage(playlist.get(), FileUtils.FILE_TYPE.PLAYLIST));
       titleLabel.setText(playlist.title());
       artistLabel.setText(UserDataHandler.getUsername());

       MenuItem deletePlaylistItem = new MenuItem("Delete Playlist");
       deletePlaylistItem.setOnAction(_ -> {
           PlaylistDataHandler.removePlaylist(playlist);
           controller.getTabManager().removeTab(this);
           controller.refreshTreeView();
       });

       optionsMenuButton.getItems().addAll(deletePlaylistItem);
   }

   public void setPaused(boolean paused) {
       if(paused) {
           playPauseImageView.setImage(Constants.IMAGES.PLAY.get());
       } else  {
           playPauseImageView.setImage(Constants.IMAGES.PAUSE.get());
       }
   }

   public void refreshSongs() {
       songItemVBox.getChildren().clear();

       if(type == Constants.PARENT_TYPE.ALBUM) {
           logger.debug("Refreshing songs for '{}'", album.title());

           try {
               List<Song> songs = album.songs();
               songs.sort((o1, o2) -> o2.title().compareTo(o1.title()));

               logger.debug("Creating nodes of length '{}'", songs.size());
               Node[] nodes = new Node[songs.size()];

               for(int i = 0; i < nodes.length; i++){
                   FXMLLoader loader = new FXMLLoader();
                   loader.setLocation(ResourceHandler.getResourceURL("fxml/songItem.fxml"));
                   nodes[i] = loader.load();
                   Song song = songs.get(i);

                   SongItemController songItemController = loader.getController();
                       songItemController.setAlbum(song.album());
                       songItemController.setItemInfo(
                               i + 1,
                               song
                       );
                       songItemController.setControllers(controller, PreviewTabController.this);

                   int finalI = i;
                   Platform.runLater(() -> songItemVBox.getChildren().add(nodes[finalI]));
                   logger.debug("Song added '{}'", song.title());
               }
           } catch (Exception e) {
               logger.error("Song list refresh failed : {}", e.getMessage());
           }
       } else if(type == Constants.PARENT_TYPE.PLAYLIST) {
           logger.info("Refreshing songs for '{}'", playlist.title());
           try {
               List<Song> songs = playlist.songs();
               int nodesLength = playlist.songs().size();

               Node[] nodes = new Node[nodesLength];

               for(int i = 0; i < nodes.length; i++){
                   FXMLLoader loader = new FXMLLoader();
                   loader.setLocation(Path.of("src/main/resources/com/berrygobbler78/flacplayer/fxml/songItem.fxml").toUri().toURL());
                   nodes[i] = loader.load();

                   Song song = songs.get(i);

                   SongItemController songItemController = loader.getController();
                       songItemController.setPlaylist(playlist);
                       songItemController.setItemInfo(
                               i + 1,
                               song
                       );
                       songItemController.setControllers(controller, PreviewTabController.this);

                   int finalI = i;
                   Platform.runLater(() -> songItemVBox.getChildren().add(nodes[finalI]));
                   logger.debug("Song added '{}'", song.title());
               }
           } catch (Exception e) {
               logger.error("Song list refresh failed : {}", e.getMessage());
           }

       }
   }

   public void setMainController(MainController controller) {
       this.controller = controller;
       musicPlayer = controller.getMusicPlayer();
   }

   public Constants.PARENT_TYPE getType() {
       return type;
   }

   public Playlist getPlaylist() {
       return playlist;
   }

}
