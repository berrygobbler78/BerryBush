package com.berrygobbler78.flacplayer.util.records;

import java.util.List;
import java.util.Objects;

public record Album(String title, Artist artist, List<Song> songs, String artPath) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Album album = (Album) o;
        return Objects.equals(title, album.title) &&
               Objects.equals(artPath, album.artPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, artPath);
    }
}
