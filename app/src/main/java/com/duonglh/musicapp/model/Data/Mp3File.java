package com.duonglh.musicapp.model.Data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.duonglh.musicapp.model.Song.Song;
import com.duonglh.musicapp.model.Song.SongDataBase;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
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
    public void loadAllData(Context context) {
        this.context = context;
        // get From DataBase
        listSong = (ArrayList<Song>) SongDataBase.getInstance(context).songDAO().getListSongs();

//        getFromMediaAudio();

        getFromOtherLocation(Environment.getExternalStorageDirectory());

        listSong = (ArrayList<Song>) listSong.stream().sorted(Comparator.comparing(Song::getNameSong)).collect(Collectors.toList());

    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void getFromMediaAudio() {
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        final String[] project = {
                MediaStore.Audio.Media.TITLE,   // nameSong
                MediaStore.Audio.Media.ARTIST,  // namAuthor
                MediaStore.Audio.Media.DATA,     // path
                MediaStore.Audio.Media.DURATION  // time
        };
        @SuppressLint("Recycle")
        Cursor cursor = context.getContentResolver().query(uri, project, "is_music != 0", null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String nameSong = cursor.getString(0);
                String nameAuthor = cursor.getString(1);
                if(noSong(nameSong, nameAuthor)) {
                    String path = cursor.getString(2);
                    byte[] image = null;//getImageSong(path);
                    String duration = cursor.getString(3);
                    Song song = new Song(path, image, nameSong, nameAuthor, duration);
                    listSong.add(song);
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void getFromOtherLocation(File localFile) {
        File[] files = localFile.listFiles();
        if (files != null) {
            for (File singleFile : files) {
                if (singleFile.isDirectory() && !singleFile.isHidden()) {
                    String nameDirector = singleFile.getName();
                    if (nameDirector.equals("Zing MP3") | nameDirector.equals("Download") | nameDirector.equals("Music")) {
                        getFromOtherLocation(singleFile);
                    }
                } else {
                    if (singleFile.getName().endsWith(".mp3") || singleFile.getName().endsWith(".wav")) {
                        String path = singleFile.toString();
                        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                        retriever.setDataSource(path);
                        String nameSong = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                        if(nameSong == null){
                            nameSong = path.substring(path.lastIndexOf("/")+1, path.length()-4);
                        }
                        String nameAuthor = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                        if(noSong(nameSong, nameAuthor)) {
                            byte[] image = retriever.getEmbeddedPicture();
                            String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                            Song song = new Song(path, image, nameSong, nameAuthor, duration);
                            listSong.add(song);
                        }
                        retriever.close();
                        retriever.release();
                    }
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public boolean noSong(String nameSong, String nameAuthor){
        return listSong.stream().filter(s->(s.getNameSong().equals(nameSong) && s.getNameAuthor().equals(nameAuthor)))
        .findAny().orElse(null) == null;
    }

    public ArrayList<Song> getListSong(){
        return listSong;
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    public String getDurationSong(String path){
        try{
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(path);
            String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            retriever.release();
            return duration;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void addSong(Song song){
        listSong.add(song);
        listSong = (ArrayList<Song>) listSong.stream().sorted(Comparator.comparing(Song::getNameSong))
                .collect(Collectors.toList());
    }

}
