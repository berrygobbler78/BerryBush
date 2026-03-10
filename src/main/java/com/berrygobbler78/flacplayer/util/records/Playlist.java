package com.berrygobbler78.flacplayer.util.records;

import java.util.List;

public record Playlist(String title, String author, List<Song> songs, String path, String iconPath, String imagePath) { }
