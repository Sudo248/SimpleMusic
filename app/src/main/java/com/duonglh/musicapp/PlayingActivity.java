package com.duonglh.musicapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.duonglh.musicapp.model.MyMediaPlayer;
import com.duonglh.musicapp.model.Song.Song;
import com.gauravk.audiovisualizer.visualizer.CircleLineVisualizer;

import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class PlayingActivity extends AppCompatActivity {
    private Button backButton;
    private ImageButton shuffleButton, previousButton, playButton, nextButton, repeatButton;
    private TextView durationView, totalDurationView, nameSongView, nameAuthorView;
    private CircleImageView playingImageView;
    private SeekBar seekBar;
    private Thread updateSeekBar;
    private CircleLineVisualizer circleVisualizer;
    private int position = 0;
    private boolean startNewSong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playing);
        Objects.requireNonNull(getSupportActionBar()).hide();
        mapping();
        loadData();
        setClickListener();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (circleVisualizer != null)
            circleVisualizer.release();
        PlayingActivity.this.finish();
    }

    private void mapping(){
        backButton           = findViewById(R.id.comeBackButton);
        shuffleButton        = findViewById(R.id.shuffle);
        previousButton       = findViewById(R.id.previous);
        playButton           = findViewById(R.id.playing_button);
        nextButton           = findViewById(R.id.next);
        repeatButton         = findViewById(R.id.repeat);
        durationView         = findViewById(R.id.duration);
        totalDurationView    = findViewById(R.id.totalDuration);
        nameSongView         = findViewById(R.id.name_song_playing);
        nameAuthorView       = findViewById(R.id.name_author_playing);
        playingImageView     = findViewById(R.id.image_playing_song);
        seekBar              = findViewById(R.id.seekBar);
        circleVisualizer     = findViewById(R.id.circleVisualizer);

    }

    private void loadData(){

        Intent intent       = this.getIntent();
        Bundle bundle       = intent.getExtras();
        position            = bundle.getInt("position",0);
        startNewSong        = bundle.getBoolean("startNewSong");
        MyMediaPlayer.getInstance().setContext(this);
        MyMediaPlayer.getInstance().setViewSong(new MyMediaPlayer.ViewSong() {
            @Override
            public void update() {
                setView();
            }
        });
    }

    private void setClickListener(){

        if(startNewSong){
            MyMediaPlayer.getInstance().play(position);
            animationRotation();
        }
        else{
            if(MyMediaPlayer.MUSIC.isPlaying()){
                playButton.setBackgroundResource(R.drawable.ic_baseline_pause);
                animationRotation();
            }
            else{
                playButton.setBackgroundResource(R.drawable.ic_baseline_play);
                playingImageView.animate().cancel();
            }
        }

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(MyMediaPlayer.MUSIC.isPlaying()){
                    playingImageView.animate().cancel();
                    MyMediaPlayer.MUSIC.pause();
                    playButton.setBackgroundResource(R.drawable.ic_baseline_play);
                }
                else{
                    animationRotation();
                    MyMediaPlayer.MUSIC.start();
                    playButton.setBackgroundResource(R.drawable.ic_baseline_pause);
                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(Activity.RESULT_OK);
                if(circleVisualizer != null){
                    circleVisualizer.release();
                }
                PlayingActivity.this.finish();
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyMediaPlayer.getInstance().nextSong();
                setView();
            }
        });

        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyMediaPlayer.getInstance().previousSong();
                setView();
            }
        });

        repeatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(MyMediaPlayer.MUSIC.isLooping()){
                    MyMediaPlayer.MUSIC.setLooping(false);
                    repeatButton.setBackgroundResource(R.drawable.ic_baseline_repeat_on);
                }
                else{
                    MyMediaPlayer.MUSIC.setLooping(true);
                    repeatButton.setBackgroundResource(R.drawable.ic_baseline_repeat);
                }
            }
        });

        shuffleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(MyMediaPlayer.getInstance().isShuffle()){
                    MyMediaPlayer.getInstance().setShuffle(false);
                    shuffleButton.setBackgroundResource(R.drawable.ic_baseline_shuffle_on);
                }
                else{
                    MyMediaPlayer.getInstance().setShuffle(true);
                    shuffleButton.setBackgroundResource(R.drawable.ic_baseline_shuffle);
                }
                if(MyMediaPlayer.MUSIC.isLooping()){
                    repeatButton.performClick();
                }
            }
        });

        updateSeekBar = new Thread(){
            @Override
            public void run() {
                int totalDuration = MyMediaPlayer.MUSIC.getDuration();
                seekBar.setMax(totalDuration);
                int currentDuration = 0;
                while (currentDuration <= totalDuration){
                    try{
                        sleep(500);
                        currentDuration = MyMediaPlayer.MUSIC.getCurrentPosition();
                        seekBar.setProgress(currentDuration);

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                run();
            }
        };

        updateSeekBar.setDaemon(true);
        updateSeekBar.start();

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                durationView.setText(timeToString(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                MyMediaPlayer.MUSIC.seekTo(seekBar.getProgress());
                durationView.setText(timeToString(seekBar.getProgress()));
            }
        });

        final Handler handler = new Handler();
        final int delay = 1000;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                durationView.setText(timeToString(MyMediaPlayer.MUSIC.getCurrentPosition()));
                handler.postDelayed(this, delay);
            }
        }, delay);
        setView();
    }

    private void setView(){
        Song songPlaying = MyMediaPlayer.getInstance().getCurrentSong();
        nameSongView.setText(songPlaying.getNameSong());
        nameAuthorView.setText(songPlaying.getNameAuthor());
        if(songPlaying.getImage() != null) {
            Glide.with(getApplicationContext()).asBitmap()
                    .load(songPlaying.getImage())
                    .into(playingImageView);
        }
        else{
            playingImageView.setImageResource(R.drawable.avatar);
        }
        totalDurationView.setText(songPlaying.getDuration());
        int id = MyMediaPlayer.MUSIC.getAudioSessionId();
        if(id != -1){
            circleVisualizer.setEnabled(false);
            circleVisualizer.setAudioSessionId(id);
        }
    }

    private void animationRotation(){
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                playingImageView.animate().rotationBy(360).withEndAction(this).setDuration(10000)
                        .setInterpolator(new LinearInterpolator()).start();
            }
        };
        playingImageView.animate().rotationBy(360).withEndAction(runnable).setDuration(10000)
                .setInterpolator(new LinearInterpolator()).start();
    }

    private String timeToString(int duration){
        String time;
        int hours = duration / 1000 / 60 / 60;
        int minutes = duration / 1000 / 60 % 60;
        int seconds = duration / 1000 % 60;
        if(hours == 0){
            time = minutes + ":" + (seconds < 10 ? ("0"+ seconds) : (""+seconds));
        }
        else{
            time = hours + ":" + (minutes < 10 ? ("0"+ minutes) : (""+minutes)) + ":" + (seconds < 10 ? ("0"+ seconds) : (""+seconds));
        }
        return time;
    }

}