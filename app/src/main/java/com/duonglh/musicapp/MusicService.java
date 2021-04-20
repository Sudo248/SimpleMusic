package com.duonglh.musicapp;


import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.duonglh.musicapp.model.MyMediaPlayer;
import com.duonglh.musicapp.model.MyNotificationChannel;
import com.duonglh.musicapp.model.Song.Song;

public class MusicService extends Service {
    public final int REQUEST_CODE_NOTIFICATION = 1;
    public final int ID_NOTIFICATION = 2;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        sendNotification();

        return START_NOT_STICKY;// không khởi động lại service khi có cơ hội.
    }



    private void sendNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,REQUEST_CODE_NOTIFICATION,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        Song playingSong = MyMediaPlayer.getInstance().getCurrentSong();

//        Bitmap image = BitmapFactory.decodeByteArray(playingSong.getImage(),0,playingSong.getImage().length);
        Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.avatar);
        RemoteViews remoteView = new RemoteViews(getPackageName(),R.layout.notification);
        remoteView.setTextViewText(R.id.text_notification_name_song, playingSong.getNameSong());
        remoteView.setTextViewText(R.id.txt_notification_name_author, playingSong.getNameAuthor());
        remoteView.setImageViewBitmap(R.id.notification_image_song, image);
        Notification notification = new NotificationCompat.Builder(this, MyNotificationChannel.CHANNEL_ID)
                .setSmallIcon(R.drawable.music_icon)
                .setContentIntent(pendingIntent)
                .setCustomContentView(remoteView)
                .build();

        startForeground(ID_NOTIFICATION,notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
