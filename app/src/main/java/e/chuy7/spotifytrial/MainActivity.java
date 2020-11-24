package e.chuy7.spotifytrial;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.types.PlayerContext;
import com.spotify.protocol.types.Track;


public class MainActivity extends AppCompatActivity{
    static final class BroadcastTypes{
        static final String SPOTIFY_PACKAGE = "com.spotify.music";
        static final String PLAYBACK_STATE_CHANGED = SPOTIFY_PACKAGE + ".playbackstatechanged";
        static final String QUEUE_CHANGED = SPOTIFY_PACKAGE + ".queuechanged";
        static final String METADATA_CHANGED = SPOTIFY_PACKAGE + ".metadatachanged";
    }

    private static final String CLIENT_ID = "";
    private static final String REDIRECT_URI = "simplespotifyintegration://callback";
    private SpotifyAppRemote mSpotifyAppRemote;
    private TextView current_artist,current_album,current_song;
    private ImageView imageView;
    private ConstraintLayout layout;
    Button play, next, previous, repeat, shuffle, Lyrics;
    private int backgroundColor, textColor;

    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().hide();
        int c = getResources().getConfiguration().orientation;
        if (c == Configuration.ORIENTATION_LANDSCAPE) {
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        current_artist = findViewById(R.id.ArtistPlaying);
        current_album = findViewById(R.id.AlbumPlaying);
        current_song = findViewById(R.id.SongPlaying);
        play = findViewById(R.id.play_button);
        next = findViewById(R.id.next_button);
        previous = findViewById(R.id.previous_button);
        repeat = findViewById(R.id.repeat_Switch);
        shuffle = findViewById(R.id.shuffle_Switch);
        imageView = findViewById(R.id.imageView);
        layout = findViewById(R.id.whole_Layout);
        Lyrics = findViewById(R.id.lyricsTotal);
        findViewById(R.id.ArtistPlaying).setSelected(true);
        findViewById(R.id.AlbumPlaying).setSelected(true);
        findViewById(R.id.SongPlaying).setSelected(true);
    }

    protected void onStart(){
        super.onStart();
        final int imageSize = (int) getResources().getDimension(R.dimen.image_size);
        ConnectionParams connectionParams =
                new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .setPreferredImageSize(imageSize)
                        .showAuthView(true)
                        .build();

        SpotifyAppRemote.CONNECTOR.connect(this, connectionParams,
                new Connector.ConnectionListener() {

                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        mSpotifyAppRemote = spotifyAppRemote;
                        Log.d("MainActivity", "Connected! Yay!");

                        connected();
                        new api_Calls();
                    }


                    public void onFailure(Throwable throwable) {
                        Log.e("MyActivity", throwable.getMessage(), throwable);
                    }
                });
    }


    private void connected() {

        mSpotifyAppRemote.getPlayerApi().resume();;
        play.setText("Pause");
        // Subscribe to PlayerState
        mSpotifyAppRemote.getPlayerApi()
                .subscribeToPlayerState().setEventCallback(playerState -> {
            final Track track = playerState.track;
            if (track != null) {
                current_artist.setText(track.artist.name);
                current_album.setText(track.album.name);
                current_song.setText(track.name);
                mSpotifyAppRemote.getImagesApi().getImage(playerState.track.imageUri).setResultCallback(bitmap -> imageView.setImageBitmap(bitmap));
                mSpotifyAppRemote.getImagesApi().getImage(playerState.track.imageUri).setResultCallback(this::createPaletteAsync);
            }



            if(playerState.playbackOptions.isShuffling){
                shuffle.setText("Shuffling On");
            }
            else{
                shuffle.setText("Shuffling Off");
            }

            if(playerState.playbackOptions.repeatMode == 0){
                repeat.setText("Repeating Off");
            }else if (playerState.playbackOptions.repeatMode == 1){
                repeat.setText("Repeating Song");
            }else if(playerState.playbackOptions.repeatMode == 2){
                repeat.setText("Repeating All");
            }

            next.setOnClickListener(view -> {
                mSpotifyAppRemote.getPlayerApi().skipNext();
                play.setText("Pause");
            });

            previous.setOnClickListener(view -> {
                mSpotifyAppRemote.getPlayerApi().skipPrevious();
                play.setText("Pause");
            });

            play.setOnClickListener(view -> {
                if (playerState.isPaused) {
                    mSpotifyAppRemote.getPlayerApi().resume();
                    play.setText("Pause");
                }
                else {
                    mSpotifyAppRemote.getPlayerApi().pause();
                    play.setText("Play");
                }
            });

            repeat.setOnClickListener(view -> {
                if (playerState.playbackOptions.repeatMode == 0) {
                    repeat.setText("Repeating All");
                    mSpotifyAppRemote.getPlayerApi().setRepeat(2);
                }
                else if (playerState.playbackOptions.repeatMode == 2) {
                    repeat.setText("Repeating Song");
                    mSpotifyAppRemote.getPlayerApi().setRepeat(1);
                }
                else {
                    repeat.setText("Repeating Off");
                    mSpotifyAppRemote.getPlayerApi().setRepeat(0);
                }
            });
            shuffle.setOnClickListener(view -> {
                if (!playerState.playbackOptions.isShuffling) {
                    shuffle.setText("Shuffling On");
                    mSpotifyAppRemote.getPlayerApi().setShuffle(true);
                } else {
                    shuffle.setText("Shuffling Off");
                    mSpotifyAppRemote.getPlayerApi().setShuffle(false);
                }
            });

            Lyrics.setOnClickListener(view -> {
                update_Lyrics(track.artist.name, track.name, backgroundColor, textColor);
            });
        });

    }

    @SuppressLint("ObsoleteSdkInt")
    private void createPaletteAsync(Bitmap bitmap) {
        Palette.from(bitmap).generate((palette -> {
            backgroundColor = palette.getDominantSwatch().getRgb();
            textColor = palette.getDominantSwatch().getBodyTextColor();
            Palette.Swatch screenBack = palette.getDarkVibrantSwatch();
            if (screenBack != null) {
                backgroundColor = screenBack.getRgb();
                textColor = screenBack.getBodyTextColor();
            }

            layout.setBackgroundColor(backgroundColor);
            current_song.setTextColor(textColor);
            current_artist.setTextColor(textColor);
            current_album.setTextColor(textColor);
            play.setTextColor(textColor);
            next.setTextColor(textColor);
            previous.setTextColor(textColor);
            repeat.setTextColor(textColor);
            shuffle.setTextColor(textColor);
            getWindow().setNavigationBarColor(backgroundColor);
            Lyrics.setTextColor(textColor);
        }
        ));
    }

    public void update_Lyrics(String track, String artist,int backgroundColor, int textColor){
        Music music = new Music(artist, track, backgroundColor, textColor);
        Intent intent = new Intent(MainActivity.this, Scraper.class);
        intent.putExtra("Music", music);
        startActivity(intent);
    }
}
