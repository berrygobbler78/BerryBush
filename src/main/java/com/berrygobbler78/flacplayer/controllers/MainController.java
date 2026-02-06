package com.berrygobbler78.flacplayer.controllers;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.List;

import com.berrygobbler78.flacplayer.App;
import com.berrygobbler78.flacplayer.Enums;
import com.berrygobbler78.flacplayer.MusicPlayer;
import com.berrygobbler78.flacplayer.userdata.Playlist;
import com.berrygobbler78.flacplayer.userdata.References;
import com.berrygobbler78.flacplayer.Enums.*;
import com.berrygobbler78.flacplayer.Images.IMAGE;

import com.berrygobbler78.flacplayer.util.FileUtils;
import com.jfoenix.controls.JFXSlider;
import com.pixelduke.window.ThemeWindowManagerFactory;
import com.pixelduke.window.Win11ThemeWindowManager;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import static com.berrygobbler78.flacplayer.util.FileUtils.getCoverIcon;

public class MainController implements  Initializable {
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

    private TreeItem<String> defaultRoot = null;

    private HashMap<TreeItem<String>, String> artistItemMap = new HashMap<>();
    private HashMap<TreeItem<String>, String> albumItemMap = new HashMap<>();
    private HashMap<TreeItem<String>, String> songItemMap = new HashMap<>();
    private HashMap<TreeItem<String>, Playlist> playlistItemMap = new HashMap<>();

    private TreeItem<String> userItem = null;

    public static HashMap<Tab, PreviewTabController> tabControllerMap = new HashMap<>();

    private static final MusicPlayer musicPlayer = App.musicPlayer;
    private static final References references = App.references;

    private int repeatStatus = 0;
    private boolean shuffleStatus = false;

    private final Stage primaryStage = App.getPrimaryStage();

    boolean paused = true;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        previewTabPane.setTabDragPolicy(TabPane.TabDragPolicy.REORDER);
        previewTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
        previewTabPane.setTabMaxWidth(125);

        refreshTreeView();
        resetBottomBar();

//        DoubleClick check
        treeView.setOnMouseClicked(event -> {
            if(event.getButton().equals(MouseButton.PRIMARY)){
                if(event.getClickCount() == 2){
                    selectPreview();
                }
            }
        });

       // Hides JFX thumb
        songProgressSlider.setValueFactory(arg0 -> Bindings.createStringBinding(() -> "", songProgressSlider.valueProperty()));
        songProgressSlider.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::updateSongPos);

//        volumeSlider.valueProperty().addListener((ObservableValue<? extends Number> arg0, Number arg1, Number arg2) -> {
//            if (musicPlayer.isActive()) {
//                musicPlayer.mediaPlayer.setVolume(volumeSlider.getValue() / 150.0);
//            }
//        });
    }

    @FXML
    private void search() {
        treeView.getRoot().getChildren().clear();
        try {
            treeView.getRoot().getChildren().addAll(searchList(searchBar.getText()));
            treeView.getSelectionModel().selectFirst();
        } catch (Exception e) {
            TreeItem<String> root = new TreeItem<>("");
            root.getChildren().add(userItem);

            TreeMap<String, TreeItem<String>> map = new TreeMap<>();
            for(TreeItem<String> item : artistItemMap.keySet()){
                map.put(item.getValue(), item);
            }

            for(Map.Entry<String, TreeItem<String>> item : map.entrySet()){
                item.getValue().setExpanded(false);
                root.getChildren().add(item.getValue());
            }

            treeView.setRoot(root);
        }
    }

    private List<TreeItem<String>> searchList(String search) {
        List<TreeItem<String>> foundItems = new ArrayList<>();

        if(search.isEmpty()){
            return null;
        }

        for(TreeItem<String> item : artistItemMap.keySet()){
            if(item.getValue().toLowerCase().contains(search.toLowerCase())){
                item.setExpanded(false);
                foundItems.add(item);
            }
        }

        for(TreeItem<String> item : albumItemMap.keySet()){
            if(item.getValue().toLowerCase().contains(search.toLowerCase())){
                item.setExpanded(false);
                foundItems.add(item);
            }
        }

        for(TreeItem<String> item : songItemMap.keySet()){
            if(item.getValue().toLowerCase().contains(search.toLowerCase())){
                foundItems.add(item);
            }
        }

        for(TreeItem<String> item : playlistItemMap.keySet()){
            if(item.getValue().toLowerCase().contains(search.toLowerCase())){
                foundItems.add(item);
            }
        }

        if(userItem.getValue().toLowerCase().contains(search.toLowerCase())){
            foundItems.add(userItem);
        }

        return foundItems;
    }

    public void setSongProgressSliderPos(int currentSongDuration, int totalSongDuration) {
        songProgressSlider.setValue((double) currentSongDuration /totalSongDuration * 100 + 0.00001);
    }

    @FXML
    private void forceGenerateCache() {
        for (File artistFolder : Objects.requireNonNull(new File(App.references.getRootDirectoryPath()).listFiles(FileUtils.getFileFilter(FILTER_TYPE.FOLDER)))) {
            for (File albumFolder : Objects.requireNonNull(artistFolder.listFiles(FileUtils.getFileFilter(FILTER_TYPE.FOLDER)))) {
                for(File coverImage : albumFolder.listFiles(FileUtils.getFileFilter(FILTER_TYPE.COVER_IMAGE))){
                    coverImage.delete();
                }
                for(File iconImage : albumFolder.listFiles(FileUtils.getFileFilter(FILTER_TYPE.COVER_IMAGE))){
                    iconImage.delete();
                }

            }
        }

        FileUtils.refreshCoverArt();
    }

    public void setTotTrackTime(int sec) {
        this.totTrackTime.setText(formatTime(sec));
    }

    private String formatTime(int sec) {
        int min = sec / 60;
        String text;
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
        FileUtils.openFileExplorer(App.references.getRootDirectoryPath());
    }

    @FXML
    public void refreshTreeView() {
        FileUtils.refreshCoverArt();

        artistItemMap.clear();
        albumItemMap.clear();
        songItemMap.clear();

        TreeItem<String> rootItem = new TreeItem<>(new File(App.references.getRootDirectoryPath()).getName());

        TreeItem<String> userItem = new TreeItem<>(App.references.getUserName(), new ImageView(IMAGE.USER.get()));
        if(references.getPlaylists() != null || !references.getPlaylists().isEmpty()) {
            for(Playlist playlist : Objects.requireNonNull(App.references.getPlaylists())) {
                Image playlistIcon = null;
                try {
                    playlistIcon = getCoverIcon(playlist.getPath(), FILE_TYPE.PLAYLIST);
                } catch (IOException e) {
                    System.err.println("Error loading cover image for playlist: " + playlist.getName());
                }
                TreeItem<String> playlistItem = new TreeItem<>(playlist.getName(), new ImageView(playlistIcon));

                playlistItemMap.put(playlistItem, playlist);
                userItem.getChildren().add(playlistItem);
            }

            this.userItem = userItem;
            rootItem.getChildren().add(userItem);
        }

        for (File artistFile : Objects.requireNonNull(new File(App.references.getRootDirectoryPath()).listFiles(FileUtils.getFileFilter(FILTER_TYPE.FOLDER)))) {
            TreeItem<String> artistItem = new TreeItem<>(artistFile.getName(), new ImageView(IMAGE.CD.get()));
            for (File albumFile : Objects.requireNonNull(artistFile.listFiles(FileUtils.getFileFilter(FILTER_TYPE.FOLDER)))) {
                TreeItem<String> albumItem = null;
                Image albumArtIcon;
                try {
                     albumArtIcon = getCoverIcon(albumFile.getAbsolutePath(), FILE_TYPE.ALBUM);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                try {
                    String tempName = albumFile.getName().replaceAll("#00F3", "?");
                    albumItem = new TreeItem<String>(tempName, new ImageView(albumArtIcon));
                } catch(Exception e) {
                    String tempName = albumFile.getName().replaceAll("#00F3", "?");
                    albumItem = new TreeItem<>(tempName, new ImageView(IMAGE.WARNING.get()));
                }
                int songNum = 1;
                for(File songFile : Objects.requireNonNull(albumFile.listFiles(FileUtils.getFileFilter(FILTER_TYPE.FLAC)))) {
                    TreeItem<String> songItem = null;
                    songItem = new TreeItem<>(songNum + ". " + FileUtils.getSongTitle(songFile.getAbsolutePath()), new ImageView(albumArtIcon));
                    songNum++;
                    songItemMap.put(songItem, songFile.getAbsolutePath());
                }

                albumItemMap.put(albumItem, albumFile.getAbsolutePath());
                artistItem.getChildren().add(albumItem);

            }
            artistItemMap.put(artistItem, artistFile.getAbsolutePath());
            rootItem.getChildren().add(artistItem);

        }

        treeView.setRoot(rootItem);
        treeView.setShowRoot(false);
        treeView.refresh();

//        rootItem.getChildren().sort(Comparator.comparing(t->t.getValue().length()));
    }

    public ArrayList<Tab> getTabs() {
        return new ArrayList<>(previewTabPane.getTabs());
    }

    @FXML
    private void newPlaylist() {
        Win11ThemeWindowManager themeWindowManager = (Win11ThemeWindowManager) ThemeWindowManagerFactory.create();
        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);

        FXMLLoader loader = new FXMLLoader();
        try {
            loader.setLocation(Path.of("src/main/resources/com/berrygobbler78/flacplayer/fxml/newPlaylistWindow.fxml").toUri().toURL());
            AnchorPane playlistPane = loader.load();
            PopupWindowsController controller = loader.getController();
            controller.setValues(dialog, this);

            Scene dialogScene = new Scene(playlistPane, 300, 100);
            dialogScene.setFill(Color.TRANSPARENT);

            dialog.setTitle("Create Playlist");
            dialog.initStyle(StageStyle.UNIFIED);
            dialog.getIcons().add(IMAGE.CD.get());
            dialog.setResizable(false);
            dialog.setScene(dialogScene);
            dialog.show();

            themeWindowManager.setDarkModeForWindowFrame(dialog, true);
            themeWindowManager.setWindowBackdrop(dialog, Win11ThemeWindowManager.Backdrop.ACRYLIC);
        } catch (Exception e) {
            System.err.println("Error opening playlist window: " + e.getMessage());
        }
    }

    @FXML
    private void selectPreview() {
        TreeItem<String> selectedItem = treeView.getSelectionModel().getSelectedItem();

        for (Tab tab : previewTabPane.getTabs()) {
            if (tab.getText().equals(selectedItem.getValue())) {
                previewTabPane.getSelectionModel().select(tab);
                tabControllerMap.get(tab).refreshSongItemVBox();
                return;
            }
        }

        if(playlistItemMap.containsKey(selectedItem)) {
            FXMLLoader loader = new FXMLLoader();
            try {
                loader.setLocation(
                        Path.of("src/main/resources/com/berrygobbler78/flacplayer/fxml/previewTab.fxml").toUri().toURL());
                Node previewNode = loader.load();
                Tab previewTab = new Tab(selectedItem.getValue(), previewNode);
                previewTab.setContent(previewNode);

                PreviewTabController previewTabController = loader.getController();

                previewTabController.setMainController(this);
                previewTabController.setPlaylistValues(playlistItemMap.get(selectedItem));
                previewTabController.refreshSongItemVBox();
                previewTabController.setPlayPauseImageViewPaused(true);

                previewTab.setGraphic(new ImageView(FileUtils.getCoverIcon(playlistItemMap.get(selectedItem).getPath(), FILE_TYPE.PLAYLIST)));

                previewTabPane.getTabs().add(previewTab);
                previewTabPane.getSelectionModel().select(previewTab);

                previewTab.setOnSelectionChanged(event -> {
                    tabControllerMap.get(previewTab).refreshSongItemVBox();
                });

                if(!tabControllerMap.containsKey(previewTab)) {
                    tabControllerMap.put(previewTab, previewTabController);
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if(artistItemMap.containsKey(selectedItem)) {
//            Artist action
        } else if (albumItemMap.containsKey(selectedItem)) {
            boolean operationAvailable = true;

            File albumFile = new File(albumItemMap.get(selectedItem));

            if (new File(albumFile.getParent()).getName().equals(treeView.getRoot().getValue())) {
                operationAvailable = false;
            }

            if (operationAvailable) {
                FXMLLoader loader = new FXMLLoader();

                try {
                    loader.setLocation(
                            Path.of("src/main/resources/com/berrygobbler78/flacplayer/fxml/previewTab.fxml").toUri().toURL());

                    Node previewNode = loader.load();
                    Tab previewTab = new Tab(albumFile.getName());
                    previewTab.setContent(previewNode);

                    PreviewTabController previewTabController = loader.getController();
                    previewTabController.setMainController(this);
                    previewTabController.setAlbumValues(albumFile);
                    previewTabController.refreshSongItemVBox();
                    previewTabController.setPlayPauseImageViewPaused(true);

                    previewTab.setGraphic(new ImageView(FileUtils.getCoverIcon(albumFile.getAbsolutePath(), FILE_TYPE.ALBUM)));

                    previewTabPane.getTabs().add(previewTab);
                    previewTabPane.getSelectionModel().select(previewTab);

                    if(!tabControllerMap.containsKey(previewTab)) {
                        tabControllerMap.put(previewTab, previewTabController);
                    }

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } else if(songItemMap.containsKey(selectedItem)) {
            musicPlayer.loadSong(songItemMap.get(selectedItem));
            musicPlayer.play();
        } else {
            System.err.println("Unknown item selected: " + selectedItem.getValue());
        }

    }

    @FXML
    public void playPauseMedia() {
        if (musicPlayer.isPlaying()) {
            musicPlayer.pause();
        } else {
            musicPlayer.play();
        }
    }

    public void setCurrentPlayPauseImageViewPaused(Boolean paused) {
        if(paused) {
            currentPlayPauseImageView.setImage(IMAGE.PLAY.get());
        } else  {
            currentPlayPauseImageView.setImage(IMAGE.PAUSE.get());
        }

        this.paused = paused;
    }

    @FXML
    public void resetBottomBar() {
        songLabel.setText("No Song Playing");
        artistLabel.setText("No Artist");
    }

    public void updateBottomBar() {
        songLabel.setText(FileUtils.getSongTitle(musicPlayer.getCurrentSongPath()));
        artistLabel.setText(FileUtils.getSongArtist(musicPlayer.getCurrentSongPath()));
        setCurrentPlayPauseImageViewPaused(!musicPlayer.isPlaying());
        try{
            currentAlbumImageView.setImage(FileUtils.getCoverImage(musicPlayer.getCurrentSongPath(), FILE_TYPE.SONG));
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
        repeatStatus = (repeatStatus +1) % 3;

        switch(repeatStatus) {
            case 0:
                repeatImageView.setImage(IMAGE.REPEAT_UNSELECTED.get());
                musicPlayer.setRepeatStatus(Enums.REPEAT_STATUS.OFF);
                break;
            case 1:
                repeatImageView.setImage(IMAGE.REPEAT_ALL.get());
                musicPlayer.setRepeatStatus(Enums.REPEAT_STATUS.REPEAT_ALL);
                break;
            case 2:
                repeatImageView.setImage(IMAGE.REPEAT_ONE.get());
                musicPlayer.setRepeatStatus(Enums.REPEAT_STATUS.OFF);
                break;
        }
    }

    @FXML
    public void shuffleToggle() {
        shuffleStatus = !shuffleStatus;
        musicPlayer.setShuffleStatus(shuffleStatus);

        if(shuffleStatus) {
            shuffleImageView.setImage(IMAGE.SHUFFLE_SELECTED.get());
            musicPlayer.shuffle();
        } else {
            shuffleImageView.setImage(IMAGE.SHUFFLE_UNSELECTED.get());
            musicPlayer.refreshSongQueue();
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
        File selectedDirectory = FileUtils.openDirectoryChooser(new Stage(), "Pick a Directory", new File(App.references.getRootDirectoryPath()).getParent());
        App.references.setRootDirectoryPath(selectedDirectory.getAbsolutePath());
        refreshTreeView();
    }


}
