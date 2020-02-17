package com.pehom.deepvoidplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class CreatePlaylistActivity extends AppCompatActivity {
    private RecyclerView chosenTracksRecyclerView;
    private RecyclerView tracksToSelectRecyclerView;
    private EditText playlistTitleEditText;
    private ArrayList<Artist> artists;
    private ArrayList<Track> thisArtistTracksArrayList;

    private LinearLayoutManager artistsLayoutManager;
    private ArtistsAdapter artistsAdapter;
    private float stopx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_playlist);
        chosenTracksRecyclerView = findViewById(R.id.chosenTracks);
      //  tracksToSelectRecyclerView = findViewById(R.id.tracksToSelectRecyclerView);
        playlistTitleEditText = findViewById(R.id.playlistTitleEditText);
        showArtists();

    }

    private void showArtists() {
        artists = new ArrayList<>();

        ContentResolver mContentResolver = getContentResolver();

        String[] mProjection =
                {

                        MediaStore.Audio.Artists.ARTIST,
                        MediaStore.Audio.Artists.NUMBER_OF_TRACKS,
                        MediaStore.Audio.Artists.ARTIST_KEY
                };

        Cursor artistCursor = mContentResolver.query(
                MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
                mProjection,
                null,
                null,
                MediaStore.Audio.Artists.ARTIST + " ASC");

        if (artistCursor != null && artistCursor.moveToFirst()) {

            do {
                String currentArtistName = artistCursor.getString(artistCursor.getColumnIndex(MediaStore.Audio.Artists.ARTIST));
                String numberOfTracks = artistCursor.getString(artistCursor.getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_TRACKS));
                String artistKey = artistCursor.getString(artistCursor.getColumnIndex(MediaStore.Audio.Artists.ARTIST_KEY));
                Artist currentArtist  = new Artist(currentArtistName, artistKey, numberOfTracks, null);
                artists.add(currentArtist);
               // testTextView.append(currentArtist + "\n");
            } while (artistCursor.moveToNext());
        }
        tracksToSelectRecyclerView = findViewById(R.id.tracksToSelectRecyclerView);
        // playlistRecyclerView.setHasFixedSize(true);

        artistsLayoutManager = new LinearLayoutManager(this);
        artistsAdapter = new ArtistsAdapter(artists, new ArtistsAdapter.OnArtistClickListener() {
            @Override
            public void onArtistClick(int position) {
                Log.d("myClick", "click-click");
                showArtistTracks(position);
                ImageView backToArtists = findViewById(R.id.backToArtistImageView);
                backToArtists.setVisibility(View.VISIBLE);

            }
        });
       /* artistsAdapter.setOnArtistClickListener(new ArtistsAdapter.OnArtistClickListener() {
            @Override
            public void onArtistClick(int position) {
                Log.d("myClick", "click-click");
            }
        });
      *//*  artistsAdapter.setOnArtistClickListener(new ArtistsAdapter.OnArtistClickListener() {
            @Override
            public void onArtistClick(int position) {
                showArtistTracks(position);

            }
        });*//*
        artistsAdapter.setOnArtistTouchListener(new ArtistsAdapter.OnArtistTouchListener() {
            @Override
            public void onArtistTouch(int position) {

                *//*switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: // нажатие


                        break;
                    case MotionEvent.ACTION_MOVE: // движение

                        break;
                    case MotionEvent.ACTION_UP: // отпускание
                    case MotionEvent.ACTION_CANCEL:

                        stopx = event.getX();
                        break;
                }
                Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                int width = size.x;
                if (stopx < width/2) {
                    moveThisArtistTracks();
                }*//*
            }
        });*/

        tracksToSelectRecyclerView.setLayoutManager(artistsLayoutManager);
        tracksToSelectRecyclerView.setAdapter(artistsAdapter);


    }

    private void showArtistTracks(int position) {
        thisArtistTracksArrayList = new ArrayList<>();

        ContentResolver contentResolver = getContentResolver();
        Uri trackUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA
        };

        String[] selectionArgs = {artists.get(position).getArtistKey()};
        Cursor cursor  = contentResolver.query(trackUri,
                projection,
                MediaStore.Audio.Media.ARTIST_KEY+ "=?",
                selectionArgs ,
                null);


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

                Log.d("theTrack", "" + currentArtist + "  " + currentTitle + "  " + currentDuration);
               thisArtistTracksArrayList.add(currentTrack);

            } while (cursor.moveToNext());
        }
        RecyclerView.LayoutManager trackLayoutManager = new LinearLayoutManager(this);
        TrackAdapter thisArtistTracksAdapter = new TrackAdapter(thisArtistTracksArrayList);

        tracksToSelectRecyclerView.setLayoutManager(trackLayoutManager);
        tracksToSelectRecyclerView.setAdapter(thisArtistTracksAdapter);
    }

    public void backToArtists(View view) {
        showArtists();
        ImageView backToArtists = findViewById(R.id.backToArtistImageView);
        backToArtists.setVisibility(View.INVISIBLE);

    }
}
