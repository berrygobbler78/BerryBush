package com.berrygobbler78.flacplayer.util.records;

import java.util.List;

public record Album(String title, Artist artist, List<Song> songs, String iconPath, String imagePath) { }
