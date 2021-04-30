package com.duonglh.musicapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.duonglh.musicapp.model.Data.Mp3File;
import com.duonglh.musicapp.model.Song.Song;
import com.duonglh.musicapp.model.Song.SongDataBase;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.security.Key;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements MyInterface.MediaPlayerAction{
    public static final int REQUEST_FROM_PLAYING_ACTIVITY = 1;
    public static final int REQUEST_PERMISSION = 2;
    private ProgressBar progressBar;
    private CircleImageView imageViewSongPlaying;
    private TextView textViewNameSongPlaying, textViewAuthorPlaying;
    private Button previousButton, nextButton, playingButton;
    private SearchView searchView;
    private ConstraintLayout mainPlaying;
    private MusicService musicService;
    private MyInterface.UpdateView updateView;
    private boolean isBoundService, once = false, isDownloaded = false;
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());
    private MyInterface.ResponseSearch responseSearch;
    private MyInterface.onKeyDown onKeyDown;
    private ViewPager viewPager;
    private BottomNavigationView bottomNavigationView;
    private ViewPagerAdapter viewPagerAdapter;
    private int currentFragment = 0;
    private RelativeLayout titleSearch;


    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent service = new Intent(MainActivity.this, MusicService.class);
        isBoundService = bindService(service, serviceConnection, BIND_AUTO_CREATE);

//        SongDataBase.getInstance(this).songDAO().deleteAllSong();
//        this.deleteDatabase("songs");

        startNewActivity();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent stopService = new Intent(this, MusicService.class);
        stopService(stopService);
    }

    @Override
    public void onBackPressed() {
        if(currentFragment == 2){
            if (!onKeyDown.press()) {
                viewPager.setCurrentItem(0);
            }
            return;
        }
        else if (!searchView.getQuery().toString().equals("")) {
            searchView.setQuery("",false);
            searchView.clearFocus();
            searchView.setIconified(true);
            return;
        }
        moveTaskToBack(true);
    }

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
    private void startNewActivity(){
        setContentView(R.layout.intro);
        getSupportActionBar().hide();
        checkPermissions();
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED &&
                    checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED) {
                String[] permission = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO};
                setContentView(R.layout.empty_layout);
                requestPermissions(permission, REQUEST_PERMISSION);
            } else {
                new LoadDataAsyncTask().execute();
            }
        } else {
            new LoadDataAsyncTask().execute();
            makeDir();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                setContentView(R.layout.intro);
                new LoadDataAsyncTask().execute();
                makeDir();
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_LONG).show();
                String[] permission = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO};
                requestPermissions(permission, REQUEST_PERMISSION);
            }
        }
    }

    private void mapping() {
        progressBar             = findViewById(R.id.progress_music);
        imageViewSongPlaying    = findViewById(R.id.main_playing_music_icon);
        textViewNameSongPlaying = findViewById(R.id.main_playing_name_song);
        textViewAuthorPlaying   = findViewById(R.id.main_name_author);
        previousButton          = findViewById(R.id.main_previous_button);
        nextButton              = findViewById(R.id.main_next_button);
        playingButton           = findViewById(R.id.main_play_button);
        mainPlaying             = findViewById(R.id.main_playing);
        searchView              = findViewById(R.id.searchView);
        bottomNavigationView    = findViewById(R.id.bottom_nav);
        viewPager               = findViewById(R.id.viewPager);
        titleSearch             = findViewById(R.id.titleSearch);
    }

    private void prepareUI() {

        setUpViewPager();
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.device:
                        viewPager.setCurrentItem(0);
                        break;
                    case R.id.download:
                        viewPager.setCurrentItem(1);
                        break;
                    case R.id.rank:
                        viewPager.setCurrentItem(2);
                        break;
                }
                return true;
            }
        });

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
                responseSearch = (MyInterface.ResponseSearch) viewPagerAdapter.getItem(currentFragment);
                responseSearch.response(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                responseSearch = (MyInterface.ResponseSearch) viewPagerAdapter.getItem(currentFragment);
                responseSearch.response(newText);

                return false;
            }
        });

    }

    private void preparePlaying() {
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_FROM_PLAYING_ACTIVITY) {
            if(musicService != null) {
                musicService.setUpdateView(updateView);
                musicService.setCallBack(this);
                if(mainPlaying.getVisibility() == View.GONE){
                    mainPlaying.setVisibility(View.VISIBLE);
                    once = true;
                    preparePlaying();
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
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            musicService.updateListSong();
            return null;
        }

        @RequiresApi(api = Build.VERSION_CODES.R)
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            setContentView(R.layout.activity_main);
            mapping();
            updateView = new MyInterface.UpdateView() {
                @Override
                public void update() {
                    setView();
                }
            };
            if (!Mp3File.getInstance().getListSong().isEmpty()) {
                musicService.setUpdateView(updateView);
                musicService.setCallBack(MainActivity.this);
                if(musicService.isPlaying()){
                    mainPlaying.setVisibility(View.VISIBLE);
                }
            } else {
                showNoSongDialog(Gravity.CENTER_VERTICAL);
                Toast.makeText(MainActivity.this, "NO  MUSIC", Toast.LENGTH_LONG).show();
            }
            prepareUI();
        }
    }

    public void StartPlayingActivity(int position, boolean isStartNewSong){
        Intent intent = new Intent(MainActivity.this, PlayingActivity.class);
        intent.putExtra("position", position);
        intent.putExtra("startNewSong", isStartNewSong);
        startActivityForResult(intent, REQUEST_FROM_PLAYING_ACTIVITY);
//        overridePendingTransition( R.anim.slide_up, R.anim.slide_down );
    }

    private void setUpViewPager(){

        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        viewPager.setAdapter(viewPagerAdapter);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }
            @Override
            public void onPageSelected(int position) {
                currentFragment = position;
                hideKeyBoard();
                switch (position){
                    case 0:
                        bottomNavigationView.getMenu().findItem(R.id.device).setChecked(true);
                        titleSearch.setVisibility(View.VISIBLE);
                        if(once) mainPlaying.setVisibility(View.VISIBLE);
                        bottomNavigationView.setVisibility(View.VISIBLE);
                        break;
                    case 1:
                        bottomNavigationView.getMenu().findItem(R.id.download).setChecked(true);
                        titleSearch.setVisibility(View.VISIBLE);
                        if(once) mainPlaying.setVisibility(View.VISIBLE);
                        bottomNavigationView.setVisibility(View.VISIBLE);
                        break;
                    case 2:
                        bottomNavigationView.getMenu().findItem(R.id.rank).setChecked(true);
                        titleSearch.setVisibility(View.GONE);
                        mainPlaying.setVisibility(View.GONE);
                        bottomNavigationView.setVisibility(View.GONE);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    @SuppressLint("SetTextI18n")
    private void showNoSongDialog(int gravity){
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_no_song);

        Window window = dialog.getWindow();
        if(window == null) return;

        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams windowAttributes = window.getAttributes();
        windowAttributes.gravity = gravity;
        window.setAttributes(windowAttributes);

        if(gravity == Gravity.CENTER_VERTICAL ){
            dialog.setCancelable(true);
        }
        else{
            dialog.setCancelable(false);
        }

        ImageButton directDownloadButton = dialog.findViewById(R.id.directDownloadButton);

        directDownloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                directToDownloadFragment();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public void hideKeyBoard(){
        if (!searchView.getQuery().equals("")) {
            searchView.setQuery("",false);
            searchView.clearFocus();
        }
        searchView.setIconified(true);
    }

    public void makeDir(){
        File SimpleMusicDir = new File(Environment.getExternalStorageDirectory().toString(),java.io.File.separator +"Simple Music");
        if (!SimpleMusicDir.exists()) {
            SimpleMusicDir.mkdir();
        }
    }

    public void setOnKeyDown(MyInterface.onKeyDown onKeyDown){
        this.onKeyDown = onKeyDown;
    }

    public void updateListSongService(){
        musicService.updateListSong();
    }

    public  void removeSongInService(int position){
        musicService.removeSong(position);
    }

    public void directToDownloadFragment(){
        viewPager.setCurrentItem(1);
    }

    public boolean isDownloaded() {
        return isDownloaded;
    }

    public void setDownloaded(boolean downloaded) {
        isDownloaded = downloaded;
    }
}