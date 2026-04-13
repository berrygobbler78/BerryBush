package com.berrygobbler78.flacplayer.gui.managers;

import com.berrygobbler78.flacplayer.util.records.RecordHandler;
import com.berrygobbler78.flacplayer.util.records.Album;
import com.berrygobbler78.flacplayer.util.records.Artist;
import com.berrygobbler78.flacplayer.util.records.Song;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.LinkedHashSet;

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

        var suggestions = new LinkedHashSet<String>();

        for (Artist a : RecordHandler.getArtists()) {
            if (a != null && a.title() != null && !a.title().isBlank()) {
                suggestions.add(a.title());
            }
        }

        for (Album a : RecordHandler.getAlbums()) {
            if (a != null && a.title() != null && !a.title().isBlank()) {
                suggestions.add(a.title());
            }
        }

        for (Song s : RecordHandler.getSongs()) {
            if (s != null && s.title() != null && !s.title().isBlank()) {
                suggestions.add(s.title());
            }
        }

        searchResults.addAll(suggestions);
    }

    private void installAutocomplete() {
        searchBar.textProperty().addListener((_, _, newText) -> {
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

            for (var suggestion : filtered) {
                var label = new Label(suggestion);
                var item = new CustomMenuItem(label, true);

                item.setOnAction(_ -> {
                    searchBar.setText(suggestion);
                    searchBar.positionCaret(suggestion.length());
                    suggestionsPopup.hide();
                });

                suggestionsPopup.getItems().add(item);
            }

            if (!suggestionsPopup.isShowing()) suggestionsPopup.show(searchBar, Side.BOTTOM, 0, 0);
        });

        searchBar.focusedProperty().addListener((_, _, isFocused) -> {
            if (!isFocused) suggestionsPopup.hide();
        });
    }
}
