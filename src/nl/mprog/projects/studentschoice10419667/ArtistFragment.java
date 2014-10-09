
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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

/**
 * A placeholder fragment containing the artist list view.
 */
public class ArtistFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    public static final String EXTRA_ARTIST_ID = "chosen_artist_id";

    /**
     * Returns a new instance of this fragment for the given section number.
     */
    public static ArtistFragment newInstance(int sectionNumber) {
        ArtistFragment fragment = new ArtistFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public ArtistFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_artist, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        String[] fromColumns = {
            MediaStore.Audio.Artists.ARTIST
        };
        int[] toViews = {
            R.id.item
        };

        String[] projection = {
                "DISTINCT " + MediaStore.Audio.Media.ARTIST_ID + " as _id",
                MediaStore.Audio.Artists.ARTIST
        };
        final Cursor cursor = getActivity().getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                MediaStore.Audio.Artists.ARTIST);

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(),
                R.layout.single_item_row, cursor, fromColumns, toViews, 0);

        ListView artistList = (ListView) getActivity().findViewById(R.id.artist_listview);
        artistList.setAdapter(adapter);

        final OnItemClickListener artistClickedHandler = new OnItemClickListener() {
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                
                cursor.moveToPosition(position);
                int artistId = cursor.getInt(cursor.getColumnIndex("_id"));
                
                Intent intent = new Intent(getActivity(), ArtistSongsActivity.class);
                intent.putExtra(EXTRA_ARTIST_ID, artistId);
                startActivity(intent);
                
                
            }
        };
        
        artistList.setOnItemClickListener(artistClickedHandler);
    }
}
