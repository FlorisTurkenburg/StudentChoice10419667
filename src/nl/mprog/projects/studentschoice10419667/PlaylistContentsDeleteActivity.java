
package nl.mprog.projects.studentschoice10419667;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBarActivity;
import android.widget.ListView;

public class PlaylistContentsDeleteActivity extends ActionBarActivity {
    public static String PlaylistName;
    public static int playlist_id;

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
        long playlist_id2 = cursor.getLong(cursor.getColumnIndex("_id"));
        PlaylistName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Playlists.NAME));
        setTitle(PlaylistName);

        if (playlist_id2 > 0) {

            String[] projection2 = {
                    MediaStore.Audio.Playlists.Members._ID,
                    MediaStore.Audio.Playlists.Members.AUDIO_ID,
                    MediaStore.Audio.Playlists.Members.TITLE,
                    MediaStore.Audio.Playlists.Members.ARTIST
            };

            cursor = null;
            cursor = getContentResolver().query(
                    MediaStore.Audio.Playlists.Members.getContentUri("external", playlist_id2),
                    projection2,
                    null,
                    null,
                    null);
        }

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.delete_song_list_row, cursor,
                fromColumns, toViews, 0);
        ListView songList = (ListView) findViewById(R.id.playlist_songs_listview);
        songList.setAdapter(adapter);
    }


}