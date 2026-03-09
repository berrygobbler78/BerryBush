package com.berrygobbler78.flacplayer.controllers;

import java.io.File;
import java.net.URL;
import java.util.*;

import com.berrygobbler78.flacplayer.App;
import com.berrygobbler78.flacplayer.configuration.UserDataHandler;
import com.berrygobbler78.flacplayer.util.ImageUtils;
import com.berrygobbler78.flacplayer.util.MusicPlayer;
import com.berrygobbler78.flacplayer.util.Constants.*;

import com.berrygobbler78.flacplayer.util.FileUtils;
import com.jfoenix.controls.JFXSlider;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MainController implements  Initializable {
   private static final Logger logger = LogManager.getLogger();

   @FXML
   private RadioMenuItem alphabeticalRB, alphabeticalReverseRB;
   @FXML
   private AnchorPane anchorPane;
   @FXML
   private Label songLabel;
   @FXML
   private Label artistLabel;
   @FXML
   private Button playPauseButton, nextButton, previousButton, repeatButton, shuffleButton;
   @FXML
   private Slider volumeSlider;
   @FXML
   private JFXSlider songProgressSlider;
   @FXML
   public  BorderPane topBorderPane;
   @FXML
   private TreeView<String> treeView;
   @FXML
   private ImageView currentPlayPauseImageView, repeatImageView, shuffleImageView, previousImageView, nextImageView;
   @FXML
   private ImageView currentAlbumImageView;
   @FXML
   private TabPane previewTabPane;
   @FXML
   private Label totTrackTime, currentTrackTime;
   @FXML
   private TextField searchBar;

   private final Stage primaryStage = App.getPrimaryStage();

   boolean paused = true;

   private MusicPlayer musicPlayer;

   private TreeManager treeManager;
   private TabManager tabManager;

   @Override
   public void initialize(URL location, ResourceBundle resources) {
       logger.debug("Initializing {}", MainController.class.getName());
       treeManager = new TreeManager(this, treeView);
       tabManager = new TabManager(this, previewTabPane);

       musicPlayer = new MusicPlayer(this);

       resetBottomBar();

      // Hides JFX thumb
       songProgressSlider.setValueFactory(_ -> Bindings.createStringBinding(() -> "", songProgressSlider.valueProperty()));
       songProgressSlider.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::updateSongPos);

       alphabeticalRB.setOnAction(_ -> treeManager.sort(TreeManager.SortingType.ALPHABETICAL));
       alphabeticalReverseRB.setOnAction(_ -> treeManager.sort(TreeManager.SortingType.REVERSE_ALPHABETICAL));


       volumeSlider.valueProperty().addListener((ObservableValue<? extends Number> _, Number _, Number _) -> {
          if (musicPlayer.getMusicPlayerState() != MusicPlayer.MUSIC_PLAYER_STATUS.STOPPED) {
              musicPlayer.setVolume((float) (volumeSlider.getValue() / 150.0));
          }
      });
   }

   public TreeManager getTreeManager() {
       return treeManager;
   }

   public TabManager getTabManager() {
       return tabManager;
   }

   @FXML
   private void search() {
       treeManager.search(searchBar.getText());
   }



   public void setSongProgressSliderPos(int currentSongDuration, int totalSongDuration) {
       songProgressSlider.setValue((double) currentSongDuration /totalSongDuration * 100 + 0.00001);
   }

   @FXML
   private void forceGenerateCache() {
       // for (File artistFolder : Objects.requireNonNull(new File(App.getReferences().getRootDirectoryPath()).listFiles(FileUtils.getFileFilter(FILTER_TYPE.FOLDER)))) {
       //     for (File albumFolder : Objects.requireNonNull(artistFolder.listFiles(FileUtils.getFileFilter(FILTER_TYPE.FOLDER)))) {
       //         for(File coverImage : Objects.requireNonNull(albumFolder.listFiles(FileUtils.getFileFilter(FILTER_TYPE.COVER_IMAGE)))){
       //             if(coverImage.delete()) {
       //                 LOGGER.fine(String.format("[%s] deleted", coverImage.getName()));
       //             } else {
       //                 LOGGER.warning(String.format("[%s] could not be deleted", coverImage.getName()));
       //             }
       //         }
       //         for(File iconImage : Objects.requireNonNull(albumFolder.listFiles(FileUtils.getFileFilter(FILTER_TYPE.COVER_IMAGE)))){
       //             if(iconImage.delete()) {
       //                 LOGGER.fine(String.format("[%s] deleted", iconImage.getName()));
       //             } else {
       //                 LOGGER.warning(String.format("[%s] could not be deleted", iconImage.getName()));
       //             }
       //         }
       //     }
       // }
       //
       // FileUtils.refreshAllArt();
   }

   public void setTotTrackTime(int sec) {
       this.totTrackTime.setText(formatTime(sec));
   }

   private String formatTime(int sec) {
       String text;
       int min = sec / 60;

       if(min < 10){
           text = "0" + min;
       } else {
           text = String.valueOf(min);
       }

       if((sec % 60) < 10){
           text = text + ":0" + sec % 60;
       } else {
           text = text + ":" + sec % 60;
       }

       return text;
   }

   public void setCurrentTrackTime(int sec) {
       this.currentTrackTime.setText(formatTime(sec));
   }

   @FXML
   private void openDirectory() {
       FileUtils.openFileExplorer(UserDataHandler.getPath());
   }

   @FXML
   public void refreshTreeView() {
       treeManager.refresh();
   }

   @FXML
   private void newPlaylist() {
       // final Stage dialog = new Stage();
       // dialog.initModality(Modality.APPLICATION_MODAL);
       // dialog.initOwner(primaryStage);
       //
       // FXMLLoader loader = new FXMLLoader();
       // AnchorPane playlistPane = null;
       //
       // try{
       //     loader.setLocation(Path.of(FXML_PATHS.NEW_PLAYLIST.get()).toUri().toURL());
       //     playlistPane = loader.load();
       // } catch (IOException e) {
       //     LOGGER.severe("Could not load playlist window: " + e.getMessage());
       // }
       // PopupWindowsController controller = loader.getController();
       // controller.setValues(dialog, this);
       //
       // Scene dialogScene = new Scene(playlistPane, 300, 100);
       // dialogScene.setFill(Color.TRANSPARENT);
       //
       // dialog.setTitle("Create Playlist");
       // dialog.initStyle(StageStyle.UNIFIED);
       // dialog.getIcons().add(IMAGES.BERRIES.get());
       // dialog.setResizable(false);
       // dialog.setScene(dialogScene);
       // dialog.show();
       //
       // switch (App.getCurrentOS()) {
       //     case WINDOWS_11 -> {
       //         Win11ThemeWindowManager themeWindowManager = (Win11ThemeWindowManager) ThemeWindowManagerFactory.create();
       //         themeWindowManager.setDarkModeForWindowFrame(dialog, true);
       //         themeWindowManager.setWindowBackdrop(dialog, Win11ThemeWindowManager.Backdrop.ACRYLIC);
       //     }
       // }
   }

   @FXML
   void selectPreview() {
        TreeItem<String> selectedItem = treeView.getSelectionModel().getSelectedItem();
        tabManager.openSelected(selectedItem);
   }

   @FXML
   public void playPauseMedia() {
       if (musicPlayer.getMusicPlayerState() == MusicPlayer.MUSIC_PLAYER_STATUS.PLAYING) {
           musicPlayer.pause();
       } else {
           musicPlayer.play();
       }
   }

   public void setPaused(Boolean paused) {
       if(paused) {
           currentPlayPauseImageView.setImage(IMAGES.PLAY.get());
       } else  {
           currentPlayPauseImageView.setImage(IMAGES.PAUSE.get());
       }

       this.paused = paused;
   }

   @FXML
   public void resetBottomBar() {
       songLabel.setText("No Song Playing");
       artistLabel.setText("No Artist");
   }

   public void updateBottomBar() {
       songLabel.setText(musicPlayer.getCurrentSong().title());
       artistLabel.setText(musicPlayer.getCurrentSong().artist().name());

       switch (musicPlayer.getMusicPlayerState()) {
           case MusicPlayer.MUSIC_PLAYER_STATUS.PLAYING:
               setPaused(false);
               break;
           default:
               setPaused(true);
               break;
       }

       try{
           currentAlbumImageView.setImage(ImageUtils.pathToImage(musicPlayer.getCurrentSong().album().imagePath()));
       } catch (Exception e) {
           System.err.println("Error loading song image: " + e.getMessage());
       }
   }

   @FXML
   public void nextMedia() {
       musicPlayer.next();
   }

   @FXML
   public void previousMedia() {
       musicPlayer.previous();
   }

   @FXML
   public void repeatCycle() {
       musicPlayer.cycleRepeatStatus();
   }

   public void updateRepeatButton() {
       switch(musicPlayer.getRepeatStatus()) {
           case OFF:
               repeatImageView.setImage(IMAGES.REPEAT_UNSELECTED.get());
               break;
           case REPEAT_ALL:
               repeatImageView.setImage(IMAGES.REPEAT_ALL.get());
               break;
           case REPEAT_ONE:
               repeatImageView.setImage(IMAGES.REPEAT_ONE.get());
               break;
       }
   }
   @FXML
   public void shuffleToggle() {
       musicPlayer.toggleShuffleStatus();
   }

   public void updateShuffleButton() {
       switch(musicPlayer.getShuffleStatus()) {
           case OFF -> shuffleImageView.setImage(IMAGES.SHUFFLE_UNSELECTED.get());
           case SHUFFLE -> shuffleImageView.setImage(IMAGES.SHUFFLE_SELECTED.get());
       }
   }

   @FXML
   public void initChangeSongPos() {
       musicPlayer.pauseTimeline();
   }

   @FXML
   public void changeSongPos() {
       musicPlayer.changeSongPos(songProgressSlider.getValue());
   }

   private void updateSongPos(MouseEvent e) {
       setCurrentTrackTime(musicPlayer.getSongPosFromSlider((int) songProgressSlider.getValue()));
   }

   public void pickDirectory() {
       File selectedDirectory = FileUtils.openDirectoryChooser(new Stage(), "Pick a Directory", UserDataHandler.getPath());
       UserDataHandler.setPath(selectedDirectory.getAbsolutePath());
       refreshTreeView();
   }

   public MusicPlayer getMusicPlayer() {
       return musicPlayer;
   }


}
