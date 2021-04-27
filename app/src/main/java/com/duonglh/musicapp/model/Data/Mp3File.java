package com.duonglh.musicapp.model.Data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.annotation.RequiresApi;

import com.duonglh.musicapp.model.MyMediaPlayer;
import com.duonglh.musicapp.model.Song.Song;
import com.duonglh.musicapp.model.Song.SongDataBase;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Mp3File {

    @SuppressLint("StaticFieldLeak")
    private static Mp3File MP3INSTANCE = null;
    private Context context;
    private ArrayList<Song> listSong;

    public Mp3File(){
        listSong = new ArrayList<>();
    }

    public static Mp3File getInstance(){
        if(MP3INSTANCE == null){
            MP3INSTANCE = new Mp3File();
        }
        return MP3INSTANCE;
    }

    // get Data From Storage
    @RequiresApi(api = Build.VERSION_CODES.R)
    public ArrayList<Song> loadAllData(Context context) {
        this.context = context;
        // get From DataBase
        SongDataBase.getInstance(context).songDAO().deleteAllSong();
        listSong = (ArrayList<Song>) SongDataBase.getInstance(context).songDAO().getListSongs();
        listSong.addAll(getFromMediaAudio());
        listSong.addAll(getFromOtherLocation(Environment.getExternalStorageDirectory()));
        listSong = (ArrayList<Song>) listSong.stream().sorted(Comparator.comparing(Song::getNameSong))
                .collect(Collectors.toList());
        return listSong;
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private ArrayList<Song> getFromMediaAudio() {
        ArrayList<Song> mListSong = new ArrayList<>();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        final String project[] = {
                MediaStore.Audio.Media.TITLE,    // nameSong
                MediaStore.Audio.Media.DATA,     // path
                MediaStore.Audio.Media.ARTIST,   // namAuthor
                MediaStore.Audio.Media.DURATION  // time
        };
        @SuppressLint("Recycle")
        Cursor cursor = context.getContentResolver().query(uri, project, "is_music != 0", null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String nameSong = cursor.getString(0);
                if (!hasSong(nameSong)) {
                    String path = cursor.getString(1);
                    byte[] image = null;//getImageSong(path);
                    String nameAuthor = cursor.getString(2);
                    String duration = cursor.getString(3);
                    Song song = new Song(path, image, nameSong, nameAuthor, duration);
                    mListSong.add(song);
                    SongDataBase.getInstance(context).songDAO().insertSong(song);
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
        return mListSong;
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private ArrayList<Song> getFromOtherLocation(File localFile) {
        ArrayList<Song> mListSong = new ArrayList<>();
        File[] files = localFile.listFiles();
        if (files != null) {
            for (File singleFile : files) {
                if (singleFile.isDirectory() && !singleFile.isHidden()) {
                    String nameDirector = singleFile.getName();
                    if (nameDirector.equals("Zing MP3") || nameDirector.equals("Music")) {
                        mListSong.addAll(getFromOtherLocation(singleFile));
                    }
                } else {
                    if (singleFile.getName().endsWith(".mp3") || singleFile.getName().endsWith(".wav")) {
                        String path = singleFile.toString();
                        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                        retriever.setDataSource(path);
                        String nameSong = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                        if (!hasSong(nameSong)) {
                            String nameAuthor = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                            byte[] image = retriever.getEmbeddedPicture();
                            String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                            Song song = new Song(path, image, nameSong, nameAuthor, duration);
                            mListSong.add(song);
                            SongDataBase.getInstance(context).songDAO().insertSong(song);
                        }
                        retriever.release();
                    }
                }
            }
        }
        return mListSong;
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private boolean hasSong(@NotNull String nameSong) {
        List<Song> list = new ArrayList<>();
        list = listSong.stream()
                .filter(item -> item.getNameSong().equals(nameSong))
                .collect(Collectors.toList());
        return list.size() > 0;
    }

    private void getDataSong(String path) {

    }

    private void getDefaultImage(){

    }

    public ArrayList<Song> getListSong(){
        return listSong;
    }

}
