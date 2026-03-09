## Introduction:
Welcome to BerryBush.
The intention for this application is to be able to rip flac files from your cd collection and play them natively.

Please report bugs as you see them, I will fix them ASAP. Also let me know of any suggestions, I am open to anything!

![img.png](READMEIMAGE.png)

## Instructions:
1. Set up a directory
2. Rip your files. 
   1. I use EAC (Easy Audio Copy), with the MusicBrainz Metadata Plugin for metadata. (You will want metadata to show images). 
   My EAC profile is under resources.com.berrygobbler78.flacplayer. 
   2. Use the format: [track2digits - title.flac]. E.g: 01 - Silly Music Vol.1.flac .
3. Run the app and select your base directory and username. You can change it later in the settings. If at any time the userdata is messed up, it will prompt you to pick a new directory and username.
4. Playlists!
   1. You can make playlists with the plus button in the explorer. 
   2. Images can be added under resources/graphics/playlist-art/name-of-playlist.png Art will automatically be added when refreshing the explorer.
   3. To add a song, select the menu button next to a song in the preview tab and go to Playlists > Your Playlist Name


### FIXES:
- [ ] Fix playlists
- [ ] Gapless playback
- [ ] Polish volume control (Threading)
- [ ] Scrollbar retains position after refreshing songlist
- [ ] Fix UI resizing

### ROADMAP
- [ ] Color pick-able UI
- [ ] Musicbrainz integration
- [ ] Show songs in queue
- [ ] Edit file tagging (change image, name, artist, etc)
- [ ] Home page
- [ ] Change background picture
- [ ] Artist images
- [ ] Artist pages
- [ ] Playback speed
- [ ] Support for other file types
- [ ] Listening stats
- [ ] Highlight active parent in treeview

### Completed  ✓
- [x] Shuffling
- [x] Fix the window
- [x] Search feature
- [x] Setup wizard
- [x] Playlists
- [x] Switch to TOML data storage
- [x] Fix MPRIS
- [x] Add sorting for treeview

## Credits:
- I am using some Icon8 icons for my buttons, they're great.

