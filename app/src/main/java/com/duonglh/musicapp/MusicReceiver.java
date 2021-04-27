package com.duonglh.musicapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import static com.duonglh.musicapp.model.MyNotificationChannel.ACTION_NEXT;
import static com.duonglh.musicapp.model.MyNotificationChannel.ACTION_PLAY;
import static com.duonglh.musicapp.model.MyNotificationChannel.ACTION_PREVIOUS;

public class MusicReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Intent serviceIntent = new Intent(context, MusicService.class);
        if(action != null){
            switch (action){
                case ACTION_PLAY:
                    serviceIntent.putExtra("action", "PlayPause");
                    context.startService(serviceIntent);
                    break;
                case ACTION_PREVIOUS:
                    serviceIntent.putExtra("action", "Previous");
                    context.startService(serviceIntent);
                    break;
                case ACTION_NEXT:
                    serviceIntent.putExtra("action","Next");
                    context.startService(serviceIntent);
                    break;
            }
        }
    }
}
