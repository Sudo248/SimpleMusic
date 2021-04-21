package com.duonglh.musicapp.model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;

import com.duonglh.musicapp.model.Song.Song;

import java.net.IDN;
import java.util.ArrayList;
import java.util.Random;

public class MyMediaPlayer {
    @SuppressLint("StaticFieldLeak")
    public static MediaPlayer MUSIC = new MediaPlayer();
    @SuppressLint("StaticFieldLeak")
    private static MyMediaPlayer INSTANCE;
    private Context currentContext;
    private ArrayList<Song> listSong;
    private int IDCurrentSong;
    private boolean shuffle;

    public static MyMediaPlayer getInstance(){
        if(INSTANCE == null){
            INSTANCE = new MyMediaPlayer();
        }
        return INSTANCE;
    }

    MyMediaPlayer(){
        IDCurrentSong = -1;
    }

    public interface ViewSong {
        void update();
    }

    private ViewSong viewSong;

    public void setViewSong(ViewSong viewSong){
        this.viewSong = viewSong;
    }

    public void setContext(Context context){
        currentContext = context;
    }

    public ArrayList<Song> getListSong(){
        return listSong;
    }

    public void setListSong(ArrayList<Song> mListSong){
        listSong = mListSong;
    }

    public void create(int mCurrentSong){
        MUSIC.stop();
        MUSIC.release();
        IDCurrentSong = mCurrentSong;
        MUSIC = MediaPlayer.create(currentContext, Uri.parse(listSong.get(IDCurrentSong).getPath()));
        setCompletionSong();
    }

    public void play(int mCurrentSong){
        create(mCurrentSong);
        setCompletionSong();
        MUSIC.start();
    }

    public void nextSong(){
        IDCurrentSong = ((IDCurrentSong + 1) % listSong.size());
        create(IDCurrentSong);
        setCompletionSong();
        MUSIC.start();
    }

    public void previousSong(){
        IDCurrentSong = ((IDCurrentSong - 1 < 0) ? (listSong.size() - 1) : (IDCurrentSong - 1));
        create(IDCurrentSong);
        setCompletionSong();
        MUSIC.start();
    }

    public void setCompletionSong(){
        MUSIC.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if(shuffle) {
                    int num_shuffle = new Random().nextInt(listSong.size());
                    play(num_shuffle);
                }
                else{
                    nextSong();
                }
                viewSong.update();
            }
        });
    }

    public void setShuffle(boolean mShuffle){
        shuffle = mShuffle;
    }

    public boolean isShuffle(){
        return shuffle;
    }

    public int getIDCurrentSong(){
        return IDCurrentSong;
    }

    public Song getCurrentSong(){
        return listSong.get(IDCurrentSong);
    }










}
