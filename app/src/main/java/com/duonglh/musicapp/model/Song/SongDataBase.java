package com.duonglh.musicapp.model.Song;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Song.class}, version = 1, exportSchema = false)

public abstract class SongDataBase extends RoomDatabase {
    private static final String DATABASE_NAME = "songs.db";
    private static SongDataBase INSTANCE;

    public static synchronized SongDataBase getInstance(Context context){
        if(INSTANCE == null){
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),SongDataBase.class, DATABASE_NAME)
                    .allowMainThreadQueries()
                    .build();
        }
        return INSTANCE;
    }
    public abstract SongDAO songDAO();
}
