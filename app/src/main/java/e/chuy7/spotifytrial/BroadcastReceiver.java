package e.chuy7.spotifytrial;

import android.content.Context;
import android.content.Intent;


public class BroadcastReceiver extends android.content.BroadcastReceiver {
    static final class BroadcastTypes{
        static final String SPOTIFY_PACKAGE = "com.spotify.music";
        static final String PLAYBACK_STATE_CHANGED = SPOTIFY_PACKAGE + ".playbackstatechanged";
        static final String QUEUE_CHANGED = SPOTIFY_PACKAGE + ".queuechanged";
        static final String METADATA_CHANGED = SPOTIFY_PACKAGE + ".metadatachanged";
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if(action.equals(BroadcastTypes.METADATA_CHANGED)){
            String trackName = intent.getStringExtra("track");
            String artist = intent.getStringExtra("artist");
            String albumName = intent.getStringExtra("album");
            String trackID = intent.getStringExtra("id");

        }
    }


}
