package com.duonglh.musicapp.service;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

public class MyNotificationChannel extends Application {

    public static final String CHANNEL_ID_1 = "notification_channel_1_music";
    public static final String CHANNEL_ID_2 = "notification_channel_2_music";
    public static final String ACTION_PREVIOUS = "action_Previous";
    public static final String ACTION_PLAY = "action_Play";
    public static final String ACTION_NEXT = "action_Next";
    public static final String ACTION_CANCEL = "action_Cancel";
    @Override
    public void onCreate() {
        super.onCreate();
        createChannelNotification();
    }

    private void createChannelNotification() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel1 = new NotificationChannel(CHANNEL_ID_1,"Channel1", NotificationManager.IMPORTANCE_HIGH);
            channel1.setDescription("Channel 1 Description");
            NotificationChannel channel2 = new NotificationChannel(CHANNEL_ID_2,"Channel2", NotificationManager.IMPORTANCE_HIGH);
            channel1.setDescription("Channel 2 Description");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel1);
            manager.createNotificationChannel(channel2);
        }
    }
}
