
package nl.mprog.projects.studentschoice10419667;

import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

public class PlaylistContentsActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist_contents);

        Intent intent = getIntent();
        final int playlist_id = intent.getIntExtra(PlaylistFragment.EXTRA_PLAYLIST_ID, -1);

        String[] fromColumns = {
                MediaStore.Audio.Playlists.Members.TITLE, 
                MediaStore.Audio.Playlists.Members.ARTIST
        };

        int[] toViews = {
                R.id.song_title, 
                R.id.song_artist
        };

        String[] projection = {
                MediaStore.Audio.Playlists._ID
        };
        
        String selection = MediaStore.Audio.Playlists._ID + " = " + playlist_id;


        Cursor cursor = getContentResolver().query(
                MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                null);
        
        cursor.moveToFirst();
        long playlist_id2 = cursor.getLong(0);

        if (playlist_id2 > 0) {
            
            String[] projection2 = {
                    MediaStore.Audio.Playlists.Members._ID,
                    MediaStore.Audio.Playlists.Members.AUDIO_ID,
                    MediaStore.Audio.Playlists.Members.TITLE,
                    MediaStore.Audio.Playlists.Members.ARTIST
            };
            
            cursor = null;
            cursor = getContentResolver().query(
                    MediaStore.Audio.Playlists.Members.getContentUri("external",playlist_id2), 
                    projection2, 
                    null, 
                    null, 
                    null);
        }

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.song_list_row, cursor,
                fromColumns, toViews, 0);
        ListView songList = (ListView) findViewById(R.id.playlist_songs_listview);
        songList.setAdapter(adapter);
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
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
