package com.berrygobbler78.flacplayer.util.records;

import java.util.List;
import java.util.Objects;

public record Album(String title, Artist artist, List<Song> songs, String iconPath, String imagePath) {
    @Override
    public int hashCode() {
        return Objects.hash(title, iconPath, imagePath);
    }
}
