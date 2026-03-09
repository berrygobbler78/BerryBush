package com.berrygobbler78.flacplayer.util.records;

import java.util.List;

public record Artist(String name, List<Album> albums, String iconPath, String imagePath) {}
