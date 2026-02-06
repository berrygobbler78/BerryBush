package com.berrygobbler78.flacplayer.userdata;

import com.berrygobbler78.flacplayer.App;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

public class Playlist implements Serializable {
    private String path;
    private ArrayList<String> songList =new ArrayList<>();
    private String playlistName;

    public Playlist(String playlistName) {
        this.playlistName = playlistName;
        path = "src/main/resources/com/berrygobbler78/flacplayer/graphics/playlist-art/" +
                playlistName.toLowerCase().replace(" ", "-");
    }

    public void setName(String playlistName) {
        this.playlistName = playlistName;
    }

    public String getName() {
        return playlistName;
    }

    public String getPath() {
        return path;
    }

    public ArrayList<String> getSongList(){
        for(String s: songList){
            if(!new File(s).exists()){
                songList.remove(s);
            }
        }
        return songList;
    }

    public void addSong(String song) {
        songList.add(song);
        App.savePlaylists();
    }

    public void addSong(int index, String song) {
        songList.add(index, song);
        App.savePlaylists();
    }

    public void removeSong(int index) {
        songList.remove(index);
        App.savePlaylists();
    }
}
