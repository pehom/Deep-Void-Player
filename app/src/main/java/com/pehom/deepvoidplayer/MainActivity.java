package com.pehom.deepvoidplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private static final int MY_PERMISSION_REQUEST = 1;
    private MediaPlayer mediaPlayer;
    private ImageView playPauseIcon;
    private SeekBar seekbar;
    private ArrayList<Track> playlistArrayList;
    private RecyclerView.LayoutManager trackLayoutManager;
    private RecyclerView playlistRecyclerView;
    private TextView currentTrackTextView;
    private TrackAdapter playlistAdapter;


    private int currentPlaylistItemPosition = 0;
    private int currentTrackProgress = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        playPauseIcon = findViewById(R.id.imageViewPlay);

        currentTrackTextView = findViewById(R.id.currentTrackTextView);

        mediaPlayer = new MediaPlayer();

        seekbar = findViewById(R.id.seekBar);
        playlistArrayList = new ArrayList<Track>();


        if (ContextCompat.checkSelfPermission(
       MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
          if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                 Manifest.permission.READ_EXTERNAL_STORAGE)){
             ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST);
          }else {
             ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST);

         }

        } else {
            doStuff();
        }



    }

    public void doStuff(){
        getMusic();
        playlistRecyclerView = findViewById(R.id.playlistRecyclerView);
       // playlistRecyclerView.setHasFixedSize(true);

        trackLayoutManager = new LinearLayoutManager(this);
        playlistAdapter = new TrackAdapter(playlistArrayList);
        playlistAdapter.setOnTrackClickListener(new TrackAdapter.OnTrackClickListener() {
            @Override
            public void onTrackClick(int position) {
                currentPlaylistItemPosition = position;
                playThePosition(position);
            }
        });

        playlistRecyclerView.setLayoutManager(trackLayoutManager);
        playlistRecyclerView.setAdapter(playlistAdapter);




    }

    public void getMusic(){

        ContentResolver contentResolver = getContentResolver();
        Uri trackUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        Cursor cursor  = contentResolver.query(trackUri, null, selection, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int trackTitle = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int trackArtist = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int trackDuration = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);

            do {
                Track currentTrack = new Track();
                String currentTitle = cursor.getString(trackTitle);
                String currentArtist = cursor.getString(trackArtist);
                String currentDuration = cursor.getString(trackDuration);
                int duration = Integer.parseInt(currentDuration);
                int min = duration/1000/60;
                int sec = duration/1000 - min*60;
                if (sec < 10) currentDuration = "" + min + ":0" + sec;
                else currentDuration = "" + min + ":" + sec;
                currentTrack.setArtist(currentArtist);
                currentTrack.setTitle(currentTitle);
                currentTrack.setDuration(currentDuration);
                currentTrack.setData(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
                playlistArrayList.add(currentTrack);

            } while (cursor.moveToNext());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,  String[] permissions,  int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
                        doStuff();
                    }
                } else {
                    Toast.makeText(this, "No permission granted", Toast.LENGTH_SHORT).show();
                    finish();
                }
                return;
            }
        }
    }

    public void nextTrack(View view) {
        if (currentPlaylistItemPosition < playlistArrayList.size()-1) {
            currentPlaylistItemPosition++;
            playThePosition(currentPlaylistItemPosition);
        }

    }

    public void prevTrack(View view) {
        if (currentPlaylistItemPosition > 0) {
            currentPlaylistItemPosition--;
            playThePosition(currentPlaylistItemPosition);
        }
    }

    public void play(View view) {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            playPauseIcon.setImageResource(R.drawable.ic_play_arrow_red);
        } else {
            mediaPlayer.start();
            playPauseIcon.setImageResource(R.drawable.ic_pause_red);
        }


    }

    public void playThePosition(int position){
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(playlistArrayList.get(position).getData());
                mediaPlayer.prepare();
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        if (currentPlaylistItemPosition < playlistArrayList.size()-1) {
                            currentPlaylistItemPosition++;
                            playThePosition(currentPlaylistItemPosition);
                        }
                    }
                });
                playPauseIcon.setImageResource(R.drawable.ic_pause_red);
                seekbar.setMax(mediaPlayer.getDuration());
                new Timer().scheduleAtFixedRate(new TimerTask() {

                    @Override
                    public void run() {
                        seekbar.setProgress(mediaPlayer.getCurrentPosition());
                        int min = mediaPlayer.getCurrentPosition()/1000/60;
                        int sec = mediaPlayer.getCurrentPosition()/1000 - min*60;
                        Log.d("trackProgress", "trackProgress = " + min + ":" + sec);
                    }
                }, 0, 1000);
                seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (fromUser) {
                            mediaPlayer.seekTo(progress);
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
            currentTrackTextView.setText(playlistArrayList.get(position).getArtist() + " - " + playlistArrayList.get(position).getTitle());


        } else {
            mediaPlayer = new MediaPlayer();

            try {
                mediaPlayer.setDataSource(playlistArrayList.get(position).getData());
                mediaPlayer.prepare();
                seekbar.setMax(mediaPlayer.getDuration());
                new Timer().scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        seekbar.setProgress(mediaPlayer.getCurrentPosition());
                        int min = mediaPlayer.getCurrentPosition()/1000/60;
                        int sec = mediaPlayer.getCurrentPosition()/1000 - min*60;
                        Log.d("trackProgress", "trackProgress = " + min + ":" + sec);

                    }
                }, 0, 1000);

                seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (fromUser) {
                            mediaPlayer.seekTo(progress);
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
                mediaPlayer.start();
                playPauseIcon.setImageResource(R.drawable.ic_pause_red);

            } catch (IOException e) {
                e.printStackTrace();
            }

            currentTrackTextView.setText(playlistArrayList.get(position).getArtist() + " - " + playlistArrayList.get(position).getTitle());

        }

    }

}
