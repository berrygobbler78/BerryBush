package com.berrygobbler78.flacplayer;

import com.berrygobbler78.flacplayer.util.handlers.RecordHandler;
import com.berrygobbler78.flacplayer.util.records.Album;
import com.berrygobbler78.flacplayer.util.records.Artist;
import com.berrygobbler78.flacplayer.util.records.Song;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.LinkedHashSet;
import java.util.Set;

public class SearchManager {
    private final TextField searchBar;
    private final ObservableList<String> searchResults = FXCollections.observableArrayList();
    private final ContextMenu suggestionsPopup = new ContextMenu();

    public SearchManager(TextField searchBar) {
        this.searchBar = searchBar;

        generateSuggestions();
        installAutocomplete();
    }

    public void generateSuggestions() {
        searchResults.clear();

        Set<String> suggestions = new LinkedHashSet<>();

        for (Artist a : RecordHandler.getArtistList()) {
            if (a != null && a.name() != null && !a.name().isBlank()) {
                suggestions.add(a.name());
            }
        }

        for (Album a : RecordHandler.getAlbumList()) {
            if (a != null && a.title() != null && !a.title().isBlank()) {
                suggestions.add(a.title());
            }
        }

        for (Song s : RecordHandler.getSongList()) {
            if (s != null && s.title() != null && !s.title().isBlank()) {
                suggestions.add(s.title());
            }
        }

        searchResults.addAll(suggestions);
    }

    private void installAutocomplete() {
        searchBar.textProperty().addListener((obs, oldText, newText) -> {
            if (newText == null || newText.isBlank()) {
                suggestionsPopup.hide();
                return;
            }

            var filtered = searchResults.stream()
                    .filter(item -> item.toLowerCase().contains(newText.toLowerCase()))
                    .limit(10)
                    .toList();

            if (filtered.isEmpty()) {
                suggestionsPopup.hide();
                return;
            }

            suggestionsPopup.getItems().clear();

            for (String suggestion : filtered) {
                Label label = new Label(suggestion);
                CustomMenuItem item = new CustomMenuItem(label, true);

                item.setOnAction(e -> {
                    searchBar.setText(suggestion);
                    searchBar.positionCaret(suggestion.length());
                    suggestionsPopup.hide();
                });
                suggestionsPopup.getItems().add(item);
            }

            if (!suggestionsPopup.isShowing()) {
                suggestionsPopup.show(searchBar, Side.BOTTOM, 0, 0);
            }
        });

        searchBar.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                suggestionsPopup.hide();
            }
        });
    }
}
