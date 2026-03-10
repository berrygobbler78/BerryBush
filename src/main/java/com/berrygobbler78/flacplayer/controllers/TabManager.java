package com.berrygobbler78.flacplayer.controllers;

import com.berrygobbler78.flacplayer.util.ImageUtils;
import com.berrygobbler78.flacplayer.util.handlers.ResourceHandler;
import com.berrygobbler78.flacplayer.util.records.Album;
import com.berrygobbler78.flacplayer.util.records.Artist;
import com.berrygobbler78.flacplayer.util.records.Playlist;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TabManager {
    private static final Logger logger = LogManager.getLogger();

    private final MainController controller;
    private final TabPane tabPane;

    public static HashMap<Tab, PreviewTabController> tabControllerMap = new HashMap<>();

    public TabManager(MainController controller, TabPane tabPane) {
        logger.debug("{} created", TabManager.class.getName());

        this.controller = controller;
        this.tabPane = tabPane;
            this.tabPane.setTabDragPolicy(TabPane.TabDragPolicy.REORDER);
            this.tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
            this.tabPane.setTabMaxWidth(125);
            this.tabPane.setFocusTraversable(true);
    }

    public void openSelected(TreeItem<String> selectedItem) {
        if(selectedItem == null) {
            logger.error("Selected item was null, returning...");
            return;
        }

        for (Tab tab : tabPane.getTabs()) {
            if (tab.getText().equals(selectedItem.getValue())) {
                logger.debug("Tab '{}' already open", tab.getText());
                return;
            }
        }

        for(TreeItem<String> ti : controller.getTreeManager().getSongMap().keySet()) {
            if(selectedItem.getValue().equals(ti.getValue()) && selectedItem.getGraphic() == ti.getGraphic()) {
                logger.debug("Found song match for selected item");
                controller.getMusicPlayer().loadSong(controller.getTreeManager().getSongMap().get(ti), true);
                return;
            }
        }

        for(TreeItem<String> ti : controller.getTreeManager().getAlbumMap().keySet()) {
            if(selectedItem.getValue().equals(ti.getValue()) && selectedItem.getGraphic() == ti.getGraphic()) {
                logger.debug("Found album match for selected item");

                Album album = controller.getTreeManager().getAlbumMap().get(ti);

                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(ResourceHandler.getResourceURL("fxml/previewTab.fxml"));

                Node previewNode;

                try{
                    previewNode = loader.load();
                } catch (IOException e) {
                    logger.error("Could not load preview fxml : {}", e.getMessage());
                    return;
                }

                Tab previewTab = new Tab(selectedItem.getValue());
                    previewTab.setContent(previewNode);
                    previewTab.setGraphic(new ImageView(ImageUtils.pathToImage(album.iconPath())));


                PreviewTabController previewTabController = loader.getController();
                    previewTabController.setMainController(controller);
                    previewTabController.setAlbumValues(album);
                    previewTabController.setPaused(true);

                previewTab.setOnSelectionChanged(_ -> {
                    if(previewTab.isSelected()) {
                        previewTabController.refreshSongs();
                    }
                });

                tabPane.getTabs().add(previewTab);
                tabPane.getSelectionModel().select(previewTab);

                if(!tabControllerMap.containsKey(previewTab)) {
                    tabControllerMap.put(previewTab, previewTabController);
                }
                return;
            }
        }

        for(TreeItem<String> ti : controller.getTreeManager().getPlaylistMap().keySet()) {
            if(selectedItem.getValue().equals(ti.getValue()) && selectedItem.getGraphic() == ti.getGraphic()) {
                logger.debug("Found playlist match for selected item");

                Playlist playlist = controller.getTreeManager().getPlaylistMap().get(ti);

                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(ResourceHandler.getResourceURL("fxml/previewTab.fxml"));

                Node previewNode;

                try{
                    previewNode = loader.load();
                } catch (IOException e) {
                    logger.error("Could not load preview fxml : {}", e.getMessage());
                    return;
                }

                Tab previewTab = new Tab(selectedItem.getValue());
                previewTab.setContent(previewNode);
                // previewTab.setGraphic(new ImageView(ImageUtils.pathToImage(playlist.iconPath())));

                PreviewTabController previewTabController = loader.getController();
                previewTabController.setMainController(controller);
                previewTabController.setPlaylistValues(playlist);
                previewTabController.setPaused(true);

                previewTab.setOnSelectionChanged(_ -> {
                    if(previewTab.isSelected()) {
                        previewTabController.refreshSongs();
                    }
                });

                tabPane.getTabs().add(previewTab);
                tabPane.getSelectionModel().select(previewTab);

                if(!tabControllerMap.containsKey(previewTab)) {
                    tabControllerMap.put(previewTab, previewTabController);
                }
                return;
            }
        }

        for(TreeItem<String> ti : controller.getTreeManager().getArtistMap().keySet()) {
            if(selectedItem.getValue().equals(ti.getValue()) && selectedItem.getGraphic() == ti.getGraphic()) {
                logger.debug("Found artist match for selected item");

                Artist artist = controller.getTreeManager().getArtistMap().get(ti);

                logger.error("Unimplemented artist page feature");

                return;
            }
        }

        logger.error("Unknown item selected : '{}'", selectedItem.getValue());
    }

    public void removeTab(PreviewTabController tab) {
        Tab tabToRemove = null;

        for (Map.Entry<Tab, PreviewTabController> entry : tabControllerMap.entrySet()) {
            if (entry.getValue().equals(tab)) {
                tabToRemove = entry.getKey();
                break;
            }
        }

        if (tabToRemove != null) {
            tabControllerMap.remove(tabToRemove);
            tabPane.getTabs().remove(tabToRemove);
        }
    }
}
