package com.duonglh.musicapp;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.widget.RemoteViews;


import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.duonglh.musicapp.model.Data.Mp3File;
import com.duonglh.musicapp.model.Song.Song;

import java.util.ArrayList;
import java.util.Random;

import static com.duonglh.musicapp.model.MyNotificationChannel.ACTION_NEXT;
import static com.duonglh.musicapp.model.MyNotificationChannel.ACTION_PLAY;
import static com.duonglh.musicapp.model.MyNotificationChannel.ACTION_PREVIOUS;
import static com.duonglh.musicapp.model.MyNotificationChannel.CHANNEL_ID_1;

public class MusicService extends Service {
    public final int REQUEST_CODE_NOTIFICATION = 1;
    public final int ID_NOTIFICATION = 2;
    private MediaPlayer MUSIC;
    private int IDCurrentSong;
    private boolean isShuffle;
    private ArrayList<Song> listSong;
    private MyInterface.UpdateView updateView;
    private final IBinder iBinder = new MusicBinder();
    private MediaSessionCompat mediaSessionCompat;
    private MyInterface.MediaPlayerAction playerAction;
    PendingIntent contentPending, nextPending, playPausePending, previousPending;


    @Override
    public void onCreate() {
        super.onCreate();
        mediaSessionCompat = new MediaSessionCompat(this, "Simple Music");

        Intent intent = new Intent(this, PlayingActivity.class);
        contentPending = PendingIntent.getActivity(this, REQUEST_CODE_NOTIFICATION, intent, 0);

        Intent preIntent = new Intent(this, MusicReceiver.class).setAction(ACTION_PREVIOUS);
        previousPending = PendingIntent.getBroadcast(this, REQUEST_CODE_NOTIFICATION, preIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent playPauseIntent = new Intent(this, MusicReceiver.class).setAction(ACTION_PLAY);
        playPausePending = PendingIntent.getBroadcast(this, REQUEST_CODE_NOTIFICATION, playPauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent nextIntent = new Intent(this, MusicReceiver.class).setAction(ACTION_NEXT);
        nextPending = PendingIntent.getBroadcast(this, REQUEST_CODE_NOTIFICATION, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        int idCurrentSong = intent.getIntExtra("CurrentSong", -1);
        if(idCurrentSong != -1){
            if(MUSIC == null ){
                create(idCurrentSong);
                MUSIC.start();
            }
        }

        String action = intent.getStringExtra("action");
        if(action != null){
            switch (action){
                case "PlayPause":
                    playerAction.play();
                    break;
                case "Previous":
                    playerAction.previousSong();
                    break;
                case "Next":
                    playerAction.nextSong();
                    break;
            }
        }

        return START_STICKY;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    private void sendNotification(int playPauseButton) {

        byte[] image = listSong.get(IDCurrentSong).getImage();
        Bitmap picture;
        if(image != null){
            picture = BitmapFactory.decodeByteArray(image, 0 , image.length);
        }
        else{
            picture = BitmapFactory.decodeResource(getResources(), R.drawable.avatar);
        }

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID_1)
                .setSmallIcon(R.drawable.music_icon)
                .setLargeIcon(picture)
                .setContentTitle(listSong.get(IDCurrentSong).getNameSong())
                .setContentText(listSong.get(IDCurrentSong).getNameAuthor())
                .addAction(R.drawable.ic_baseline_skip_previous, "Previous", previousPending)
                .addAction(playPauseButton, "PlayPause", playPausePending)
                .addAction(R.drawable.ic_baseline_skip_next, "Next", nextPending)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(mediaSessionCompat.getSessionToken()))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true)
                .build();

        startForeground(ID_NOTIFICATION, notification);
    }


    public void setUpdateView(MyInterface.UpdateView updateView){
        this.updateView = updateView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void create(int IDCurrentSong) {

        this.IDCurrentSong = IDCurrentSong;
        if(MUSIC != null){
            MUSIC.stop();
            MUSIC.release();
        }
        Log.e("Duong",listSong.size()+"");
        MUSIC = MediaPlayer.create(getBaseContext(), Uri.parse(listSong.get(IDCurrentSong).getPath()));

        MUSIC.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if(isShuffle) {
                    int num_shuffle = new Random().nextInt(listSong.size());
                        create(num_shuffle);
                        play();
                }
                else{
                    nextSong();
                }
                updateView.update();
            }
        });
        sendNotification(R.drawable.ic_baseline_pause_circle);
    }

    public void play() {
        MUSIC.start();
        sendNotification(R.drawable.ic_baseline_pause_circle);
    }

    public void pause() {
        MUSIC.pause();
        sendNotification(R.drawable.ic_baseline_play_circle);
    }

    public void nextSong() {
        IDCurrentSong = ((IDCurrentSong + 1) % listSong.size());
        create(IDCurrentSong);
        MUSIC.start();
        Log.e("Duong next Song", " "+listSong.size());
    }

    public void previousSong() {
        IDCurrentSong = ((IDCurrentSong - 1 < 0) ? (listSong.size() - 1) : (IDCurrentSong - 1));
        create(IDCurrentSong);
        MUSIC.start();
    }

    public void setShuffle(boolean isShuffle) {
        this.isShuffle = isShuffle;
    }

    public void setLoop(boolean isLoop) {
        MUSIC.setLooping(isLoop);
    }

    public int getCurrentPosition() {
        return MUSIC.getCurrentPosition();
    }

    public int getDuration() {
        return MUSIC.getDuration();
    }

    public int getAudioSessionId() {
        return MUSIC.getAudioSessionId();
    }

    public void SeekTo(int position) {
        MUSIC.seekTo(position);
    }


    public boolean isShuffle() {
        return isShuffle;
    }

    public boolean isPlaying() {
        return MUSIC.isPlaying();
    }

    public boolean isLooping() {
        return MUSIC.isLooping();
    }

    public Song getCurrentSong() {
        return listSong.get(IDCurrentSong);
    }

    public void updateListSong(){
        this.listSong = Mp3File.getInstance().getListSong();
    }

    public void removeSong(int position){
        if(position < IDCurrentSong) IDCurrentSong--;
    }

    public class MusicBinder extends Binder{
        public MusicService getService(){
            return MusicService.this;
        }
    }
    public void setCallBack(MyInterface.MediaPlayerAction playerAction){
        this.playerAction = playerAction;
    }

}

