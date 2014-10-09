Design Document
===============

MainActivity
-----------
- Consists of 3 tabs, implemented with a ViewPager and Fragments
- Tab 1: Songs;
  + Displays a ListView of all the songs available and their artist
  + ListView is filled through a SimpleCursorAdapter and a Cursor
  + The ContentResolver for the Cursor gets the contents from MediaStore.Audio
  + Clicking on an item in the ListView will play the song (MediaPlayerService)
- Tab 2: Artists;
  + Displays Listview of all the artists
  + Implemented in a way similar to Tab 1
  + Clicking on an item in the ListView will open a new view with a list of all the songs from the corresponding artist
- Tab 3: Playlists;
  + Displays a ListView of all the stored playlists
  + Again similar to the first tab, getting the contents from MediaStore.Audio.Playlists
  + Clicking on a playlist will open a new view displaying the songs and their artists which are in the playlist
  + This tab also provide a menu option to create a new playlist
- The views of the tabs are filled in their corresponding Fragment.java classes (e.g. SongFragment.java)
- All the views will have at the bottom a small audio player "widget" which displays the song and artist currently playing, and 3 buttons for previous song, next song, and pause/resume.

MediaPlayer
----------
  - Service running asynchronously.
  - Uses Wake Lock to prevent the system from stopping the CPU and thus stopping the playback.
  - It is a foreground Service to make it less likely to be killed and to provide a notification in the status bar.
  - Handles Audio Focus to mute or lower the sound when other sounds/applications need the audio output.
  - Handles AUDIO_BECOMING_NOISY intent to pause the audio when external speakers (e.g. earphones) are disconnected.

Playlist
---------
While in the view which displays the contents of a playlist, a menu is available with 4 options:
  - Delete; 
    + Opens a ListView of all the songs in the playlist with checkboxes. 
    + The selected songs are removed from the playlist when the user clicks on "Delete" in the ActionBar. 
    + Clicking on "Cancel" will leave the playlist unchanged.
    + The ActionBar also displays the amount of selections.
    + Provides a "Select All" option.
  - Change Title;
    + Opens a dialog to edit the name of the playlist.
  - Add Songs;
    + Opens a new View containing two tabs: Songs and Artists.
    + Each tab displays the corresponding ListView of songs and artists with checkboxes.
    + Clicking on "Done" in the ActionBar will add the selected songs to the playlist.
    + Basicly this is similar to the Main screen, excluding the third tab, and with checkboxes for the songs.
  - Remove Playlist;
    + This will delete the playlist after getting a confirmation from the user via an AlertDialog.


![Overview of the app](/Overview.png "Overview of the screens")
