package com.berrygobbler78.flacplayer.util.records;

import java.util.List;
import java.util.Objects;

public record Artist(String name, List<Album> albums, String artPath) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Artist artist = (Artist) o;
        return Objects.equals(name, artist.name) &&
               Objects.equals(artPath, artist.artPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, artPath);
    }
}
