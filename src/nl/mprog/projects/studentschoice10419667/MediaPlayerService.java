/*
 * AudioPlayer app
 * 
 * Author: Floris Turkenburg
 * UvANetID: 10419667
 * Email: sk8_floris@hotmail.com
 * 
 * This is the Students Choice project for the Native Apps Studio course
 */

package nl.mprog.projects.studentschoice10419667;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;

import java.io.IOException;

public class MediaPlayerService extends Service implements MediaPlayer.OnPreparedListener,
        OnCompletionListener, MediaPlayer.OnErrorListener {
    public static final String ACTION_PLAY = "mprog.studentschoice10419667.action.PLAY";
    public static final String ACTION_PAUSE = "mprog.studentschoice10419667.action.PAUSE";
    public static final String ACTION_RESUME = "mprog.studentschoice10419667.action.RESUME";
    public static final String ACTION_START = "mprog.studentschoice10419667.action.START";
    public static final String ACTION_NOTHING = "mprog.studentschoice10419667.action.NOTHING";
    public static final String ACTION_NEXT = "mprog.studentschoice10419667.action.NEXT";
    public static final String ACTION_PREV = "mprog.studentschoice10419667.action.PREV";

    private final IBinder mBinder = new MediaPlayerBinder();

    private static String state;
    public static final String PLAYING = "mprog.PLAYING";
    public static final String PAUSED = "mprog.PAUSED";
    public static final String DONE = "mprog.DONE";

    private MediaPlayerCallback callback;

    private static String songPlayingName;
    private static String songPlayingArtist;
    private static int songId;
    private static int artistId;
    private static long playlistId;
    private static int playOrder;

    private static int from;

    private static String prevUri;
    private static String nextUri;
    private static int prevId;
    private static int nextId;
    private static int prevPlayOrder;
    private static int nextPlayOrder;

    MediaPlayer mMediaPlayer = null;

    @Override
    public void onCreate() {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            String action = intent.getAction();
        
            if (action.equals(ACTION_PLAY)) {
                String myUri = intent.getStringExtra(MainActivity.EXTRA_DATA_URI);
                songId = intent.getIntExtra(MainActivity.EXTRA_SONG_ID, -1);
                from = intent.getIntExtra(MainActivity.EXTRA_FROM, -1);
                
                switch (from) {
                    case MainActivity.SONGSTAB:
                        break;
                    case MainActivity.ARTISTSTAB:
                        artistId = intent.getIntExtra(MainActivity.EXTRA_ARTIST_ID, -1);
                        break;
                    case MainActivity.PLAYLISTSTAB:
                        playlistId = intent.getLongExtra(MainActivity.EXTRA_PLAYLIST_ID, 0);
                        playOrder = intent.getIntExtra(MainActivity.EXTRA_PLAY_ORDER, -1);
                        break;
                        
                }
    
                playNewSong(myUri);
    
            } else if (action.equals(ACTION_PAUSE)) {
                pauseSong();
            } else if (action.equals(ACTION_RESUME)) {
                resumeSong();
            } else if (action.equals(ACTION_NEXT)) {
                playNext();
            } else if (action.equals(ACTION_PREV)) {
                playPrev();
            } else if (action.equals(ACTION_START)) {
                startSong();
            } else if (action.equals(ACTION_NOTHING)) {
    
            }
        } catch (NullPointerException e) {
        }
        
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /** Called when MediaPlayer is ready */
    public void onPrepared(MediaPlayer player) {
        player.start();
        state = PLAYING;
        showNotification(true);
        if (callback != null) {
            callback.updateSongPlaying(songPlayingName, songPlayingArtist);
        }
    }


    public void onDestroy() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        }
        stopForeground(true);
        mMediaPlayer.release();
        mMediaPlayer = null;
        state = null;
        songPlayingName = null;
        songPlayingArtist = null;
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return true;
    }

    public void onCompletion(MediaPlayer player) {
        if (from != -1) {
            playNext();
        } else {
            state = DONE;
            stopForeground(true);
            if (callback != null) {
                callback.updateSongPlaying(songPlayingName, songPlayingArtist);
            }
        }
    }

    public void playNewSong(String uri) {

        mMediaPlayer.reset();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mMediaPlayer.setDataSource(uri);
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        setTitleAndArtist();

        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.prepareAsync(); // prepare async to not block main thread

        if (from != -1) {
            setPrevAndNextSong();
        }
    }

    public void playNext() {
        
        if (from != -1) {
            songId = nextId;
            if (from == MainActivity.PLAYLISTSTAB) {
                playOrder = nextPlayOrder;
            }
            playNewSong(nextUri);
        }

    }

    public void playPrev() {
        if (from != -1) {
            songId = prevId;
            if (from == MainActivity.PLAYLISTSTAB) {
                playOrder = prevPlayOrder;
            }
            playNewSong(prevUri);
        }
    }
    
    public void setPrevAndNextSong() {
        switch (from) {
            case MainActivity.SONGSTAB:
                setPrevAndNextSongAllSongs();
                break;
            case MainActivity.ARTISTSTAB:
                setPrevAndNextSongArtist();
                break;
            case MainActivity.PLAYLISTSTAB:
                setPrevAndNextSongPlaylist();
                break;
        }
    }
    
    public void setPrevAndNextSongAllSongs() {
        String[] projection = {
                MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA
        };

        Cursor cursor = getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                MediaStore.Audio.Media.TITLE);
        
        int count = cursor.getCount();
        int row = -1;
        for (int i = 0; i < count; i++) {
            cursor.moveToPosition(i);
            if (songId == cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID))) {
                row = i;
                break;
            }
        }

        if (row != -1) {
            // The default repeat mode used here is Repeat all, so after the last song comes the 
            // first song, and before the first comes the last.
            if (cursor.moveToPosition(row - 1)) {
            } else {
                cursor.moveToLast();
            }
            prevUri = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            prevId = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID));

            if (cursor.moveToPosition(row + 1)) {
            } else {
                cursor.moveToFirst();
            }
            nextUri = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            nextId = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
        }
    }

    public void setPrevAndNextSongArtist() {
        String[] projection = {
                MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST_ID, MediaStore.Audio.Media.DATA
        };
        String selection = MediaStore.Audio.Media.ARTIST_ID + " = " + artistId;

        Cursor cursor = getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                MediaStore.Audio.Media.TITLE);
    
        int count = cursor.getCount();
        int row = -1;
        for (int i = 0; i < count; i++) {
            cursor.moveToPosition(i);
            if (songId == cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID))) {
                row = i;
                break;
            }
        }

        if (row != -1) {
            // The default repeat mode used here is Repeat all, so after the last song comes the 
            // first song, and before the first comes the last.
            if (cursor.moveToPosition(row - 1)) {
            } else {
                cursor.moveToLast();
            }
            prevUri = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            prevId = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID));

            if (cursor.moveToPosition(row + 1)) {
            } else {
                cursor.moveToFirst();
            }
            nextUri = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            nextId = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
        }
    
    }
    
    public void setPrevAndNextSongPlaylist() {
        String[] projection = {
                MediaStore.Audio.Playlists.Members._ID,
                MediaStore.Audio.Playlists.Members.AUDIO_ID,
                MediaStore.Audio.Playlists.Members.PLAY_ORDER,
                MediaStore.Audio.Playlists.Members.DATA
        };

        Cursor cursor = getContentResolver().query(
                MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId),
                projection,
                null,
                null,
                MediaStore.Audio.Playlists.Members.PLAY_ORDER);

        int count = cursor.getCount();
        int row = -1;
        for (int i = 0; i < count; i++) {
            cursor.moveToPosition(i);
            // The check on PLAY_ORDER is needed because a song can be multiple times in the same
            // playlist
            if (playOrder == cursor.getInt(cursor.getColumnIndex(
                    MediaStore.Audio.Playlists.Members.PLAY_ORDER)) && songId == cursor.getInt(
                    cursor.getColumnIndex(MediaStore.Audio.Playlists.Members.AUDIO_ID))) {
                row = i;
                break;
            }

        }

        if (row != -1) {
            // The default playlist repeat mode used here is Repeat all, so after the last song
            // comes the first song, and before the first comes the last.
            if (cursor.moveToPosition(row - 1)) {
            } else {
                cursor.moveToLast();
            }
            prevUri = cursor.getString(cursor
                    .getColumnIndex(MediaStore.Audio.Playlists.Members.DATA));
            prevId = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.Audio.Playlists.Members.AUDIO_ID));
            prevPlayOrder = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.Audio.Playlists.Members.PLAY_ORDER));

            if (cursor.moveToPosition(row + 1)) {
            } else {
                cursor.moveToFirst();
            }
            nextUri = cursor.getString(cursor
                    .getColumnIndex(MediaStore.Audio.Playlists.Members.DATA));
            nextId = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.Audio.Playlists.Members.AUDIO_ID));
            nextPlayOrder = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.Audio.Playlists.Members.PLAY_ORDER));
        }

    }

    public void pauseSong() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            state = PAUSED;
            showNotification(false);
            if (callback != null) {
                callback.updateSongPlaying(songPlayingName, songPlayingArtist);
            }
        }
    }

    public void resumeSong() {
        mMediaPlayer.start();
        state = PLAYING;
        showNotification(true);
        if (callback != null) {
            callback.updateSongPlaying(songPlayingName, songPlayingArtist);
        }
    }

    public void startSong() {
        mMediaPlayer.start();
        state = PLAYING;
        showNotification(true);
        if (callback != null) {
            callback.updateSongPlaying(songPlayingName, songPlayingArtist);
        }
    }

    public void setTitleAndArtist() {
        String[] projection = {
                MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST
        };

        String selection = MediaStore.Audio.Media._ID + " = " + songId;

        Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                null);
        cursor.moveToFirst();
        songPlayingName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
        songPlayingArtist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));

        cursor.close();
    }

    public void showNotification(boolean playing) {
        String tickerText;
        String contentTitle;
        int iconId;
        if (playing) {
            contentTitle = getString(R.string.notification_title_playing);
            tickerText = contentTitle + "\n" + songPlayingName + "\n" + songPlayingArtist;
            iconId = R.drawable.ic_action_play;
        } else {
            contentTitle = getString(R.string.notification_title_paused);
            tickerText = contentTitle;
            iconId = R.drawable.ic_action_pause;
        }
        
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(iconId)
                .setContentTitle(contentTitle)
                .setContentText(songPlayingName)
                .setSubText(songPlayingArtist)
                .setTicker(tickerText)
                .setOngoing(true);

        Intent resultIntent = new Intent(this, MainActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(resultPendingIntent);
        startForeground(1, mBuilder.build());
    }

    public String getState() {
        if (state != null) {
            return state;
        } else {
            return "";
        }
    }

    public void requestUpdate() {
        if (callback != null) {
            callback.updateSongPlaying(songPlayingName, songPlayingArtist);
        }
    }

    public class MediaPlayerBinder extends Binder {
        MediaPlayerService getService() {
            return MediaPlayerService.this;
        }
    }

    public void setCallback(MediaPlayerCallback callback) {
        this.callback = callback;
    }

    public interface MediaPlayerCallback {
        public void updateSongPlaying(String title, String artist);
    }

}
