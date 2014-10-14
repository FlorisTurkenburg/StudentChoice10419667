
package nl.mprog.projects.studentschoice10419667;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;

public class CheckboxCursorAdapter extends SimpleCursorAdapter {

    private Cursor cursor;
    private Context context;
    private int count;
    private ArrayList<Boolean> itemChecked = new ArrayList<Boolean>();
    private int checkedCount;
    private UpdateSelectedCallback callback;
    private String whichList;

    public CheckboxCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to,
            int flag, String whichList) {
        super(context, layout, c, from, to, flag);
        this.cursor = c;
        this.context = context;
        this.count = this.getCount();
        this.whichList = whichList;

        for (int i = 0; i < count; i++) {
            itemChecked.add(i, false);
            checkedCount = 0;
        }
    }

    public View getView(final int pos, View view, ViewGroup parent) {
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.delete_song_list_row, null);
        }

        cursor.moveToPosition(pos);
        TextView title = (TextView) view.findViewById(R.id.song_title);
        TextView artist = (TextView) view.findViewById(R.id.song_artist);

        if (whichList.equals("PLAYLIST")) {
            title.setText(cursor.getString(cursor
                    .getColumnIndex(MediaStore.Audio.Playlists.Members.TITLE)));
            artist.setText(cursor.getString(cursor
                    .getColumnIndex(MediaStore.Audio.Playlists.Members.ARTIST)));
        } else if (whichList.equals("MEDIA")) {
            title.setText(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
            artist.setText(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
        }

        final CheckBox checkbox = (CheckBox) view.findViewById(R.id.delete_check);

        checkbox.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                CheckBox cb = (CheckBox) v.findViewById(R.id.delete_check);

                if (cb.isChecked()) {
                    itemChecked.set(pos, true);
                    checkedCount++;

                } else if (!cb.isChecked()) {
                    itemChecked.set(pos, false);
                    checkedCount--;
                }
                if (callback != null) {
                    callback.refreshCount();
                }
            }
        });

        checkbox.setChecked(itemChecked.get(pos));

        return (view);
    }

    public ArrayList<Boolean> getSelection() {
        return itemChecked;
    }

    public int getCheckedCount() {
        return checkedCount;
    }

    public void setCheckedAll(boolean checked) {
        for (int i = 0; i < count; i++) {
            itemChecked.set(i, checked);
        }
        if (checked) {
            checkedCount = count;
        } else {
            checkedCount = 0;
        }

        if (callback != null) {
            callback.refreshCount();
        }
    }

    public void setCallback(UpdateSelectedCallback callback) {
        this.callback = callback;
    }

    public interface UpdateSelectedCallback {
        public void refreshCount();
    }

}
