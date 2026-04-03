package com.berrygobbler78.flacplayer;

import com.berrygobbler78.flacplayer.configuration.UserDataHandler;
import com.berrygobbler78.flacplayer.controllers.LandingController;
import com.berrygobbler78.flacplayer.util.ImageUtils;
import com.berrygobbler78.flacplayer.util.handlers.RecordHandler;
import com.berrygobbler78.flacplayer.util.handlers.ResourceHandler;
import com.berrygobbler78.flacplayer.util.records.Album;
import com.berrygobbler78.flacplayer.util.records.Artist;
import com.berrygobbler78.flacplayer.util.records.Song;
import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.util.Duration;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class TreeManager {
    private static final Logger logger = LogManager.getLogger();

    private final LandingController controller;

    private final TreeView<String> TREE_VIEW;
    private final TreeItem<String> DEFAULT_ROOT = new TreeItem<>();

    private final HashMap<TreeItem<String>, Artist> ARTIST_MAP = new HashMap<>();
    private final HashMap<TreeItem<String>, Album> ALBUM_MAP = new HashMap<>();
    private final HashMap<TreeItem<String>, Song> SONG_MAP = new HashMap<>();

    public enum SortingType {
        ALPHABETICAL,
        REVERSE_ALPHABETICAL,
        RECENT,
        REVERSE_RECENT
    }

    private SortingType currentSort = SortingType.ALPHABETICAL;

    private  TreeItem<String> userItem = new TreeItem<>(UserDataHandler.getUsername());

    public TreeManager(LandingController controller, TreeView<String> treeView) {
        this.controller = controller;
        this.TREE_VIEW = treeView;

        TREE_VIEW.setOnMouseClicked(event -> {
            Album album = ALBUM_MAP.get(TREE_VIEW.getSelectionModel().getSelectedItem());
        });

        TREE_VIEW.setCellFactory(_ -> new TreeCell<>() {
            private ChangeListener<? super Boolean> expandedListener;
            private TreeItem<String> lastItem;

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                // Remove listener from previous item
                if (lastItem != null && expandedListener != null) {
                    lastItem.expandedProperty().removeListener(expandedListener);
                }

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    lastItem = null;
                } else {
                    setText(item);
                    TreeItem<String> treeItem = getTreeItem();
                    lastItem = treeItem;

                    if (treeItem != null) {
                        setGraphic(treeItem.getGraphic());

                        // Listener for smooth expansion transition
                        expandedListener = (obs, wasExpanded, isExpanded) -> {
                            Node disclosureNode = lookup(".disclosure-node");
                            if (disclosureNode != null) {
                                RotateTransition rt = new RotateTransition(Duration.millis(200), disclosureNode);
                                if (isExpanded) {
                                    rt.setFromAngle(0);
                                    rt.setToAngle(90);
                                } else {
                                    rt.setFromAngle(90);
                                    rt.setToAngle(0);
                                }
                                rt.play();
                            }
                        };

                        treeItem.expandedProperty().addListener(expandedListener);

                        // Initial state of disclosure node
                        Node disclosureNode = lookup(".disclosure-node");
                        if (disclosureNode != null) {
                            disclosureNode.setRotate(treeItem.isExpanded() ? 90 : 0);
                        } else {
                            // If disclosure node is not found yet, wait for layout to set initial rotation
                            Platform.runLater(() -> {
                                Node dn = lookup(".disclosure-node");
                                if (dn != null) {
                                    dn.setRotate(treeItem.isExpanded() ? 90 : 0);
                                }
                            });
                        }
                    }
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

        if(userItem.getValue().toLowerCase().contains(query.toLowerCase())){
            searchRoot.getChildren().add(copyTreeItem(userItem));
        }

        TREE_VIEW.getSelectionModel().selectFirst();
    }

    public void refresh() {
        logger.info("Refreshing tree view...");

        ARTIST_MAP.clear();
        ALBUM_MAP.clear();
        SONG_MAP.clear();

        DEFAULT_ROOT.getChildren().clear();

        for (Artist artist : RecordHandler.getArtistList()) {
            TreeItem<String> artistItem = null;
            TreeItem<String> albumItem;

            // Find or create an artist
            for (TreeItem<String> item : ARTIST_MAP.keySet()) {
                if (item.getValue().equals(artist.name())) {
                    artistItem = item;
                    break;
                }
            }

            if (artistItem == null) {
                artistItem = new TreeItem<>(artist.name(), new ImageView(new Image(String.valueOf(ResourceHandler.getResourceURL("graphics/sidebar/cd.png")), 20, 20, false, false)));
                DEFAULT_ROOT.getChildren().add(artistItem);
                ARTIST_MAP.put(artistItem, artist);
            }

            // Find or create an album under an artist
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
                            new ImageView(ImageUtils.pathToImage(album.artPath()))
                    );

                    artistItem.getChildren().add(albumItem);
                    ALBUM_MAP.put(albumItem, album);
                }
            }
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
                    obj1.setExpanded(false);
                    obj2.setExpanded(false);

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
                    obj1.setExpanded(false);
                    obj2.setExpanded(false);
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