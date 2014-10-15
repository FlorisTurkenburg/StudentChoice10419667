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

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import android.widget.Toast;

import nl.mprog.projects.studentschoice10419667.MediaPlayerService.MediaPlayerBinder;
import nl.mprog.projects.studentschoice10419667.MediaPlayerService.MediaPlayerCallback;

public class PlaylistContentsActivity extends ActionBarActivity implements MediaPlayerCallback {
    public static String PlaylistName;
    public static int playlist_id;
    public static long playlist_id2;
    public static SimpleCursorAdapter adapter;

    MediaPlayerService mService;
    boolean mBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist_contents);

        Intent intent = getIntent();
        playlist_id = intent.getIntExtra(PlaylistFragment.EXTRA_PLAYLIST_ID, -1);

        String[] fromColumns = {
                MediaStore.Audio.Playlists.Members.TITLE,
                MediaStore.Audio.Playlists.Members.ARTIST
        };

        int[] toViews = {
                R.id.song_title,
                R.id.song_artist
        };

        Cursor cursor = getPlaylistContentCursor();

        adapter = new SimpleCursorAdapter(this, R.layout.song_list_row, cursor,
                fromColumns, toViews, 0);
        ListView songList = (ListView) findViewById(R.id.playlist_songs_listview);
        songList.setAdapter(adapter);

        songList.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView parent, View v, int position, long id) {
                Cursor cursor = adapter.getCursor();
                cursor.moveToPosition(position);
                Toast.makeText(PlaylistContentsActivity.this,
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)),
                        Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(PlaylistContentsActivity.this, MediaPlayerService.class);
                intent.putExtra(MainActivity.EXTRA_DATA_URI, cursor.getString(cursor
                        .getColumnIndex(MediaStore.Audio.Playlists.Members.DATA)));
                intent.putExtra(MainActivity.EXTRA_SONG_ID, cursor.getInt(cursor
                        .getColumnIndex(MediaStore.Audio.Playlists.Members.AUDIO_ID)));
                intent.putExtra(MainActivity.EXTRA_PLAYLIST_ID, playlist_id2);
                intent.putExtra(MainActivity.EXTRA_PLAY_ORDER, cursor.getInt(cursor
                        .getColumnIndex(MediaStore.Audio.Playlists.Members.PLAY_ORDER)));
                intent.setAction(MediaPlayerService.ACTION_PLAY);
                startService(intent);
            }
        });

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

    @Override
    protected void onResume() {
        super.onResume();
        adapter.changeCursor(getPlaylistContentCursor());
        adapter.notifyDataSetChanged();
    }

    public Cursor getPlaylistContentCursor() {

        String[] projection = {
                MediaStore.Audio.Playlists._ID,
                MediaStore.Audio.Playlists.NAME
        };

        String selection = MediaStore.Audio.Playlists._ID + " = " + playlist_id;

        Cursor cursor = getContentResolver().query(
                MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                null);

        cursor.moveToFirst();
        playlist_id2 = cursor.getLong(cursor.getColumnIndex("_id"));
        PlaylistName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Playlists.NAME));
        setTitle(PlaylistName);

        if (playlist_id2 > 0) {

            String[] projection2 = {
                    MediaStore.Audio.Playlists.Members._ID,
                    MediaStore.Audio.Playlists.Members.AUDIO_ID,
                    MediaStore.Audio.Playlists.Members.TITLE,
                    MediaStore.Audio.Playlists.Members.ARTIST,
                    MediaStore.Audio.Playlists.Members.PLAY_ORDER,
                    MediaStore.Audio.Playlists.Members.DATA
            };

            cursor = null;
            cursor = getContentResolver().query(
                    MediaStore.Audio.Playlists.Members.getContentUri("external", playlist_id2),
                    projection2,
                    null,
                    null,
                    null);
        }

        return cursor;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.playlist_contents, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.edit_title) {
            changePlaylistName();
            return true;
        } else if (id == R.id.remove_playlist) {
            removePlaylist();
            return true;
        } else if (id == R.id.delete_songs) {
            Intent intent = new Intent(this, PlaylistContentsDeleteActivity.class);
            intent.putExtra(PlaylistFragment.EXTRA_PLAYLIST_ID, playlist_id);
            startActivity(intent);
        } else if (id == R.id.add_songs) {
            Intent intent = new Intent(this, PlaylistContentsAddActivity.class);
            intent.putExtra(PlaylistFragment.EXTRA_PLAYLIST_ID, playlist_id);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    public void changePlaylistName() {
        AlertDialog.Builder builder = new AlertDialog.Builder(PlaylistContentsActivity.this);
        LayoutInflater inflater = PlaylistContentsActivity.this.getLayoutInflater();

        builder.setTitle(R.string.edit_title).setIcon(R.drawable.ic_action_edit);

        View layout = inflater.inflate(R.layout.edit_title_dialog, null);

        builder.setView(layout);

        final EditText editText = (EditText) layout.findViewById(R.id.edit_title_text);
        editText.setText(PlaylistName, BufferType.EDITABLE);

        builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newName = editText.getText().toString();
                setNewName(newName);

            }
        })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User canceled the dialog.

                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void setNewName(String newName) {
        boolean existing = checkIfNameExists(newName);

        if (!existing) {
            ContentValues values = new ContentValues(1);
            values.put(MediaStore.Audio.Playlists.NAME, newName);
            String selection = MediaStore.Audio.Playlists._ID + " = " + playlist_id;

            getContentResolver().update(
                    MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                    values,
                    selection,
                    null);

            setTitle(newName);
        } else {
            Toast.makeText(getApplicationContext(), "This name already exists!", Toast.LENGTH_LONG)
                    .show();
        }

    }

    // If the new name already exists, this method returns true
    public boolean checkIfNameExists(String name) {
        String[] projection = {
                MediaStore.Audio.Playlists._ID,
                MediaStore.Audio.Playlists.NAME
        };

        String selection = MediaStore.Audio.Playlists.NAME + " = ? ";
        String[] selectionArgs = {
                name
        };

        Cursor cursor = getContentResolver().query(
                MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null);

        // id is the ID of the playlist that is found to have the same name as the new name, if no
        // playlist if found for this name, id = -1
        int id = -1;

        if (cursor != null) {
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                id = cursor.getInt(0);
            }
        }
        cursor.close();

        if (id == -1) {
            return false;
        } else {
            return true;
        }

    }

    // Display a confirmation dialog
    public void removePlaylist() {
        AlertDialog.Builder builder = new AlertDialog.Builder(PlaylistContentsActivity.this);

        builder.setTitle(R.string.delete_playlist).setIcon(R.drawable.ic_action_discard);
        builder.setMessage("Are you sure you want to delete:\n" + PlaylistName);

        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                deletePlaylist();
                finish();

            }
        })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User canceled the dialog.

                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void deletePlaylist() {
        String selection = MediaStore.Audio.Playlists._ID + " = " + playlist_id;
        getContentResolver().delete(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, selection,
                null);

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
            mService.setCallback(PlaylistContentsActivity.this);

            // This will succeed if the service is re-connected, e.g. when configurations change.
            // This block will make sure the "player" view contains the correct information.
            try {
                String state = mService.getState();
                ImageButton button = (ImageButton) PlaylistContentsActivity.this
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
