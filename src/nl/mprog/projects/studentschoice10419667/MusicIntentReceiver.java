package nl.mprog.projects.studentschoice10419667;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MusicIntentReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
            Intent signal = new Intent(context, MediaPlayerService.class);
            signal.setAction(MediaPlayerService.ACTION_PAUSE);
            context.startService(signal);
        }
    }

}
