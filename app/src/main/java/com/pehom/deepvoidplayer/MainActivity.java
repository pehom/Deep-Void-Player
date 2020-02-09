package com.pehom.deepvoidplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
    private List<String> playlistArrayList;
    private ListView playlistListView;
    private TextView currentTrackTextView;
    private ArrayAdapter<String> adapter;
    private HashMap tracksHashMap;
    private int currentPlaylistItemPosition = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        playPauseIcon = findViewById(R.id.imageViewPlay);
        playlistListView = findViewById(R.id.playlistListView);
        currentTrackTextView = findViewById(R.id.currentTrackTextView);
        mediaPlayer = new MediaPlayer();

        seekbar = findViewById(R.id.seekBar);
        playlistArrayList = new ArrayList<String>();

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
        adapter = new ArrayAdapter<>(this, R.layout.playlist_item, playlistArrayList);
        playlistListView.setAdapter(adapter);
        playlistListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //play
                currentPlaylistItemPosition = position;
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = new MediaPlayer();
                    currentTrackTextView.setText("" + parent.getItemAtPosition(position));
                    try {
                        mediaPlayer.setDataSource(""+tracksHashMap.get(parent.getItemAtPosition(position)));
                        mediaPlayer.prepare();
                        seekbar.setMax(mediaPlayer.getDuration());
                        new Timer().scheduleAtFixedRate(new TimerTask() {
                            @Override
                            public void run() {
                                seekbar.setProgress(mediaPlayer.getCurrentPosition());
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
                } else {
                    mediaPlayer = new MediaPlayer();
                    Log.d("mypath", "path = " + tracksHashMap.get(parent.getItemAtPosition(position)));
                    currentTrackTextView.setText("position = " + position + parent.getItemAtPosition(position));
                    try {
                        mediaPlayer.setDataSource(""+tracksHashMap.get(parent.getItemAtPosition(position)));
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                        playPauseIcon.setImageResource(R.drawable.ic_pause_red);
                        seekbar.setMax(mediaPlayer.getDuration());
                        new Timer().scheduleAtFixedRate(new TimerTask() {
                            @Override
                            public void run() {
                                seekbar.setProgress(mediaPlayer.getCurrentPosition());
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
                }
            }
        });
    }

    public void getMusic(){
        tracksHashMap = new HashMap();
        ContentResolver contentResolver = getContentResolver();
        Uri trackUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        Cursor cursor  = contentResolver.query(trackUri, null, selection, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int trackTitle = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int trackArtist = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);

            do {
                String currentTitle = cursor.getString(trackTitle);
                String currentArtist = cursor.getString(trackArtist);
                String track = currentArtist + " - " + currentTitle;
                playlistArrayList.add(track);
                tracksHashMap.put(track, cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
                Log.d("hashmap", "" + tracksHashMap.get(track) );
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
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = new MediaPlayer();

                currentTrackTextView.setText("" + playlistListView.getItemAtPosition(currentPlaylistItemPosition));
                try {
                    mediaPlayer.setDataSource(""+tracksHashMap.get(playlistListView.getItemAtPosition(currentPlaylistItemPosition)));
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    playPauseIcon.setImageResource(R.drawable.ic_pause_red);
                    seekbar.setMax(mediaPlayer.getDuration());
                    new Timer().scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            seekbar.setProgress(mediaPlayer.getCurrentPosition());
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
            } else {
                mediaPlayer = new MediaPlayer();
                Log.d("mypath", "path = " + tracksHashMap.get(playlistListView.getItemAtPosition(currentPlaylistItemPosition)));
                currentTrackTextView.setText("" + playlistListView.getItemAtPosition(currentPlaylistItemPosition));
                try {
                    mediaPlayer.setDataSource(""+tracksHashMap.get(playlistListView.getItemAtPosition(currentPlaylistItemPosition)));
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    playPauseIcon.setImageResource(R.drawable.ic_pause_red);
                    seekbar.setMax(mediaPlayer.getDuration());
                    new Timer().scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            seekbar.setProgress(mediaPlayer.getCurrentPosition());
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

            }
        }

    }

    public void prevTrack(View view) {
        if (currentPlaylistItemPosition > 1) {
            currentPlaylistItemPosition--;
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = new MediaPlayer();

                currentTrackTextView.setText("" + playlistListView.getItemAtPosition(currentPlaylistItemPosition));
                try {
                    mediaPlayer.setDataSource(""+tracksHashMap.get(playlistListView.getItemAtPosition(currentPlaylistItemPosition)));
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    playPauseIcon.setImageResource(R.drawable.ic_pause_red);

                    seekbar.setMax(mediaPlayer.getDuration());
                    new Timer().scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            seekbar.setProgress(mediaPlayer.getCurrentPosition());
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
            } else {
                mediaPlayer = new MediaPlayer();
                Log.d("mypath", "path = " + tracksHashMap.get(playlistListView.getItemAtPosition(currentPlaylistItemPosition)));
                currentTrackTextView.setText("" + playlistListView.getItemAtPosition(currentPlaylistItemPosition));
                try {
                    mediaPlayer.setDataSource(""+tracksHashMap.get(playlistListView.getItemAtPosition(currentPlaylistItemPosition)));
                    mediaPlayer.prepare();
                    seekbar.setMax(mediaPlayer.getDuration());
                    new Timer().scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            seekbar.setProgress(mediaPlayer.getCurrentPosition());
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

            }
        }
    }

    public void play(View view) {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            playPauseIcon.setImageResource(R.drawable.ic_play_arrow_red);
        }
        else {


            seekbar.setMax(mediaPlayer.getDuration());
            new Timer().scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    seekbar.setProgress(mediaPlayer.getCurrentPosition());
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

        }
    }

}
