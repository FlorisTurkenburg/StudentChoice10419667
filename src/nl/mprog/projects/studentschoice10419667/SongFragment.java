
package nl.mprog.projects.studentschoice10419667;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

/**
 * A placeholder fragment containing the Song list view.
 */
public class SongFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    /**
     * Returns a new instance of this fragment for the given section number.
     */
    public static SongFragment newInstance(int sectionNumber) {
        SongFragment fragment = new SongFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public SongFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_song, container, false);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

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

        final Cursor cursor = getActivity().getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                MediaStore.Audio.Media.TITLE);

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(),
                R.layout.song_list_row, cursor, fromColumns, toViews, 0);

        ListView songsList = (ListView) getActivity().findViewById(R.id.song_listview);
        songsList.setAdapter(adapter);

        final OnItemClickListener songClickedHandler = new OnItemClickListener() {
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                cursor.moveToPosition(position);
                Toast.makeText(getActivity(),
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)),
                        Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(getActivity(), MediaPlayerService.class);
                intent.putExtra(MainActivity.EXTRA_DATA_URI,
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
                intent.putExtra(MainActivity.EXTRA_SONG_ID,
                        cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));
                intent.setAction(MediaPlayerService.ACTION_PLAY);
                getActivity().startService(intent);
            }
        };

        songsList.setOnItemClickListener(songClickedHandler);
    }
}
