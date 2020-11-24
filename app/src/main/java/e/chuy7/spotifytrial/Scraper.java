package e.chuy7.spotifytrial;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

import static org.jsoup.nodes.Document.OutputSettings.Syntax.html;

public class Scraper extends AppCompatActivity {

    private TextView lyricsText;
    private ConstraintLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().hide();
        int c = getResources().getConfiguration().orientation;
        if(c == Configuration.ORIENTATION_LANDSCAPE){
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);
        layout = findViewById(R.id.linearLayout);
        lyricsText = findViewById(R.id.lyrics);
        Intent intent = getIntent();
        Music music = intent.getParcelableExtra("Music");
        start(music.getTitle(), music.getArtist(), music.getBackground_color(), music.getLyric_color());

    }

    public void restart(){
        finish();
        startActivity(getIntent());
    }

    public void start(String song_Title, String artist, int backgroundColor, int lyricColor) {
        myTaskParams params = new myTaskParams(song_Title, artist, backgroundColor, lyricColor);
        new Lyr().execute(params);
    }

    private class myTaskParams{
        String artist;
        String songTitle;
        int backgroundColor;
        int lyricColor;

        myTaskParams(String artist, String songTitle, int backgroundColor, int lyricColor) {
            this.artist = artist;
            this.songTitle = songTitle;
            this.backgroundColor = backgroundColor;
            this.lyricColor = lyricColor;
        }
    }

    private class Lyr extends AsyncTask<myTaskParams, Void, Void> {

        @Override
        protected Void doInBackground(myTaskParams... params) {
            String lyrics = "";
            String song_Title = params[0].artist.split("\\(")[0].replaceAll("'","").replaceAll("&","and").replaceAll("[%.'\''/']","").replaceAll("[^\\dA-Za-z]","-");
            String artist = params[0].songTitle.replaceAll("&","and").replaceAll("ó","o").replaceAll("á","a").replaceAll("í","i").replaceAll("ú","u").replaceAll("é","e").replaceAll("[.,''\''/']","").replaceAll("[^\\dA-Za-z]","-");

            int backgroundColor = params[0].backgroundColor;
            int lyricColor = params[0].lyricColor;
            String final_song_title = song_Title + "-lyrics";
            String mod_Song = final_song_title.replaceAll("---","-").replaceAll("--","-");
           System.out.println(song_Title);
           System.out.println(artist);
            try {

                org.jsoup.nodes.Document doc = Jsoup.connect("https://genius.com/" + artist + "-" + mod_Song).get();
                Elements LyricsTag = doc.select(".lyrics");
                Document doc2 = Jsoup.parse(LyricsTag.first().html());
                doc2.select("br").append("\\n");
                doc2.select("p").prepend("\n\\n");
                lyrics = doc2.text().replace("\\n","\n");

            }

            catch (IOException e) {

                e.printStackTrace();
            }

            String finalLyrics = lyrics;

            runOnUiThread(() ->

                    lyricsText.setText(finalLyrics));
            lyricsText.setTextColor(lyricColor);
            layout.setBackgroundColor(backgroundColor);
            getWindow().setNavigationBarColor(backgroundColor);
            return null;
        }
    }
}

