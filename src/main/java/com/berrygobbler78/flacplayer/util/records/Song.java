package com.berrygobbler78.flacplayer.util.records;

import java.util.Objects;

public record Song(String title, Album album, short track, short disc, String path) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var song = (Song) o;
        return track == song.track &&
               Objects.equals(title, song.title) &&
               Objects.equals(path, song.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, track, path);
    }
}

