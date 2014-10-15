## Deployment Instructions:
  # Clone repo  
  git clone git@github.com:FlorisTurkenburg/StudentChoice10419667.git FlorisTurkenburg
  
  # Import project into Eclipse  
  In eclipse: File -> Import -> General -> Existing Projects into Workspace -> Set the root directory to the FlorisTurkenburg directory -> Finish  
  
  # Include the Support Library (appcompat_v7)  
  Make sure you have the Android Support Library, else install it with the SDK Manager.  
  In Eclipse: Project -> Properties -> Android, add the appcompat_v7 to the libraries.
  
  # Run the app  
  Press ctrl+F11, or click Run -> Run and select the Android device or AVD on which you want to install the app.

Audio Player app
=====================
The goal of this project is to build an Android Application that plays music which is stored on the device, and has the possibility to create playlists.

List of features:
  - list of the available audio files on the device, sortable by name, artist, album, etc.
  - list of custom playlists, and option to create a new playlist
  - a view of the song currently playing and the options to stop, pause and resume the song
  - buttons for playing previous and next song in the playlist
  - continues playing the music when app is in background (e.g. when the user locks the screen)
  - notification with the currently playing song info (name, artist, album, etc.)
  - pause the music if external speakers are disconnected (for instance when the earphones are accidentally plugged out, the music must not continue over the device speakers)
  - while playing music, the app should still be able to be used for viewing the songs and playlist, create new playlists, select a new song, etc.  


![Overview of the app](/doc/Overview.png "Overview of the screens")

Via this links you can view an interactive version of the mockups:  
https://www.fluidui.com/editor/live/preview/p_VM6l0XTqhD2dIN671MLvJvRFJTQF19Sv.1412541208705
