package com.duonglh.musicapp;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.palette.graphics.Palette;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.duonglh.musicapp.model.Song.Song;
import com.duonglh.musicapp.service.MusicService;
import com.gauravk.audiovisualizer.visualizer.CircleLineVisualizer;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class PlayingActivity extends AppCompatActivity implements Interface.MediaPlayerAction{
    private Button playButton, repeatButton, nextButton, previousButton, shuffleButton;
    private ImageButton backButton;
    private TextView durationView, totalDurationView, nameSongView, nameAuthorView;
    private CircleImageView playingImageView;
    private SeekBar seekBar;
    private final Handler playingThreadHandler = new Handler(Looper.getMainLooper());
    private CircleLineVisualizer circleVisualizer;
    private int position = 0;
    private boolean startNewSong;
    private MusicService musicService;
    private boolean isTouchSeekBar = false;
    private ConstraintLayout playingActivity;
    private MusicService.MusicBinder musicBinder;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
           musicBinder = (MusicService.MusicBinder) service;
           new ConnectService().execute();
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.load_data);
        Objects.requireNonNull(getSupportActionBar()).hide();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent bindService = new Intent(PlayingActivity.this, MusicService.class);
        bindService(bindService, serviceConnection, BIND_AUTO_CREATE);
        Intent intent       = this.getIntent();
        position            = intent.getIntExtra("position",-1);
        startNewSong        = intent.getBooleanExtra("startNewSong", false);
        Intent service      = new Intent(this, MusicService.class);
        service.putExtra("CurrentSong", position);
        startService(service);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishActivity();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void mapping(){
        circleVisualizer     = findViewById(R.id.circleVisualizer);
        playButton           = findViewById(R.id.playing_button);
        repeatButton         = findViewById(R.id.repeat);
        nextButton           = findViewById(R.id.next);
        previousButton       = findViewById(R.id.previous);
        durationView         = findViewById(R.id.duration);
        totalDurationView    = findViewById(R.id.totalDuration);
        nameSongView         = findViewById(R.id.name_song_playing);
        nameAuthorView       = findViewById(R.id.name_author_playing);
        playingImageView     = findViewById(R.id.image_playing_song);
        seekBar              = findViewById(R.id.seekBar);
        playingActivity      = findViewById(R.id.playingActivity);
        backButton           = findViewById(R.id.backButton);
        shuffleButton        = findViewById(R.id.shuffle);
    }

    private void prepare(){

        musicService.setUpdateView(new Interface.UpdateView() {
            @Override
            public void update() {
                setView();
            }
        });

        musicService.setCallBack(this);

        if(startNewSong){
            musicService.create(position);
            musicService.play();
            animationRotation();
        }
        else{
            if(musicService.isPlaying()){
                playButton.setBackgroundResource(R.drawable.ic_baseline_pause);
                animationRotation();
            }
            else{
                playButton.setBackgroundResource(R.drawable.ic_baseline_play);
                playingImageView.animate().cancel();
            }
        }
        playButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                play();
            }
        });
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextSong();
            }
        });
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                previousSong();
            }
        });

        repeatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(musicService.isLooping()){
                    musicService.setLoop(false);
                    repeatButton.setBackgroundResource(R.drawable.ic_baseline_repeat_off);
                }
                else{
                    musicService.setLoop(true);
                    repeatButton.setBackgroundResource(R.drawable.ic_baseline_repeat_on);
                }
                if(musicService.isShuffle()){
                    shuffleButton.performClick();
                }
            }
        });

        shuffleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(musicService.isShuffle()){
                    musicService.setShuffle(false);
                    shuffleButton.setBackgroundResource(R.drawable.ic_baseline_shuffle_off);
                }
                else{
                    musicService.setShuffle(true);
                    shuffleButton.setBackgroundResource(R.drawable.ic_baseline_shuffle_on);
                }
                if(musicService.isLooping()){
                    repeatButton.performClick();
                }
            }
        });

        setView();

        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int finalCurrentDuration = musicService.getCurrentPosition();
                if (!isTouchSeekBar) {
                    seekBar.setProgress(finalCurrentDuration);
                    durationView.setText(timeToString(finalCurrentDuration));
                }
                playingThreadHandler.postDelayed(this, 500);
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                durationView.setText(timeToString(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isTouchSeekBar = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                musicService.SeekTo(seekBar.getProgress());
                durationView.setText(timeToString(seekBar.getProgress()));
                isTouchSeekBar = false;
            }
        });
        circleVisualizer.setDrawLine(true);
    }


    public void clickBackButton(View view){
        finishActivity();
    }

    private void setView(){
        Song songPlaying = musicService.getCurrentSong();
        Bitmap bitmap;
        nameSongView.setText(songPlaying.getNameSong());
        nameSongView.setSelected(true);
        nameAuthorView.setText(songPlaying.getNameAuthor());
        seekBar.setMax(musicService.getDuration());
        if(songPlaying.getImage() != null) {
            Glide.with(getApplicationContext()).asBitmap()
                    .load(songPlaying.getImage())
                    .into(playingImageView);
            bitmap = BitmapFactory.decodeByteArray(songPlaying.getImage(),0,songPlaying.getImage().length);
        }
        else{
            playingImageView.setImageResource(R.drawable.avatar);
            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.avatar);
        }

        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @SuppressLint({"ResourceAsColor", "UseCompatLoadingForColorStateLists"})
            @Override
            public void onGenerated(@Nullable Palette palette) {
                assert palette != null;
                Palette.Swatch swatch = palette.getDominantSwatch();
                GradientDrawable gradientDrawable;
                int color = Color.WHITE; //white
                if(swatch != null){
                    int RGB = swatch.getRgb();
                    int red = Color.red(RGB);
                    int green = Color.green(RGB);
                    int blue = Color.blue(RGB);
                    gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                            new int[] {RGB, RGB});
                    if(red > 180 && green > 180 && blue > 180){
                        color = Color.BLACK;//black
                    }
                    circleVisualizer.setColor(Color.rgb((128+red)%255, (128+green)%255, (128+blue)%255));
                }
                else{
                    gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                            new int[] {0xff000000, 0xff000000});
                }
                durationView.setTextColor(color);
                totalDurationView.setTextColor(color);
                nameAuthorView.setTextColor(color);
                nameSongView.setTextColor(color);
                playingActivity.setBackground(gradientDrawable);
                if(color == Color.WHITE)
                    backButton.setBackgroundResource(R.drawable.ic_baseline_white_arrow_left);
                else backButton.setBackgroundResource(R.drawable.ic_baseline_black_arrow_left);
            }
        });

        totalDurationView.setText(songPlaying.getDuration());
        int id = musicService.getAudioSessionId();
        if(id != -1){
            circleVisualizer.setEnabled(false);
            circleVisualizer.setAudioSessionId(id);
        }
        if(musicService.isPlaying()){
            animationRotation();
            playButton.setBackgroundResource(R.drawable.ic_baseline_pause_circle_outline_24);
        }
        else{
            playingImageView.animate().cancel();
            playButton.setBackgroundResource(R.drawable.ic_baseline_play_circle_outline_24);
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

    @SuppressLint("DefaultLocale")
    @Contract(pure = true)
    private @NotNull String timeToString(int duration){
        String time;
        duration = duration / 1000;
        int hours = duration / 3600;
        int minutes = duration / 60 % 60;
        int seconds = duration % 60;
        if(hours == 0){
            time = String.format("%d:%02d",minutes, seconds);
        }
        else{
            time = String.format("%d:%02d:%02d", hours, minutes, seconds);
        }
        return time;
    }

    private void finishActivity(){
        if(circleVisualizer != null){
            circleVisualizer.release();
        }
        setResult(Activity.RESULT_OK);
        finish();
//        overridePendingTransition(R.anim.slide_from_top,R.anim.slide_in_top);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void play() {
        if(musicService.isPlaying()){
            playingImageView.animate().cancel();
            musicService.pause();
            playButton.setBackgroundResource(R.drawable.ic_baseline_play_circle_outline_24);
        }
        else{
            animationRotation();
            musicService.play();
            playButton.setBackgroundResource(R.drawable.ic_baseline_pause_circle_outline_24);
        }
    }

    @Override
    public void nextSong() {
        musicService.nextSong();
        setView();
    }

    @Override
    public void previousSong() {
        musicService.previousSong();
        setView();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void cancel() {
        if(musicService.isPlaying()){
            playingImageView.animate().cancel();
            musicService.pause();
            playButton.setBackgroundResource(R.drawable.ic_baseline_play_circle_outline_24);
        }
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    @SuppressLint("StaticFieldLeak")
    private class ConnectService extends AsyncTask<Void, Integer, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @RequiresApi(api = Build.VERSION_CODES.R)
        @Override
        protected String doInBackground(Void... voids) {
            musicService = musicBinder.getService();
            return null;
        }

        @RequiresApi(api = Build.VERSION_CODES.R)
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            setContentView(R.layout.activity_playing);
            mapping();
            prepare();
        }
    }

}