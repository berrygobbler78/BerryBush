package com.berrygobbler78.flacplayer.controllers;

import com.berrygobbler78.flacplayer.configuration.PlaylistDataHandler;
import com.berrygobbler78.flacplayer.configuration.PlaylistDataHandler.Playlist;
import com.berrygobbler78.flacplayer.configuration.UserDataHandler;
import com.berrygobbler78.flacplayer.util.Constants;
import com.berrygobbler78.flacplayer.util.ImageUtils;
import com.berrygobbler78.flacplayer.util.handlers.RecordHandler;
import com.berrygobbler78.flacplayer.util.records.Album;
import com.berrygobbler78.flacplayer.util.records.Artist;
import com.berrygobbler78.flacplayer.util.records.Song;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class TreeManager {
    private static final Logger logger = LogManager.getLogger();

    private final MainController CONTROLLER;

    private final TreeView<String> TREE_VIEW;
    private final TreeItem<String> DEFAULT_ROOT = new TreeItem<>();

    private final HashMap<TreeItem<String>, Artist> ARTIST_MAP = new HashMap<>();
    private final HashMap<TreeItem<String>, Album> ALBUM_MAP = new HashMap<>();
    private final HashMap<TreeItem<String>, Song> SONG_MAP = new HashMap<>();
    private final HashMap<TreeItem<String>, Playlist> PLAYLIST_MAP = new HashMap<>();

    public enum SortingType {
        ALPHABETICAL,
        REVERSE_ALPHABETICAL,
        RECENT,
        REVERSE_RECENT
    }

    private SortingType currentSort = SortingType.ALPHABETICAL;

    private  TreeItem<String> userItem = new TreeItem<>(UserDataHandler.getUsername());

    public TreeManager(MainController controller, TreeView<String> treeView) {
        this.CONTROLLER = controller;
        this.TREE_VIEW = treeView;
            TREE_VIEW.setOnMouseClicked(event -> {
                if(event.getButton().equals(MouseButton.PRIMARY)){
                    if(event.getClickCount() == 2){
                        CONTROLLER.selectPreview();
                    }
                }
            });

        refresh();
    }

    public void search(String query) {
        if(query.isEmpty()){
            TREE_VIEW.setRoot(DEFAULT_ROOT);
            return;
        }

        TreeItem<String> searchRoot = new TreeItem<>();
        TREE_VIEW.setRoot(searchRoot);

        for(TreeItem<String> item : SONG_MAP.keySet()){
            if(item.getValue().toLowerCase().contains(query.toLowerCase())){
                searchRoot.getChildren().add(copyTreeItem(item));
            }
        }

        for(TreeItem<String> item : ALBUM_MAP.keySet()){
            if(item.getValue().toLowerCase().contains(query.toLowerCase())){
                searchRoot.getChildren().add(copyTreeItem(item));
            }
        }

        for(TreeItem<String> item : ARTIST_MAP.keySet()){
            if(item.getValue().toLowerCase().contains(query.toLowerCase())){
                searchRoot.getChildren().add(copyTreeItem(item));
            }
        }

        for(TreeItem<String> item : PLAYLIST_MAP.keySet()){
            if(item.getValue().toLowerCase().contains(query.toLowerCase())){
                searchRoot.getChildren().add(copyTreeItem(item));
            }
        }

        if(userItem.getValue().toLowerCase().contains(query.toLowerCase())){
            searchRoot.getChildren().add(copyTreeItem(userItem));
        }

        TREE_VIEW.getSelectionModel().selectFirst();
    }

    public void refresh() {
        logger.info("Refreshing tree view...");

        ImageUtils.refreshAllArt();

        ARTIST_MAP.clear();
        ALBUM_MAP.clear();
        SONG_MAP.clear();

        DEFAULT_ROOT.getChildren().clear();

        TreeItem<String> userItem = new TreeItem<>(UserDataHandler.getUsername(), new ImageView(Constants.IMAGES.USER.get()));
        for (Playlist playlist : Objects.requireNonNull(PlaylistDataHandler.getPlaylists())) {
            // Image playlistIcon;
            //
            // try {
            //     playlistIcon = getCoverIcon(playlist.getPath(), FileUtils.FILE_TYPE.PLAYLIST);
            // } catch (Exception e) {
            //     LOGGER.warning("Could not get cover image for playlist: " + playlist.getPath());
            //     playlistIcon = Constants.IMAGES.WARNING.get();
            // }

            TreeItem<String> playlistItem = new TreeItem<>(playlist.getName());
            PLAYLIST_MAP.put(playlistItem, playlist);

            userItem.getChildren().add(playlistItem);
        }

        this.userItem = userItem;
        DEFAULT_ROOT.getChildren().add(userItem);


        for (Artist artist : RecordHandler.getArtistList()) {
            TreeItem<String> artistItem = null;
            TreeItem<String> albumItem;

            // Find or create artist
            for (TreeItem<String> item : ARTIST_MAP.keySet()) {
                if (item.getValue().equals(artist.name())) {
                    artistItem = item;
                    break;
                }
            }

            if (artistItem == null) {
                artistItem = new TreeItem<>(artist.name(), new ImageView(Constants.IMAGES.CD.get()));
                DEFAULT_ROOT.getChildren().add(artistItem);
                ARTIST_MAP.put(artistItem, artist);
            }

            // Find or create album under artist
            for(Album album : artist.albums()) {
                albumItem = null;

                for (TreeItem<String> item : ALBUM_MAP.keySet()) {
                    if (item.getValue().equals(album.title())) {
                        albumItem = item;
                        break;
                    }
                }

                if (albumItem == null) {
                    albumItem = new TreeItem<>(
                            album.title(),
                            new ImageView(ImageUtils.pathToImage(album.iconPath()))
                    );

                    artistItem.getChildren().add(albumItem);
                    ALBUM_MAP.put(albumItem, album);
                }
            }
        }

        for(Song s : RecordHandler.getSongList()) {
            SONG_MAP.put(new TreeItem<>(s.title(), new ImageView(Constants.IMAGES.SONG.get())), s);
        }

        TREE_VIEW.setRoot(DEFAULT_ROOT);
        TREE_VIEW.setShowRoot(false);

        sort(currentSort);

        logger.info("Finished refreshing tree view");
    }

    public void sort(SortingType type) {
        switch (type) {
            case ALPHABETICAL -> {
                logger.debug("Sorting set to ALPHABETICAL");
                DEFAULT_ROOT.getChildren().sort((obj1, obj2) -> {
                    if (Objects.equals(obj1.getValue(), userItem.getValue()) || Objects.equals(obj2.getValue(), userItem.getValue())) {
                        return 0;
                    } else {
                        return obj1.getValue().compareTo(obj2.getValue());
                    }
                });
            }
            case REVERSE_ALPHABETICAL -> {
                logger.debug("Sorting set to REVERSE_ALPHABETICAL");
                DEFAULT_ROOT.getChildren().sort((obj1, obj2) -> {
                    if (Objects.equals(obj1.getValue(), userItem.getValue()) || Objects.equals(obj2.getValue(), userItem.getValue())) {
                        return 0;
                    } else {
                        return obj2.getValue().compareTo(obj1.getValue());
                    }
                });
            }
            default -> logger.error("Unimplemented sorting");
        }

        TREE_VIEW.refresh();
    }

    public HashMap<TreeItem<String>, Playlist> getPlaylistMap() {
        return PLAYLIST_MAP;
    }

    public HashMap<TreeItem<String>, Album> getAlbumMap() {
        return ALBUM_MAP;
    }

    public HashMap<TreeItem<String>, Artist> getArtistMap() {
        return ARTIST_MAP;
    }

    public HashMap<TreeItem<String>, Song> getSongMap() {
        return SONG_MAP;
    }

    public static <T> TreeItem<T> copyTreeItem(TreeItem<T> original) {
        if (original == null) return null;

        TreeItem<T> copy = new TreeItem<>(original.getValue(), original.getGraphic());

        for (TreeItem<T> child : original.getChildren()) {
            copy.getChildren().add(copyTreeItem(child));
        }

        copy.setExpanded(original.isExpanded());

        return copy;
    }
}
