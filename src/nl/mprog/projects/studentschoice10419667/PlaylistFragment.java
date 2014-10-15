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
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

/**
 * A placeholder fragment containing the playlist view.
 */
public class PlaylistFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    public static final String EXTRA_PLAYLIST_ID = "chosen_playlist_id";

    /**
     * Returns a new instance of this fragment for the given section number.
     */
    public static PlaylistFragment newInstance(int sectionNumber) {
        PlaylistFragment fragment = new PlaylistFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public PlaylistFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.playlist, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.add_playlist) {
            createPlaylistDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_playlist, container, false);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        String[] fromColumns = {
                MediaStore.Audio.Playlists.NAME
        };

        int[] toViews = {
                R.id.item
        };

        String[] projection = {
                MediaStore.Audio.Playlists._ID, MediaStore.Audio.Playlists.NAME
        };

        final Cursor cursor = getActivity().getContentResolver().query(
                MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                MediaStore.Audio.Playlists.NAME);

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(),
                R.layout.single_item_row, cursor, fromColumns, toViews, 0);

        ListView playlistList = (ListView) getActivity().findViewById(R.id.playlist_listview);
        playlistList.setAdapter(adapter);

        final OnItemClickListener playlistClickedHandler = new OnItemClickListener() {
            public void onItemClick(AdapterView parent, View v, int position, long id) {

                cursor.moveToPosition(position);
                int playlistId = cursor.getInt(cursor.getColumnIndex("_id"));

                Intent intent = new Intent(getActivity(), PlaylistContentsActivity.class);
                intent.putExtra(EXTRA_PLAYLIST_ID, playlistId);
                startActivity(intent);

            }
        };

        playlistList.setOnItemClickListener(playlistClickedHandler);

    }

    public void createPlaylistDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        builder.setTitle(R.string.new_playlist).setIcon(R.drawable.ic_action_edit);

        View layout = inflater.inflate(R.layout.edit_title_dialog, null);

        builder.setView(layout);

        final EditText editText = (EditText) layout.findViewById(R.id.edit_title_text);

        builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newName = editText.getText().toString();
                createNewPlaylist(newName);

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

    public void createNewPlaylist(String newName) {
        boolean existing = checkIfNameExists(newName);

        if (!existing) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Audio.Playlists.NAME, newName);
            values.put(MediaStore.Audio.Playlists.DATE_ADDED, System.currentTimeMillis());
            values.put(MediaStore.Audio.Playlists.DATE_MODIFIED, System.currentTimeMillis());

            Uri uri = getActivity().getContentResolver().insert(
                    MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, values);

            if (uri != null) {
                int playlistId = -1;
                Cursor cursor = getActivity().getContentResolver().query(
                        uri,
                        new String[] {
                            MediaStore.Audio.Playlists._ID
                        },
                        null,
                        null,
                        null);

                if (cursor != null) {
                    cursor.moveToFirst();
                    playlistId = cursor.getInt(
                            cursor.getColumnIndex(MediaStore.Audio.Playlists._ID));

                    Intent intent = new Intent(getActivity(), PlaylistContentsActivity.class);
                    intent.putExtra(EXTRA_PLAYLIST_ID, playlistId);
                    startActivity(intent);
                }
            }

        } else {
            Toast.makeText(getActivity().getApplicationContext(), "This name already exists!",
                    Toast.LENGTH_LONG).show();
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

        Cursor cursor = getActivity().getContentResolver().query(
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

}
