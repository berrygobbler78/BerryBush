package com.berrygobbler78.flacplayer.util.records;

import java.util.Objects;

public record Song(String title, Album album, Artist artist, int track, int disc, String path) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Song song = (Song) o;
        return track == song.track &&
               Objects.equals(title, song.title) &&
               Objects.equals(path, song.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, track, path);
    }
}

