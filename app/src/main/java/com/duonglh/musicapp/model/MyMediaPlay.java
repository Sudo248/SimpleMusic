package com.duonglh.musicapp.model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;

import androidx.room.Update;

import com.duonglh.musicapp.MainActivity;
import com.duonglh.musicapp.PlayingActivity;
import com.duonglh.musicapp.model.Song.Song;

import java.util.List;
import java.util.Random;

public class MyMediaPlay {
    public static MediaPlayer mediaPlayer = new MediaPlayer();
    public static int currentSong = 0;
    public static boolean isShuffle = false, isLooping = false;
    public static List<Song> listSong;
    @SuppressLint("StaticFieldLeak")
    public static Context context;

    public interface UpdateView{
        void update();
    }

    private UpdateView view;

    public void setUpdateView(UpdateView view) {
        this.view = view;
    }

    public void createSong(int mCurrentSong){
        MyMediaPlay.currentSong = mCurrentSong;
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = MediaPlayer.create(context, Uri.parse(listSong.get(currentSong).getPath()));
    }

    public void play(int mCurrentSong){
        MyMediaPlay.currentSong = mCurrentSong;
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = MediaPlayer.create(context, Uri.parse(listSong.get(currentSong).getPath()));
        setCompletionSong();
        mediaPlayer.start();
    }

    public void play(){
        mediaPlayer.start();
    }

    public void pause(){
        mediaPlayer.pause();
    }

    public boolean isPlaying(){
        return mediaPlayer.isPlaying();
    }

    public void nextSong(){
        mediaPlayer.stop();
        mediaPlayer.release();
        currentSong = (currentSong + 1) % listSong.size();
        mediaPlayer = MediaPlayer.create(context, Uri.parse(listSong.get(currentSong).getPath()));
        setCompletionSong();
        mediaPlayer.start();
    }

    public void previousSong(){
        mediaPlayer.stop();
        mediaPlayer.release();
        currentSong = (currentSong - 1 < 0) ? (listSong.size() - 1) : (currentSong - 1);
        mediaPlayer = MediaPlayer.create(context, Uri.parse(listSong.get(currentSong).getPath()));
        setCompletionSong();
        mediaPlayer.start();
    }

    public void setCompletionSong(){
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if(isLooping){
                    mediaPlayer.seekTo(0);
                    mediaPlayer.start();
                }
                else if(isShuffle) {
                    int shuffle = new Random().nextInt(MyMediaPlay.listSong.size());
                    play(shuffle);
                    view.update();
                }
                else{
                    nextSong();
                    view.update();
                }

            }
        });
    }

    public boolean isShuffle(){
        return MyMediaPlay.isShuffle;
    }

    public void setShuffle(boolean shuffle){
        MyMediaPlay.isShuffle = shuffle;
    }

    public boolean isLooping(){
        return MyMediaPlay.isLooping;
    }

    public void setLooping(boolean looping){
        MyMediaPlay.isLooping = looping;
    }

    public void setCurrentSong(int currentSong){
        MyMediaPlay.currentSong = currentSong;
    }

    public int getCurrentSong(){
        return currentSong;
    }

    public int getTotalDuration(){
        return mediaPlayer.getDuration();
    }

    public int getCurrentDuration(){
        return mediaPlayer.getCurrentPosition();
    }

    public void seekTo(int position){
        mediaPlayer.seekTo(position);
    }

}
