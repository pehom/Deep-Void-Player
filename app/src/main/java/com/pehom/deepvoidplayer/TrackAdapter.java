package com.pehom.deepvoidplayer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class TrackAdapter  extends RecyclerView.Adapter<TrackAdapter.TrackViewHolder> {
    private ArrayList<Track> tracks;
    private OnTrackClickListener listener;

    public TrackAdapter(ArrayList<Track> tracks) {
        this.tracks = tracks;
    }


    public interface OnTrackClickListener {
        void onTrackClick(int position);
    }

    public void setOnTrackClickListener(OnTrackClickListener listener) {

        this.listener = listener;
    }


    @NonNull
    @Override
    public TrackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.playlist_item, parent, false);
        TrackViewHolder viewHolder = new TrackViewHolder(view, listener);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull TrackViewHolder holder, int position) {
        Track currentTrack = tracks.get(position);
        holder.trackTitleTextView.setText(currentTrack.getArtist() + " - " + currentTrack.getTitle());
        holder.durationTextView.setText(currentTrack.getDuration());

    }

    @Override
    public int getItemCount() {
        return tracks.size();
    }

    public static class TrackViewHolder extends RecyclerView.ViewHolder {
        public TextView trackTitleTextView;
        public TextView durationTextView;

        public TrackViewHolder(@NonNull View itemView, final OnTrackClickListener listener) {
            super(itemView);
            trackTitleTextView = itemView.findViewById(R.id.trackTitleTextView);
            durationTextView = itemView.findViewById(R.id.durationTextView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onTrackClick(position);
                        }
                    }
                }
            });
        }
    }
}
