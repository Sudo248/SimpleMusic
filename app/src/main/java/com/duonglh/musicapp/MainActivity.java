package com.duonglh.musicapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.duonglh.musicapp.model.Data.Mp3File;
import com.duonglh.musicapp.model.MyMediaPlay;
import com.duonglh.musicapp.model.Song.Song;
import com.duonglh.musicapp.model.Song.SongAdapter;
import com.duonglh.musicapp.model.Song.SongDataBase;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private CircleImageView imageViewSongPlaying;
    private TextView textViewNameSongPlaying, textViewAuthorPlaying;
    private ImageButton previousButton, nextButton, playingButton;
    private SongAdapter songAdapter;
    private ArrayList<Song> listSong = new ArrayList<>();
    private SearchView searchView;
    private ConstraintLayout mainPlaying;
    private Thread updateProcessBar;
    private final MyMediaPlay myMediaPlay = new MyMediaPlay();
    public static final int REQUEST_FROM_PLAYING_ACTIVITY = 1;
    public static final int REQUEST_PERMISSION = 2;

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mapping();
        checkPermissions();
    }

    private void mapping(){
        recyclerView            = findViewById(R.id.listViewSongs);
        progressBar             = findViewById(R.id.progress_music);
        imageViewSongPlaying    = findViewById(R.id.main_playing_music_icon);
        textViewNameSongPlaying = findViewById(R.id.main_playing_name_song);
        textViewAuthorPlaying   = findViewById(R.id.main_name_author);
        previousButton          = findViewById(R.id.main_previous_button);
        nextButton              = findViewById(R.id.main_next_button);
        playingButton           = findViewById(R.id.main_play_button);
        mainPlaying             = findViewById(R.id.main_playing);
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void checkPermissions(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED &&
            checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED){
                String[] permission = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO};
                requestPermissions(permission, REQUEST_PERMISSION);
            }
            else{
                loadData();
            }
        }
        else{
            loadData();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_PERMISSION){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                loadData();
            }
            else{
                Toast.makeText(this,"Permission Denied",Toast.LENGTH_LONG).show();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void loadData(){
        Mp3File mp3file = new Mp3File();
        listSong = mp3file.getAllData(MainActivity.this);
        if(!listSong.isEmpty()){
            createMediaPlay();
            displayListSongs();
            setClickListener();
        }
        else{
            Toast.makeText(this, "NO  MUSIC",Toast.LENGTH_LONG).show();
        }
    }

    private void createMediaPlay(){
        MyMediaPlay.context = MainActivity.this;
        myMediaPlay.setCompletionSong();
        myMediaPlay.createSong( 0);
        myMediaPlay.setUpdateView(new MyMediaPlay.UpdateView() {
            @Override
            public void update() {
                setView();
            }
        });
    }

    private void displayListSongs(){

        songAdapter = new SongAdapter(new SongAdapter.IsClickFavorite() {
            @Override
            public void updateFavorite(Song song) {
                SongDataBase.getInstance(MainActivity.this).songDAO().updateSong(song);
            }
        }, new SongAdapter.IsOnClickItem() {
            @Override
            public void onClickItem(int position) {
                Intent intent = new Intent(MainActivity.this, PlayingActivity.class);
                intent.putExtra("position",position)
                        .putExtra("startNewSong", true);
                MainActivity.this.startActivityForResult(intent,REQUEST_FROM_PLAYING_ACTIVITY);
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        songAdapter.setData(MainActivity.this, listSong);
        recyclerView.setAdapter(songAdapter);
        setView();
    }

    private void setClickListener(){

        if(myMediaPlay.isPlaying()){
            animationRotation();
            playingButton.setBackgroundResource(R.drawable.ic_baseline_pause);
        }
        else{
            imageViewSongPlaying.animate().cancel();
            playingButton.setBackgroundResource(R.drawable.ic_baseline_play);
        }

        playingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(myMediaPlay.isPlaying()){
                    imageViewSongPlaying.animate().cancel();
                    playingButton.setBackgroundResource(R.drawable.ic_baseline_play);
                    myMediaPlay.pause();
                }
                else {
                    animationRotation();
                    myMediaPlay.play();
                    playingButton.setBackgroundResource(R.drawable.ic_baseline_pause);
                }
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myMediaPlay.nextSong();
                setView();
            }
        });

        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myMediaPlay.previousSong();
                setView();
            }
        });

        mainPlaying.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PlayingActivity.class);
                intent.putExtra("position",-1)
                        .putExtra("startNewSong", false);

                MainActivity.this.startActivityForResult(intent,REQUEST_FROM_PLAYING_ACTIVITY);
            }
        });

        updateProcessBar = new Thread(){
            @Override
            public void run() {
                int totalDuration = myMediaPlay.getTotalDuration();
                int currentDuration = 0;
                while (currentDuration <= totalDuration){
                    try{
                        sleep(500);
                        currentDuration = MyMediaPlay.mediaPlayer.getCurrentPosition();
                        progressBar.setProgress(currentDuration);

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                run();
            }
        };
        updateProcessBar.setDaemon(true);
        updateProcessBar.start();

    }

    private void animationRotation(){
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                imageViewSongPlaying.animate().rotationBy(360).withEndAction(this).setDuration(10000)
                        .setInterpolator(new LinearInterpolator()).start();
            }
        };
        imageViewSongPlaying.animate().rotationBy(360).withEndAction(runnable).setDuration(10000)
                .setInterpolator(new LinearInterpolator()).start();
    }

    private void setView(){
        Song playingSong = listSong.get(myMediaPlay.getCurrentSong());
        if (playingSong.getImage() != null) {
            Glide.with(MainActivity.this).asBitmap()
                    .load(playingSong.getImage())
                    .into(imageViewSongPlaying);
        } else {
            imageViewSongPlaying.setImageResource(R.drawable.avatar);
        }
        textViewNameSongPlaying.setText(playingSong.getNameSong());
        textViewAuthorPlaying.setText(playingSong.getNameAuthor());
        progressBar.setMax(myMediaPlay.getTotalDuration());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.main_menu, menu);
        SearchManager searchManager = (SearchManager) this.getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.search_bar).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(this.getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                songAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                songAdapter.getFilter().filter(newText);
                return false;
            }
        });

        return true;
    }

    @Override
    public void onBackPressed() {
        if(!searchView.isIconified()){
            searchView.setIconified(true);
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_FROM_PLAYING_ACTIVITY){
            MyMediaPlay.context = MainActivity.this;
            myMediaPlay.setCompletionSong();
            setView();
            if(myMediaPlay.isPlaying()){
                animationRotation();
                playingButton.setBackgroundResource(R.drawable.ic_baseline_pause);
            }
            else{
                imageViewSongPlaying.animate().cancel();
                playingButton.setBackgroundResource(R.drawable.ic_baseline_play);
            }
        }
    }

}