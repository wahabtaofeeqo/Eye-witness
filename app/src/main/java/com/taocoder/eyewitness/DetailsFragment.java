package com.taocoder.eyewitness;


import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;


/**
 * A simple {@link Fragment} subclass.
 */
public class DetailsFragment extends Fragment {

    private static Witness witness;

    private VideoView videoView;
    private ProgressBar progressBar;

    public DetailsFragment() {
        // Required empty public constructor
    }

    public static DetailsFragment getInstance(Witness w) {
        witness = w;
        return new DetailsFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_details, container, false);

        TextView filename = (TextView) view.findViewById(R.id.filename);
        TextView type = (TextView) view.findViewById(R.id.type);
        TextView desc = (TextView) view.findViewById(R.id.desc);
        MaterialButton play = (MaterialButton) view.findViewById(R.id.play);
        AppCompatImageView imageView = (AppCompatImageView) view.findViewById(R.id.picture);

        FrameLayout frameLayout = (FrameLayout) view.findViewById(R.id.holder);
        videoView = (VideoView) view.findViewById(R.id.video);
        progressBar = (ProgressBar) view.findViewById(R.id.loading);

        if (witness != null) {

            type.setText(getResources().getString(R.string.type, witness.getType().toUpperCase()));
            if (witness.getType().equalsIgnoreCase("video")) {
                filename.setText(getResources().getString(R.string.filename, witness.getFilename()));
                desc.setVisibility(View.GONE);
                play.setVisibility(View.GONE);
                imageView.setVisibility(View.GONE);

                prepare(witness.getFilename());
            }
            else if(witness.getType().equalsIgnoreCase("audio")) {
                filename.setText(getResources().getString(R.string.filename, witness.getFilename()));
                desc.setVisibility(View.GONE);
                frameLayout.setVisibility(View.GONE);
                imageView.setVisibility(View.GONE);

            }
            else if(witness.getType().equalsIgnoreCase("picture")) {
                filename.setText(getResources().getString(R.string.filename, witness.getFilename()));
                desc.setVisibility(View.GONE);
                frameLayout.setVisibility(View.GONE);
                play.setVisibility(View.GONE);

                Glide.with(getContext()).load(Utils.PICTURES_URL + witness.getFilename()).into(imageView);
            }
            else {
                desc.setText(witness.getDesc());
                filename.setVisibility(View.GONE);
                frameLayout.setVisibility(View.GONE);
                play.setVisibility(View.GONE);
            }
        }

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                play(witness.getType(), witness.getFilename());
            }
        });

        return view;
    }

    private void play(String type, String filename) {

        if (type.equalsIgnoreCase("video")) {
            MediaController mediaController = new MediaController(getContext());

            mediaController.setAnchorView(videoView);
            mediaController.setMediaPlayer(videoView);

            videoView.setMediaController(mediaController);
            Uri uri = Uri.parse(Utils.VIDEOS_URL + filename);
            videoView.setVideoURI(uri);

            //videoView.setZOrderOnTop(true);

            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    progressBar.setVisibility(View.GONE);
                    videoView.start();
                }
            });
        }

        if (type.equalsIgnoreCase("audio")) {

            try {
                MediaPlayer player = new MediaPlayer();
                player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                player.setDataSource(Utils.AUDIOS_URL + witness.getFilename());

                player.prepare();
                player.start();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void prepare(String filename) {

        MediaController mediaController = new MediaController(getContext());

        mediaController.setAnchorView(videoView);
        mediaController.setMediaPlayer(videoView);

        videoView.setMediaController(mediaController);
        Uri uri = Uri.parse(Utils.VIDEOS_URL + filename);
        videoView.setVideoURI(uri);

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                progressBar.setVisibility(View.GONE);
                videoView.start();
            }
        });
    }
}