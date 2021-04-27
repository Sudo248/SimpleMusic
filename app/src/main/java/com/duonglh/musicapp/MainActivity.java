package com.duonglh.musicapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.duonglh.musicapp.model.Data.Mp3File;
import com.duonglh.musicapp.model.Song.Song;
import com.duonglh.musicapp.model.Song.SongAdapter;
import com.duonglh.musicapp.model.Song.SongDataBase;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements MediaPlayerAction{
    public static final int REQUEST_FROM_PLAYING_ACTIVITY = 1;
    public static final int REQUEST_PERMISSION = 2;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private CircleImageView imageViewSongPlaying;
    private TextView textViewNameSongPlaying, textViewAuthorPlaying;
    private Button previousButton, nextButton, playingButton;
    private SongAdapter songAdapter;
    private SearchView searchView;
    private ConstraintLayout mainPlaying;
    private MusicService musicService;
    private UpdateView updateView;
    private boolean isBoundService;
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder musicBinder = (MusicService.MusicBinder) service;
            musicService = musicBinder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicService = null;
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent service = new Intent(MainActivity.this, MusicService.class);
        isBoundService = bindService(service, serviceConnection, BIND_AUTO_CREATE);
        startNewActivity();
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void startNewActivity(){
        setContentView(R.layout.load_data);
        getSupportActionBar().hide();
        checkPermissions();
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void continteActivity(){
        setContentView(R.layout.activity_main);
        mapping();
        musicService.setUpdateView(updateView);
        musicService.setCallBack(this);
        displayListSongs();
        mainPlaying.setVisibility(View.VISIBLE);
        setView();
        prepare();
    }

    private void mapping() {
        recyclerView            = findViewById(R.id.listViewSongs);
        progressBar             = findViewById(R.id.progress_music);
        imageViewSongPlaying    = findViewById(R.id.main_playing_music_icon);
        textViewNameSongPlaying = findViewById(R.id.main_playing_name_song);
        textViewAuthorPlaying   = findViewById(R.id.main_name_author);
        previousButton          = findViewById(R.id.main_previous_button);
        nextButton              = findViewById(R.id.main_next_button);
        playingButton           = findViewById(R.id.main_play_button);
        mainPlaying             = findViewById(R.id.main_playing);
        searchView              = findViewById(R.id.searchView);
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED &&
                    checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED) {
                String[] permission = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO};
                requestPermissions(permission, REQUEST_PERMISSION);
            } else {
                new LoadDataAsyncTask().execute();
            }
        } else {
            new LoadDataAsyncTask().execute();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                new LoadDataAsyncTask().execute();
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void displayListSongs() {
        songAdapter = new SongAdapter(new SongAdapter.IsClickFavorite() {
            @Override
            public void updateFavorite(Song song) {
                SongDataBase.getInstance(MainActivity.this).songDAO().updateSong(song);
            }
        }, new SongAdapter.IsOnClickItem() {
            @Override
            public void onClickItem(int position) {
                StartPlayingActivity(position, true);
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        songAdapter.setData(MainActivity.this, Mp3File.getInstance().getListSong());
        recyclerView.setAdapter(songAdapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Song song = Mp3File.getInstance().getListSong().get(position);
                Mp3File.getInstance().getListSong().remove(position);
                songAdapter.notifyDataSetChanged();
                musicService.removeSong(position);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SongDataBase.getInstance(MainActivity.this).songDAO().deleteSong(song);
                    }
                }).start();
            }
        });

        itemTouchHelper.attachToRecyclerView(recyclerView);

        SearchManager searchManager = (SearchManager) this.getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(this.getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.onActionViewExpanded();
        if(!searchView.isFocused()) {
            searchView.clearFocus();
        }
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

    }

    private void prepare() {
        if (musicService.isPlaying()) {
            animationRotation();
            playingButton.setBackgroundResource(R.drawable.ic_baseline_pause);
        } else {
            imageViewSongPlaying.animate().cancel();
            playingButton.setBackgroundResource(R.drawable.ic_baseline_play);
        }

        playingButton.setOnClickListener(new View.OnClickListener() {
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

        mainPlaying.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StartPlayingActivity(-1,false);
            }
        });


        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setProgress(musicService.getCurrentPosition());
                mainThreadHandler.postDelayed(this, 500);
            }
        });

    }

    private void animationRotation() {
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

    private void setView() {
        Song playingSong = musicService.getCurrentSong();
        if (playingSong.getImage() != null) {
            Glide.with(getApplicationContext()).asBitmap()
                    .load(playingSong.getImage())
                    .into(imageViewSongPlaying);
        } else {
            imageViewSongPlaying.setImageResource(R.drawable.avatar);
        }
        textViewNameSongPlaying.setText(playingSong.getNameSong());
        textViewAuthorPlaying.setText(playingSong.getNameAuthor());
        progressBar.setMax(musicService.getDuration());
    }

    @Override
    public void onBackPressed() {
        if (!searchView.getQuery().equals("")) {
            searchView.setQuery("",false);
            searchView.clearFocus();
            searchView.setIconified(true);
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_FROM_PLAYING_ACTIVITY) {
            if(musicService != null) {
                musicService.setUpdateView(updateView);
                musicService.setCallBack(this);
                if(mainPlaying.getVisibility() == View.GONE){
                    mainPlaying.setVisibility(View.VISIBLE);
                    prepare();
                }

                setView();
                if (musicService.isPlaying()) {
                    animationRotation();
                    playingButton.setBackgroundResource(R.drawable.ic_baseline_pause);
                } else {
                    imageViewSongPlaying.animate().cancel();
                    playingButton.setBackgroundResource(R.drawable.ic_baseline_play);
                }
            }
        }
    }

    @Override
    public void play() {
        if (musicService.isPlaying()) {
            imageViewSongPlaying.animate().cancel();
            playingButton.setBackgroundResource(R.drawable.ic_baseline_play);
            musicService.pause();
        } else {
            animationRotation();
            musicService.play();
            playingButton.setBackgroundResource(R.drawable.ic_baseline_pause);
        }
    }

    @Override
    public void nextSong() {
        musicService.nextSong();
        animationRotation();
        playingButton.setBackgroundResource(R.drawable.ic_baseline_pause);
        setView();
    }

    @Override
    public void previousSong() {
        musicService.previousSong();
        animationRotation();
        playingButton.setBackgroundResource(R.drawable.ic_baseline_pause);
        setView();
    }

    @SuppressLint("StaticFieldLeak")
    private class LoadDataAsyncTask extends AsyncTask<Void, Integer, String> {
        @RequiresApi(api = Build.VERSION_CODES.R)
        @Override
        protected String doInBackground(Void... voids) {
            Mp3File.getInstance().loadAllData(MainActivity.this);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @RequiresApi(api = Build.VERSION_CODES.R)
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            setContentView(R.layout.activity_main);
            mapping();
            updateView = new UpdateView() {
                @Override
                public void update() {
                    setView();
                }
            };
            if (!Mp3File.getInstance().getListSong().isEmpty()) {
                musicService.setUpdateView(updateView);
                musicService.setCallBack(MainActivity.this);
                displayListSongs();
            } else {
                Toast.makeText(MainActivity.this, "NO  MUSIC", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void StartPlayingActivity(int position, boolean isStartNewSong){
        Intent intent = new Intent(MainActivity.this, PlayingActivity.class);
        intent.putExtra("position", position);
        intent.putExtra("startNewSong", isStartNewSong);
        startActivityForResult(intent, REQUEST_FROM_PLAYING_ACTIVITY);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent intent = new Intent(this, MusicService.class);
        stopService(intent);
        unbindService(serviceConnection);
    }
}