
package nl.mprog.projects.studentschoice10419667;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import nl.mprog.projects.studentschoice10419667.MediaPlayerService.MediaPlayerBinder;
import nl.mprog.projects.studentschoice10419667.MediaPlayerService.MediaPlayerCallback;

import java.util.Locale;

public class MainActivity extends ActionBarActivity implements ActionBar.TabListener,
        MediaPlayerCallback {
    public static final String EXTRA_TAB = "studentschoice10419667.lastOpenedTab";
    public static final String EXTRA_DATA_URI = "studentschoice10419667.dataUri";
    public static final String EXTRA_SONG_ID = "studentschoice10419667.songId";
    public static final String EXTRA_PLAYLIST_ID = "studentschoice10419667.playlistId";
    public static final String EXTRA_PLAY_ORDER = "studentschoice10419667.playOrder";
    
    public static final int SONGSTAB = 0;
    public static final int ARTISTSTAB = 1;
    public static final int PLAYLISTSTAB = 2;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the
     * sections. We use a {@link FragmentPagerAdapter} derivative, which will keep every loaded
     * fragment in memory. If this becomes too memory intensive, it may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    MediaPlayerService mService;
    boolean mBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }

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
    protected void onDestroy() {
        super.onDestroy();
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
        
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSectionsPagerAdapter.notifyDataSetChanged();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.close_app) {
            if (mBound) {
                unbindService(mConnection);
                mBound = false;
            }
            stopService(new Intent(this, MediaPlayerService.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the
     * sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position) {
                case SONGSTAB:
                    return SongFragment.newInstance(position + 1);
                case ARTISTSTAB:
                    return ArtistFragment.newInstance(position + 1);
                case PLAYLISTSTAB:
                    return PlaylistFragment.newInstance(position + 1);
                default:
                    return PlaceholderFragment.newInstance(position + 1);
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case SONGSTAB:
                    return getString(R.string.tab1).toUpperCase(l);
                case ARTISTSTAB:
                    return getString(R.string.tab2).toUpperCase(l);
                case PLAYLISTSTAB:
                    return getString(R.string.tab3).toUpperCase(l);
            }
            return null;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
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
                return;
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
            mService.setCallback(MainActivity.this);

            // This will succeed if the service is re-connected, e.g. when configurations change.
            // This block will make sure the "player" view contains the correct information.
            try {
                String state = mService.getState();
                ImageButton button = (ImageButton) MainActivity.this.findViewById(R.id.play_button);
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
