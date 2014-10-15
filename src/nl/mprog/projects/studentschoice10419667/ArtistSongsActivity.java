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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import nl.mprog.projects.studentschoice10419667.MediaPlayerService.MediaPlayerBinder;
import nl.mprog.projects.studentschoice10419667.MediaPlayerService.MediaPlayerCallback;

public class ArtistSongsActivity extends ActionBarActivity implements MediaPlayerCallback {

    MediaPlayerService mService;
    boolean mBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_songs);

        Intent intent = getIntent();
        final int artist_id = intent.getIntExtra(ArtistFragment.EXTRA_ARTIST_ID, -1);

        String[] fromColumns = {
                MediaStore.Audio.Media.TITLE
        };

        int[] toViews = {
                R.id.item
        };

        String[] projection = {
                MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST_ID, MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DATA
        };
        String selection = MediaStore.Audio.Media.ARTIST_ID + " = " + artist_id;

        final Cursor cursor = getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                MediaStore.Audio.Media.TITLE);

        cursor.moveToFirst();
        setTitle(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.single_item_row,
                cursor, fromColumns, toViews, 0);
        ListView songList = (ListView) findViewById(R.id.artist_songs_listview);
        songList.setAdapter(adapter);

        final OnItemClickListener songClickedHandler = new OnItemClickListener() {
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                cursor.moveToPosition(position);
                Toast.makeText(ArtistSongsActivity.this,
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)),
                        Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(ArtistSongsActivity.this, MediaPlayerService.class);
                intent.putExtra(MainActivity.EXTRA_DATA_URI,
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
                intent.putExtra(MainActivity.EXTRA_SONG_ID,
                        cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));
                intent.setAction(MediaPlayerService.ACTION_PLAY);
                startService(intent);
            }
        };

        songList.setOnItemClickListener(songClickedHandler);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.artist_songs, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = new Intent(this, MediaPlayerService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    public void playButton(View view) {

        if (mBound) {
            ImageButton button = (ImageButton) view.findViewById(R.id.play_button);
            Intent intent = new Intent(this, MediaPlayerService.class);
            String state = mService.getState();

            if (state.equals(MediaPlayerService.PLAYING)) {
                button.setImageResource(R.drawable.ic_action_play);
                intent.setAction(MediaPlayerService.ACTION_PAUSE);
            } else if (state.equals(MediaPlayerService.PAUSED)) {
                button.setImageResource(R.drawable.ic_action_pause);
                intent.setAction(MediaPlayerService.ACTION_RESUME);
            } else if (state.equals(MediaPlayerService.DONE)) {
                button.setImageResource(R.drawable.ic_action_pause);
                intent.setAction(MediaPlayerService.ACTION_START);
            } else {
                intent.setAction(MediaPlayerService.ACTION_NOTHING);
            }
            startService(intent);
        }
    }
    
    public void nextButton(View view) {
        if (mBound) {
            Intent intent = new Intent(this, MediaPlayerService.class);
            intent.setAction(MediaPlayerService.ACTION_NEXT);
            startService(intent);
        }
    }
    
    public void prevButton(View view) {
        if (mBound) {
            Intent intent = new Intent(this, MediaPlayerService.class);
            intent.setAction(MediaPlayerService.ACTION_PREV);
            startService(intent);
        }
    }

    public void UpdateSongPlaying(String title, String artist) {
        TextView titleText = (TextView) findViewById(R.id.player_song);
        titleText.setText(title);
        titleText.setSelected(true); // This will scroll the text if it is more than 1 line
        TextView artistText = (TextView) findViewById(R.id.player_artist);
        artistText.setText(artist);
        if (mBound) {
            String state = mService.getState();
            ImageButton button = (ImageButton) findViewById(R.id.play_button);
            if (state.equals(MediaPlayerService.PLAYING)) {
                button.setImageResource(R.drawable.ic_action_pause);
            } else if (state.equals(MediaPlayerService.PAUSED)) {
                button.setImageResource(R.drawable.ic_action_play);
            } else if (state.equals(MediaPlayerService.DONE)) {
                button.setImageResource(R.drawable.ic_action_play);
            }
        }

    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            MediaPlayerBinder binder = (MediaPlayerBinder) service;
            mService = binder.getService();
            mBound = true;
            mService.setCallback(ArtistSongsActivity.this);

            // This will succeed if the service is re-connected, e.g. when configurations change.
            // This block will make sure the "player" view contains the correct information.
            try {
                String state = mService.getState();
                ImageButton button = (ImageButton) ArtistSongsActivity.this
                        .findViewById(R.id.play_button);
                if (state.equals(MediaPlayerService.PLAYING)) {
                    button.setImageResource(R.drawable.ic_action_pause);
                } else if (state.equals(MediaPlayerService.PAUSED)) {
                    button.setImageResource(R.drawable.ic_action_play);
                }
                mService.requestUpdate();
            } catch (Exception e) {

            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
}
