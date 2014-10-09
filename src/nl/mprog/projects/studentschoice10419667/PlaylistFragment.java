
package nl.mprog.projects.studentschoice10419667;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_playlist, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

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
}
