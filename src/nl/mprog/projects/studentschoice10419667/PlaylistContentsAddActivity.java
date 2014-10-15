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

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ListView;

import nl.mprog.projects.studentschoice10419667.CheckboxCursorAdapter.UpdateSelectedCallback;

import java.util.ArrayList;
import java.util.List;

public class PlaylistContentsAddActivity extends ActionBarActivity implements
        UpdateSelectedCallback {
    public static String PlaylistName;
    public static int playlist_id;
    public static long playlist_id2;
    public static int numOfSongs;
    private Cursor cursor;
    CheckboxCursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist_contents_add);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(false);

        Intent intent = getIntent();
        playlist_id = intent.getIntExtra(PlaylistFragment.EXTRA_PLAYLIST_ID, -1);

        String[] fromColumns = {
                MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST
        };

        int[] toViews = {
                R.id.song_title, R.id.song_artist
        };

        String[] projection = {
                MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.DATA
        };

        cursor = getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                MediaStore.Audio.Media.TITLE);

        adapter = new CheckboxCursorAdapter(this, R.layout.delete_song_list_row, cursor,
                fromColumns, toViews, 0, "MEDIA");
        ListView songList = (ListView) findViewById(R.id.playlist_songs_add_listview);
        adapter.setCallback(this);
        songList.setAdapter(adapter);
        setTitle(adapter.getCheckedCount() + " " + getString(R.string.selected));

        numOfSongs = adapter.getCount();

    }

    public void refreshCount() {
        setTitle(adapter.getCheckedCount() + " " + getString(R.string.selected));
        CheckBox selectAll = (CheckBox) findViewById(R.id.select_all);
        if (adapter.getCheckedCount() == numOfSongs) {
            selectAll.setChecked(true);
        } else {
            selectAll.setChecked(false);
        }

    }

    public void selectAllToggle(View view) {
        boolean checked = ((CheckBox) view).isChecked();

        if (checked) {
            adapter.setCheckedAll(true);
        } else {
            adapter.setCheckedAll(false);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.playlist_contents_add, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.save_add) {
            addSongsToPlaylist();
            return true;
        } else if (id == R.id.cancel_add) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void addSongsToPlaylist() {
        ArrayList<Boolean> checked = adapter.getSelection();
        int size = checked.size();
        List<Integer> addPos = new ArrayList<Integer>();

        setPlaylistID();

        for (int i = 0; i < size; i++) {
            if (checked.get(i)) {
                addPos.add(i);
            }
        }

        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlist_id2);
        // Get the highest PLAY_ORDER
        Cursor tempCursor = getContentResolver().query(
                uri,
                new String[] {
                    MediaStore.Audio.Playlists.Members.PLAY_ORDER
                },
                null,
                null,
                null);

        int base = 0;
        if (tempCursor.moveToLast()) {
            base = tempCursor.getInt(0) + 1;
        }
        tempCursor.close();
        ContentValues[] values = new ContentValues[addPos.size()];

        for (int i = 0; i < addPos.size(); i++) {
            cursor.moveToPosition(addPos.get(i));

            ContentValues value = new ContentValues();
            value.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, Integer.valueOf(base + i));
            value.put(MediaStore.Audio.Playlists.Members.AUDIO_ID,
                    cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));
            value.put(MediaStore.Audio.Playlists.Members.TITLE,
                    cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
            value.put(MediaStore.Audio.Playlists.Members.ARTIST,
                    cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
            values[i] = value;

        }
        getContentResolver().bulkInsert(uri, values);

        finish();

    }

    public void setPlaylistID() {

        String[] projection = {
                MediaStore.Audio.Playlists._ID,
                MediaStore.Audio.Playlists.NAME
        };

        String selection = MediaStore.Audio.Playlists._ID + " = " + playlist_id;

        Cursor c = getContentResolver().query(
                MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                null);

        c.moveToFirst();
        playlist_id2 = c.getLong(c.getColumnIndex("_id"));
    }

}
