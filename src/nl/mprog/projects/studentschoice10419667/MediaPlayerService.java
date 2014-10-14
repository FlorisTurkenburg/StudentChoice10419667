
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
    public static final String ACTION_NOTHING = "mprog.studentschoice10419667.action.NOTHING";
    private final IBinder mBinder = new MediaPlayerBinder();

    private static String state;
    public static final String PLAYING = "mprog.PLAYING";
    public static final String PAUSED = "mprog.PAUSED";

    private MediaPlayerCallback callback;

    private static String songPlayingName;
    private static String songPlayingArtist;

    MediaPlayer mMediaPlayer = null;

    @Override
    public void onCreate() {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (action.equals(ACTION_PLAY)) {
            String myUri = intent.getStringExtra(MainActivity.EXTRA_DATA_URI);
            int songId = intent.getIntExtra(MainActivity.EXTRA_SONG_ID, -1);
            playNewSong(myUri, songId);

        } else if (action.equals(ACTION_PAUSE)) {
            pauseSong();
        } else if (action.equals(ACTION_RESUME)) {
            resumeSong();
        } else if (action.equals(ACTION_NOTHING)) {

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
        showNotification();
        if (callback != null) {
            callback.UpdateSongPlaying(songPlayingName, songPlayingArtist);
        }
    }

    public void onStop() {

    }

    public void onDestroy() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        }
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
        stopSelf();
    }

    public void playNewSong(String uri, int songId) {

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

        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.prepareAsync(); // prepare async to not block main thread
    }

    public void pauseSong() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            state = PAUSED;
            
            if (callback != null) {
                callback.UpdateSongPlaying(songPlayingName, songPlayingArtist);
            }
        }
    }

    public void resumeSong() {
        mMediaPlayer.start();
        state = PLAYING;
    }
    
    public void showNotification() {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_action_play)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(songPlayingName)
                .setSubText(songPlayingArtist)
                .setTicker(getString(R.string.notification_title) + "\n" + songPlayingName + "\n" + 
                        songPlayingArtist)
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
            callback.UpdateSongPlaying(songPlayingName, songPlayingArtist);
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
        public void UpdateSongPlaying(String title, String artist);
    }
    
    
   
}
