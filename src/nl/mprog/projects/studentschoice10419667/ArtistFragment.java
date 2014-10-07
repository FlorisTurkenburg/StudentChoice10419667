package nl.mprog.projects.studentschoice10419667;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;


/**
 * A placeholder fragment containing a simple view.
 */
public class ArtistFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

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
        
        String[] fromColumns = {MediaStore.Audio.Artists.ARTIST};
        int[] toViews = {R.id.artist};
        
        
        String[] projection = {"DISTINCT " + MediaStore.Audio.Media.ARTIST_ID + " as _id", MediaStore.Audio.Artists.ARTIST};
        Cursor cursor = getActivity().getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                MediaStore.Audio.Artists.ARTIST);
        
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(),R.layout.artist_list_row,cursor,fromColumns,toViews,0);
        
        ListView contactsList = (ListView) getActivity().findViewById(R.id.artist_listview);
        contactsList.setAdapter(adapter);
        
    }
}